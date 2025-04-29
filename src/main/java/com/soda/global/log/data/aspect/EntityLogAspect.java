package com.soda.global.log.data.aspect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.soda.article.dto.article.ArticleViewResponse;
import com.soda.article.dto.comment.CommentDTO;
import com.soda.article.entity.Article;
import com.soda.article.entity.Comment;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.log.data.domain.DataLog;
import com.soda.global.log.data.domain.DataLogRepository;
import com.soda.member.dto.company.CompanyResponse;
import com.soda.member.dto.company.MemberResponse;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.project.dto.ProjectDTO;
import com.soda.project.dto.stage.StageResponse;
import com.soda.project.entity.Project;
import com.soda.project.entity.Stage;
import com.soda.request.dto.request.RequestDTO;
import com.soda.request.dto.response.ResponseDTO;
import com.soda.request.entity.Request;
import com.soda.request.entity.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EntityLogAspect {

    private final DataLogRepository dataLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ModelMapper modelMapper;

    private static final Map<Class<?>, Function<Object, Object>> dtoConverters = new HashMap<>();

    static {
        dtoConverters.put(Member.class, e -> MemberResponse.fromEntity((Member) e)); // 멤버 조회 DTO가 아직 없어 주석처리 해두었음
        dtoConverters.put(Company.class, e -> CompanyResponse.fromEntity((Company) e));

        dtoConverters.put(Project.class, e -> ProjectDTO.fromEntity((Project) e));
        dtoConverters.put(Stage.class, e -> StageResponse.fromEntity((Stage) e));

        dtoConverters.put(Request.class, e -> RequestDTO.fromEntity((Request) e));
        dtoConverters.put(Response.class, e -> ResponseDTO.fromEntity((Response) e));

        dtoConverters.put(Article.class, e -> ArticleViewResponse.fromEntity((Article) e));
        dtoConverters.put(Comment.class, e -> CommentDTO.fromEntity((Comment) e));
    }

    @Around("@annotation(annotation)")
    public Object logEntityAction(ProceedingJoinPoint joinPoint, LoggableEntityAction annotation) throws Throwable {
        String action = annotation.action();
        Class<?> entityClass = annotation.entityClass();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String operator = "system";
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            operator = auth.getName();
        }

        Object[] args = joinPoint.getArgs();

        // 변경 전 데이터 조회
        Map<String, Object> beforeData = null;
        Object entityIdFromArgs = getIdFromArgs(args);
        if (("UPDATE".equals(action) || "DELETE".equals(action)) && entityIdFromArgs != null) {
            Object beforeEntity = loadEntityFromDb(entityClass, entityIdFromArgs);
            beforeData = convertToMap(beforeEntity);
        }

        // 실제 메서드 실행
        Object result = joinPoint.proceed();

        // 변경 후 데이터 처리
        Map<String, Object> afterData = null;
        if (!"DELETE".equals(action)) {
            if (result instanceof ResponseEntity<?> responseEntity) {
                Object body = responseEntity.getBody();
                afterData = convertToMap(body);
            } else {
                afterData = convertToMap(result);
            }
        }

        String entityIdStr = null;
        if (result != null) {
            entityIdStr = extractId(result);
        } else if (entityIdFromArgs != null) {
            entityIdStr = entityIdFromArgs.toString();
        }

        Map<String, Object> diff = null;
        if ("UPDATE".equals(action) && beforeData != null && afterData != null) {
            diff = computeDiff(beforeData, afterData);
        }

        DataLog dataLog = DataLog.builder()
                .entityName(entityClass.getSimpleName())
                .entityId(entityIdStr)
                .action(action)
                .operator(operator)
                .timestamp(LocalDateTime.now())
                .beforeData(beforeData)
                .beforeDataText(objectMapper.writeValueAsString(beforeData))
                .afterData(afterData)
                .afterDataText(objectMapper.writeValueAsString(afterData))
                .diff(diff)
                .build();

        try {
            dataLogRepository.save(dataLog);
        } catch (Exception e) {
            log.error("데이터 로그 저장 실패", e);
        }

        return result;
    }


    private Map<String, Object> computeDiff(Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> diff = new HashMap<>();
        for (String key : after.keySet()) {
            Object beforeVal = before.getOrDefault(key, "N/A"); // 디폴트 값 설정
            Object afterVal = after.getOrDefault(key, "N/A");
            if (beforeVal == null) beforeVal = "N/A";
            if (!Objects.equals(beforeVal, afterVal)) {
                diff.put(key, Map.of("before", beforeVal, "after", afterVal));
            }
        }
        return diff;
    }

    private Map<String, Object> convertToMap(Object entity) {
        if (entity == null) return null;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            Function<Object, Object> converter = dtoConverters.get(entity.getClass());

            Object dto;
            if (converter != null) {
                dto = converter.apply(entity);
            } else {
                dto = modelMapper.map(entity, Object.class);
            }

            return mapper.convertValue(dto, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("객체를 Map으로 변환 실패: {}", e.getMessage());
            return Map.of("error", "변환 실패");
        }
    }

    private Object getIdFromArgs(Object[] args) {
        return Arrays.stream(args)
                .filter(arg -> arg instanceof Long || arg instanceof String)
                .findFirst()
                .orElse(null);
    }

    private Object loadEntityFromDb(Class<?> entityClass, Object id) {
        String repoBeanName = Character.toLowerCase(entityClass.getSimpleName().charAt(0)) +
                entityClass.getSimpleName().substring(1) + "Repository";

        try {
            Object repository = applicationContext.getBean(repoBeanName);
            Method findById = Arrays.stream(repository.getClass().getInterfaces())
                    .flatMap(i -> Arrays.stream(i.getMethods()))
                    .filter(m -> m.getName().equals("findById"))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchMethodException("findById not found"));
            Object result = findById.invoke(repository, id);
            if (result instanceof Optional<?>) {
                return ((Optional<?>) result).orElse(null);
            }
        } catch (Exception e) {
            log.error("엔티티 로딩 실패", e);
        }
        return null;
    }

    private String extractId(Object entity) {
        try {
            Field idField = Arrays.stream(entity.getClass().getDeclaredFields())
                    .filter(f -> f.getName().equalsIgnoreCase("id"))
                    .findFirst().orElse(null);
            if (idField == null) return null;
            idField.setAccessible(true);
            Object idValue = idField.get(entity);
            return idValue != null ? idValue.toString() : null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}