package com.soda.project.domain.stage.common.file;

import com.soda.common.BaseEntity;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class FileBase extends BaseEntity {

    protected String name;

    protected String url;

    public void delete() {
        markAsDeleted();
    }

    public abstract Long getDomainId();  // 추상 메서드로 도메인 ID를 가져옴
}
