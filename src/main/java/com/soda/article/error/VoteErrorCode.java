package com.soda.article.error;

import com.soda.global.response.ErrorCode;
import org.springframework.http.HttpStatus;

public enum VoteErrorCode implements ErrorCode {
    VOTE_ALREADY_CLOSED("1301", "마감된 투표에는 응답할 수 없습니다.", HttpStatus.BAD_REQUEST),
    VOTE_TEXT_ANSWER_NOT_ALLOWED("1302", "이 투표는 텍스트 답변을 허용하지 않습니다", HttpStatus.BAD_REQUEST),
    VOTE_MULTIPLE_SELECTION_NOT_ALLOWED("1303", "이 투표는 복수 선택을 허용하지 않습니다", HttpStatus.BAD_REQUEST),
    VOTE_NOT_FOUND("1304", "해당 Vote를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    VOTE_ALREADY_EXISTS("1305", "이미 투표가 존재합니다.", HttpStatus.BAD_REQUEST),
    VOTE_ITEM_REQUIRED("1306", "항목 선택 투표에는 최소 하나 이상의 항목이 필요합니다.", HttpStatus.BAD_REQUEST),
    VOTE_CANNOT_HAVE_BOTH_ITEMS_AND_TEXT("1307", "텍스트 답변을 허용하는 투표에는 선택 항목을 지정할 수 없습니다.", HttpStatus.BAD_REQUEST),
    VOTE_DUPLICATE_ITEM_TEXT("1308", "투표 항목에 중복된 내용이 포함되어 있습니다.", HttpStatus.BAD_REQUEST);;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    VoteErrorCode (String code, String message, HttpStatus httpStatus) {
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