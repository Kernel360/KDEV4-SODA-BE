<div align="center">

  <img src="https://github.com/user-attachments/assets/76917afe-33de-49c9-a239-b85911faf3b7" alt="SODA 로고" width="200"/>

  <h1><strong>SODA</strong> : 소통의 사다리</h1>


  <p>2025년 03월 17일 ~ 2025년 06월 19일</p>
  <p>
    웹 에이전시와 고객사 간의 원활한 협업을 위한<br/>
    ✨ <strong>올인원 프로젝트 관리 플랫폼</strong> ✨
  </p>

  <p>
    <a href="https://www.s0da.co.kr/" target="_blank">
      <img src="https://img.shields.io/badge/SODA%20웹사이트%20방문하기-F59E0B?style=for-the-badge&logo=Rocket&logoColor=white" alt="SODA 바로가기" />
    </a>
  </p>

</div>

<details>
<summary><h2>💡 주요 기능 </h2></summary>

|로그인|어드민 대시보드|회사 생성|
|:-:|:-:|:-:|
|![로그인](https://github.com/user-attachments/assets/16f01831-dc6a-4cd5-923d-956f398f33c5)|![어드민대시보드](https://github.com/user-attachments/assets/e6fb4afa-2cc4-4763-8170-9ad166416fb0)|![회사생성](https://github.com/user-attachments/assets/97fdde2f-b88f-44bb-9758-6f7cf8ff4f6b)|
|<b>유저 생성</b>|<b>프로젝트 생성</b>|<b>데이터 확인</b>|
|![유저생성](https://github.com/user-attachments/assets/5166a8d2-5dd5-4243-8a16-1c03eaa80189)|![프로젝트생성](https://github.com/user-attachments/assets/f9be97b3-ba92-4f57-bafb-c41f0e4c3dcc)|![데이터로그](https://github.com/user-attachments/assets/fce435f3-b795-40c4-b552-a944ee242ca1)|
|<b>온보딩</b>|<b>유저 대시보드</b>|<b>승인요청 목록</b>|
|![온보딩](https://github.com/user-attachments/assets/ad8bae63-000b-441c-80f4-b43e1bd28739)|![사용자대시보드](https://github.com/user-attachments/assets/b7f75d03-7213-4041-bc8d-197d521f0a5c)|![승인요청목록](https://github.com/user-attachments/assets/b2ad3773-d811-451c-aa96-966450cd6c7f)|
|<b>질문 목록</b>|<b>알림</b>|<b>승인요청 생성</b>|
|![질문목록](https://github.com/user-attachments/assets/f5f2c8a1-e8d1-4106-9097-04d2d1cf5af7)|![알림](https://github.com/user-attachments/assets/ee1d5441-ac5f-4fec-b6c4-92df55d8a53c)|![승인요청생성](https://github.com/user-attachments/assets/e95f61e6-ce5c-496c-8524-c16d1ebd578d)|
|<b>승인응답 생성</b>|<b>질문 생성</b>||
|![승인응답생성](https://github.com/user-attachments/assets/2d2deeb8-d445-4044-bc67-d6052cb79c6f)|![질문작성](https://github.com/user-attachments/assets/50a8deb6-7061-4c98-bd69-72d9fc4d5477)||

</details>


<details>
<summary><h2>🚀 부하 테스트 및 성능 개선</h2></summary>

<br>

자세한 내용은 [성능 개선](https://quilled-authority-705.notion.site/1fab5a5bac5380af8dfbc7bf697d691c?pvs=74)에 기술해두었습니다.

### 부하 테스트 시나리오
실제 사용자 이용 패턴을 고려하여, 로그인 이후의 일반적인 워크플로우를 아래와 같이 구성했습니다.
1. 최근 승인요청 조회 (22%)
2. 최근 질문 조회 (22%)
3. 참여 중인 프로젝트 조회 (22%)
4. 프로젝트 상세 - 승인요청 조회 (20%)
5. 프로젝트 상세 - 질문 조회 (10%)
6. 승인요청 생성 (2%)
7. 질문 생성 (2%)


<br/>

### 부하 테스트 결과
| 단계 | 개선 전략 | 최대 TPS | API 최대 응답시간 | 부하테스트 이후 병목 지점 |
| :--- | :--- | :--- | :--- | :--- |
| **0. 초기 상태** | - | `2.3/s` | `46s` | `RDS CPU` |
| **1. 쿼리 최적화** | N+1 해결, 인덱싱, 쿼리 최적화 | `~50/s` | `~80ms` | `EC2 CPU` |
| **2. Scale-out** | ELB + EC2 인스턴스 추가 | `~160/s` | `~80ms` | `EC2 CPU` |
| **3. 캐싱 적용** | Redis 도입 | **`~200/s`** | **`< 30ms`** | `EC2 CPU` |

<br/>

### API 성능 개선
| API 명 | 개선 전 (p95) | 최종 개선 후 (p95) | 개선 방법 | 개선 결과 |
| :----: | :-----------: | :-----------: | :------: | :------: |
| **1. 사용자 최근 승인요청 목록 조회** | `약 29초` | `약 30ms` | N+1 해결, 쿼리 최적화, 인덱싱, 캐싱 | **약 967배 향상** |
| **2. 사용자 최근 질문 목록 조회** | `약 416ms` | `약 30ms` | 쿼리 최적화, 인덱싱, 캐싱 | **약 14배 향상** |
| **3. 참여중인 프로젝트 목록 조회** | `약 506ms` | `약 25ms` | 쿼리 최적화, 인덱싱, 캐싱 | **약 20배 향상** |
| **4. 특정 프로젝트 승인요청 목록 조회** | `약 150ms` | `약 70ms` | N+1 해결, 인덱싱 | **약 2.1배 향상** |
| **5. 특정 프로젝트 질문 목록 조회** | `약 661ms` | `약 129ms` | N+1 해결, 인덱싱 | **약 5.1배 향상** |

</details>


<details>
<summary><h2> ⚙️ 시스템 아키텍처 </h2></summary>
  
![image](https://github.com/user-attachments/assets/fe24f694-c722-4a0f-b234-813e162f3687)

</details>



<details>
<summary><h2> 👨‍🔧 기술 스택 </h2></summary>
<p>
  <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=java&logoColor=white" alt="Java 17" height="24"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.4.3" height="24"/>
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Data JPA" height="24"/>
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Security" height="24"/>
  <img src="https://img.shields.io/badge/Spring%20MVC-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring MVC" height="24"/>
</p>

<p>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" height="24"/>
  <img src="https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white" alt="MongoDB" height="24"/>
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis" height="24"/>
  <img src="https://img.shields.io/badge/QueryDSL-5.0.0-4A90E2?style=for-the-badge&logo=querydsl&logoColor=white" alt="QueryDSL 5.0.0 (Jakarta)" height="24"/>
</p>
<p>
  <img src="https://img.shields.io/badge/SpringDoc%20OpenAPI-2.8.5-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="SpringDoc OpenAPI (Swagger) 2.8.5" height="24"/>
</p>

<p>
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT (jjwt, java-jwt)" height="24"/>
</p>

<p>
  <img src="https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazonaws&logoColor=white" alt="AWS S3 (SDK v2)" height="24"/>
  <img src="https://img.shields.io/badge/Spring%20Mail%20(SMTP)-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Mail (SMTP)" height="24"/>
</p>

<p>
  <img src="https://img.shields.io/badge/Lombok-black?style=for-the-badge&logo=projectlombok&logoColor=white" alt="Lombok" height="24"/>
  <img src="https://img.shields.io/badge/ModelMapper-3.2.0-orange?style=for-the-badge" alt="ModelMapper 3.2.0" height="24"/>
  <img src="https://img.shields.io/badge/Jackson%20(JSR310)-E06C1D?style=for-the-badge" alt="Jackson JSR310" height="24"/>
  <img src="https://img.shields.io/badge/Spring%20Dotenv-4.0.0-22A7F0?style=for-the-badge" alt="Spring Dotenv 4.0.0" height="24"/>
  <img src="https://img.shields.io/badge/Bean%20Validation-6DB33F?style=for-the-badge&logo=hibernate&logoColor=white" alt="Bean Validation" height="24"/>
</p>

<p>
  <img src="https://img.shields.io/badge/Logstash%20Logback%20Encoder-7.4-00A5B1?style=for-the-badge&logo=logstash&logoColor=white" alt="Logstash Logback Encoder 7.4" height="24"/>
</p>

<p>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle" height="24"/>
  <img src="https://img.shields.io/badge/JUnit%205-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit 5" height="24"/>
</p>

</details>


<details>
<summary><h2>👨‍💻 팀원 소개 </h2></summary>
  
|이름|역할|깃허브|
|---|--------|---|
|정서연|프론트 CI/CD 구축 <br/> Project, Project Article, Comment API 개발|[seoyeon-jung](https://github.com/seoyeon-jung)|
|조준범|백엔드 CI/CD 구축 <br/> Request, Response API 개발 <br/> 시스템 로그, 데이터 로그 API 개발|[JUNBEOM CHO](https://github.com/JunbeomKoreaUniv)|
|윤다빈|User, Company, Notification API 개발 <br/> Spring Security|[yoodab](https://github.com/yoodab)|

</details>



<br/>

팀 협업 과정이 궁금하다면? 👉 [팀 노션 바로가기](https://quilled-authority-705.notion.site/SODA-4-1c2b5a5bac5380cb98d0cd207b7dfe58?pvs=74)
