package com.soda.project.domain.stage.common.file;

import com.soda.project.interfaces.stage.common.file.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Service s3Service;
    private final FileFactory fileFactory;

    public PresignedUploadResponse getPresignedUrls(List<FileUploadRequest> files) {
        List<PresignedUploadResponse.Entry> entries = fileFactory.generateFileEntries(files);
        return PresignedUploadResponse.builder().entries(entries).build();
    }

    public FileConfirmResponse confirmUpload(FileStrategy strategy, Object domain, List<ConfirmedFile> confirmedFiles) {
        List<FileBase> entities = fileFactory.makeFileEntities(strategy, domain, confirmedFiles);
        strategy.saveAll(entities);
        return FileConfirmResponse.fromEntity(entities);
    }

    public FileDeleteResponse delete(FileBase file) {
        file.delete();
        return FileDeleteResponse.fromEntity(file);
    }
}
