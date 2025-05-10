package com.soda.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    // 댓글 관련
    NEW_COMMENT_ON_POST("게시글 댓글", "새로운 댓글이 달렸습니다."),
    NEW_REPLY_ON_POST("게시글 대댓글", "댓글에 새로운 답글이 달렸습니다."),
    NEW_REPLY_TO_MY_COMMENT("내 댓글 답글", "회원님의 댓글에 답글이 달렸습니다."),

    // 작업 관련
    TASK_ASSIGNED("작업 할당", "새로운 작업이 할당되었습니다."),
    TASK_COMPLETED("작업 완료", "담당하신 작업이 완료되었습니다."),

    // 승인 요청 관련
    APPROVAL_REQUEST_RECEIVED("승인 요청 수신", "새로운 승인 요청이 도착했습니다."),
    APPROVAL_RESPONSE_RECEIVED("승인 요청 응답", "요청하신 승인 건에 대한 답변이 도착했습니다."),

    // 공지사항 등 기타
    SYSTEM_NOTICE("시스템 공지", "새로운 공지사항이 있습니다.");

    private final String description;
    private final String defaultMessage;

}
