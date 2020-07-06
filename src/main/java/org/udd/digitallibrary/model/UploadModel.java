package org.udd.digitallibrary.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadModel {
    private String magazine;
    private String title;
    private String authors;
    private String keywords;
    private String areas;
    private MultipartFile file;
}
