package com.soda.global.init;

import com.soda.member.Member;
import com.soda.member.MemberProjectRole;
import com.soda.member.enums.MemberRole;
import com.soda.member.repository.MemberRepository;
import com.soda.project.MemberProject;
import com.soda.project.Project;
import com.soda.project.Stage;
import com.soda.project.task.Task;
import com.soda.project.MemberProjectRepository;
import com.soda.project.ProjectRepository;
import com.soda.project.StageRepository;
import com.soda.project.task.TaskRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TestDataLoader {

    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final MemberProjectRepository memberProjectRepository;
    private final StageRepository stageRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // 멤버 생성
        Member dabin = memberRepository.save(Member.builder()
                .name("윤다빈")
                .authId("dabin1234")
                .password(passwordEncoder.encode("password1234"))  // 비밀번호 인코딩 필요 시 처리
                .email("dabin@example.com")
                .role(MemberRole.USER)
                .build());

        Member seoyeon = memberRepository.save(Member.builder()
                .name("정서연")
                .authId("seoyeon1234")
                .password(passwordEncoder.encode("password1234"))
                .email("seoyeon@example.com")
                .role(MemberRole.USER)
                .build());

        Member junbeom = memberRepository.save(Member.builder()
                .name("조준범")
                .authId("junbeom1234")
                .password(passwordEncoder.encode("password1234"))
                .email("junbeom@example.com")
                .role(MemberRole.USER)
                .build());

        // 프로젝트 생성
        Project soda = projectRepository.save(new Project("SODA", "소다", LocalDateTime.now(), LocalDateTime.now()));
        Project vivim = projectRepository.save(new Project("VIVIM", "비빔", LocalDateTime.now(), LocalDateTime.now()));

        // 멤버-프로젝트 매핑
        MemberProject memberProject1 = memberProjectRepository.save(new MemberProject(dabin, soda, MemberProjectRole.DEV_MANAGER));
        MemberProject memberProject2 = memberProjectRepository.save(new MemberProject(seoyeon, soda, MemberProjectRole.CLI_MANAGER));
        MemberProject memberProject3 = memberProjectRepository.save(new MemberProject(junbeom, soda, MemberProjectRole.DEV_PARTICIPANT));

        // 멤버에 멤버프로젝트 필드 주입
        dabin.setMemberProjects(memberProject1);
        seoyeon.setMemberProjects(memberProject2);
        junbeom.setMemberProjects(memberProject3);
        memberRepository.save(dabin);
        memberRepository.save(seoyeon);
        memberRepository.save(junbeom);

        // 단계 생성
        Stage planning = stageRepository.save(new Stage("기획", 1F, soda));
        Stage development = stageRepository.save(new Stage("개발", 2.0F, soda));
        Stage deployment = stageRepository.save(new Stage("배포", 3.0F, soda));

        // 작업 생성
        taskRepository.save(new Task("기획하기", "기획하기~~",1F, planning));
        taskRepository.save(new Task("백엔드 프론트 개발", "프론트 백엔드 개발",2.0F, development));
        taskRepository.save(new Task("cicd 구축", "배포하기 젠킨스 써서",3.0F,deployment));
    }
}