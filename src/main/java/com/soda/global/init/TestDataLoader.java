//package com.soda.global.init;
//
//import com.soda.member.entity.Company;
//import com.soda.member.entity.Member;
//import com.soda.member.enums.CompanyProjectRole;
//import com.soda.member.enums.MemberProjectRole;
//import com.soda.member.enums.MemberRole;
//import com.soda.member.repository.CompanyRepository;
//import com.soda.member.repository.MemberRepository;
//import com.soda.project.company.CompanyProject;
//import com.soda.project.member.MemberProject;
//import com.soda.project.domain.Project;
//import com.soda.project.domain.stage.Stage;
//import com.soda.project.enums.ProjectStatus;
//import com.soda.project.infrastructure.CompanyProjectRepository;
//import com.soda.project.infrastructure.MemberProjectRepository;
//import com.soda.project.infrastructure.ProjectRepository;
//import com.soda.project.infrastructure.StageRepository;
//import com.soda.request.entity.Request;
//import com.soda.request.enums.RequestStatus;
//import com.soda.request.repository.RequestRepository;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
//@Component
//@RequiredArgsConstructor
//public class TestDataLoader {
//
//    private final MemberRepository memberRepository;
//    private final ProjectRepository projectRepository;
//    private final MemberProjectRepository memberProjectRepository;
//    private final StageRepository stageRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final CompanyRepository companyRepository;
//    private final CompanyProjectRepository companyProjectRepository;
//    private final RequestRepository requestRepository;
//
//    @PostConstruct
//    public void init() {
//
//        Company company1 = companyRepository.save(Company.builder()
//                .name("company1")
//                .phoneNumber("010-2211-2222")
//                .companyNumber("02-222-2222")
//                .ownerName("윤다빈")
//                .address("address1")
//                .build());
//
//
//        Company company2 = companyRepository.save(Company.builder()
//                .name("company2")
//                .phoneNumber("010-2211-2222")
//                .companyNumber("02-222-2222")
//                .ownerName("윤다빈")
//                .address("address1")
//                .build());
//
//
//        // 멤버 생성
//        Member dabin = memberRepository.save(Member.builder()
//                .name("윤다빈")
//                .authId("dabin1234")
//                .password(passwordEncoder.encode("password1234"))  // 비밀번호 인코딩 필요 시 처리
//                .email("dabin@example.com")
//                .role(MemberRole.USER)
//                .company(company2)
//                .build());
//
//        Member seoyeon = memberRepository.save(Member.builder()
//                .name("정서연")
//                .authId("seoyeon1234")
//                .password(passwordEncoder.encode("password1234"))
//                .email("seoyeon@example.com")
//                .role(MemberRole.USER)
//                .company(company1)
//                .build());
//
//        Member junbeom = memberRepository.save(Member.builder()
//                .name("조준범")
//                .authId("junbeom1234")
//                .password(passwordEncoder.encode("password1234"))
//                .email("junbeom@example.com")
//                .role(MemberRole.USER)
//                .company(company2)
//                .build());
//
//
//        // 프로젝트 생성
//        Project soda = projectRepository.save(new Project("SODA", "소다", LocalDateTime.now(), LocalDateTime.now(), ProjectStatus.IN_PROGRESS));
//
//        // 멤버-프로젝트 매핑
//        MemberProject memberProject1 = memberProjectRepository.save(new MemberProject(dabin, soda, MemberProjectRole.DEV_MANAGER));
//        MemberProject memberProject2 = memberProjectRepository.save(new MemberProject(seoyeon, soda, MemberProjectRole.CLI_MANAGER));
//        MemberProject memberProject3 = memberProjectRepository.save(new MemberProject(junbeom, soda, MemberProjectRole.DEV_PARTICIPANT));
//
//        CompanyProject companyProject1 = companyProjectRepository.save(CompanyProject.builder().company(company1).project(soda).companyProjectRole(CompanyProjectRole.CLIENT_COMPANY).build());
//        CompanyProject companyProject2 = companyProjectRepository.save(CompanyProject.builder().company(company2).project(soda).companyProjectRole(CompanyProjectRole.DEV_COMPANY).build());
//
//        // 멤버에 멤버프로젝트 필드 주입
//        dabin.setMemberProjects(memberProject1);
//        seoyeon.setMemberProjects(memberProject2);
//        junbeom.setMemberProjects(memberProject3);
//        memberRepository.save(dabin);
//        memberRepository.save(seoyeon);
//        memberRepository.save(junbeom);
//
//        // 단계 생성
//        Stage planning = stageRepository.save(new Stage("기획", 1F, soda));
//        Stage development = stageRepository.save(new Stage("개발", 2.0F, soda));
//        Stage deployment = stageRepository.save(new Stage("배포", 3.0F, soda));
//
//        Request projectCrud = requestRepository.save(Request.builder()
//                .member(dabin)
//                .stage(development)
//                .title("프로젝트 CRUD API 개발")
//                .status(RequestStatus.PENDING)
//                .content("프로젝트 skrr하게 개발 완료 ^_^")
//                .build());
//        Request rfpAnalyze = requestRepository.save(Request.builder()
//                        .member(junbeom)
//                        .stage(planning)
//                        .title("RFP분석")
//                        .status(RequestStatus.APPROVED)
//                        .content("RFP 1차로 분석 완료하였습니다.")
//                .build());
//    }
//}