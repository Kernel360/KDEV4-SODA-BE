package com.soda.article.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ArticleErrorCode implements ErrorCode {
    INVALID_INPUT("1101", "Invalid Input", HttpStatus.BAD_REQUEST),
    ARTICLE_ALREADY_DELETED("1102","This article is already deleted", HttpStatus.NOT_FOUND),
    INVALID_ARTICLE("1103", "The Article does not exist", HttpStatus.NOT_FOUND),
    PARENT_ARTICLE_NOT_FOUND("1104", "This parent article does not exist", HttpStatus.NOT_FOUND),
    ARTICLE_FILE_NOT_FOUND("1105", "This article file does not exist", HttpStatus.NOT_FOUND),
    USER_NOT_UPLOAD_ARTICLE_FILE("1106", "This user does not upload article_file", HttpStatus.NOT_FOUND),
    ARTICLE_LINK_NOT_FOUND("1107", "This link does not exist" , HttpStatus.NOT_FOUND ),
    USER_NOT_UPLOAD_ARTICLE_LINK("1108", "This user does not upload article_link" , HttpStatus.NOT_FOUND ),
    VOTE_ALREADY_CLOSED("1109", "마감된 투표에는 응답할 수 없습니다.", HttpStatus.BAD_REQUEST),
    VOTE_TEXT_ANSWER_NOT_ALLOWED("1110", "이 투표는 텍스트 답변을 허용하지 않습니다", HttpStatus.BAD_REQUEST),
    VOTE_MULTIPLE_SELECTION_NOT_ALLOWED("1111", "이 투표는 복수 선택을 허용하지 않습니다", HttpStatus.BAD_REQUEST);

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
