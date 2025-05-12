package com.soda.project.domain.stage.article.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ArticleErrorCode implements ErrorCode {
    INVALID_INPUT("1101", "잘못된 입력입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ARTICLE("1102", "해당 게시글이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    PARENT_ARTICLE_NOT_FOUND("1103", "부모 게시글이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    ARTICLE_FILE_NOT_FOUND("1104", "해당 게시글의 파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_UPLOAD_ARTICLE_FILE("1105", "해당 사용자가 업로드한 게시글 파일이 아닙니다.", HttpStatus.NOT_FOUND),
    ARTICLE_LINK_NOT_FOUND("1106", "해당 게시글의 링크를 찾을 수 없습니다." , HttpStatus.NOT_FOUND ),
    USER_NOT_UPLOAD_ARTICLE_LINK("1107", "해당 사용자가 업로드한 게시글 링크가 아닙니다." , HttpStatus.NOT_FOUND ),
    NO_PERMISSION_TO_MODIFY_ARTICLE("1108", "게시글 작성자만 투표를 생성할 수 있습니다.", HttpStatus.FORBIDDEN),
    ARTICLE_DATA_CONVERSION_ERROR("1109", "게시글 데이터 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    LINK_SIZE_EXCEEDED("1110", "게시글 링크는 최대 10개입니다", HttpStatus.BAD_REQUEST),
    NO_PERMISSION_TO_UPDATE_ARTICLE("1111", "게시글을 수정/삭제할 권한이 없습니다", HttpStatus.FORBIDDEN);;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ArticleErrorCode (String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
