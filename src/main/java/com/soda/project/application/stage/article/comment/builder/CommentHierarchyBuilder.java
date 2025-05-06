package com.soda.project.application.stage.article.comment.builder;

import com.soda.project.domain.stage.article.comment.Comment;
import com.soda.project.interfaces.dto.stage.article.comment.CommentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CommentHierarchyBuilder {

    public List<CommentDTO> buildHierarchy(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }
        log.debug("CommentHierarchyBuilder: 댓글 {}건으로 계층 구조 빌드 시작", comments.size());

        List<CommentDTO> allDTO = comments.stream()
                .map(CommentDTO::fromEntity)
                .toList();

        Map<Long, List<CommentDTO>> childrenMap = allDTO.stream()
                .filter(dto -> dto.getParentCommentId() != null)
                .collect(Collectors.groupingBy(CommentDTO::getParentCommentId));

        List<CommentDTO> rootCommentDTOs = allDTO.stream()
                .filter(dto -> dto.getParentCommentId() == null)
                .map(rootDto -> buildNodeWithChildren(rootDto, childrenMap))
                .toList();

        log.debug("CommentHierarchyBuilder: 계층 구조 빌드 완료 (최상위 댓글 {}건)", rootCommentDTOs.size());
        return rootCommentDTOs;
    }

    private CommentDTO buildNodeWithChildren(CommentDTO parentNode, Map<Long, List<CommentDTO>> childrenMap) {
        // 현재 부모 노드 ID에 해당하는 자식 DTO 목록을 찾음
        List<CommentDTO> children = childrenMap.getOrDefault(parentNode.getId(), Collections.emptyList());

        if (children.isEmpty()) {
            // 자식이 없으면 현재 노드(DTO)를 그대로 반환 (이미 자식 리스트는 비어있음)
            return parentNode;
        }

        // 자식들이 있다면, 각 자식에 대해 재귀적으로 하위 트리를 빌드
        List<CommentDTO> builtChildren = children.stream()
                .map(childNode -> buildNodeWithChildren(childNode, childrenMap))
                .collect(Collectors.toList());

        return parentNode.withChildComments(builtChildren);
    }
}
