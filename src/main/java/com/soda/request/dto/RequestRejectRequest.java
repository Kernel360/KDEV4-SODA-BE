package com.soda.request.dto;

import com.soda.article.domain.ArticleFileDTO;
import com.soda.article.domain.ArticleLinkDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class RequestRejectRequest {
    private String comment;
    private Long projectId;
    private List<ResponseLinkDTO> links;
}
