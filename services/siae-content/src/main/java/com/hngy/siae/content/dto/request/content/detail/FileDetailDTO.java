package com.hngy.siae.content.dto.request.content.detail;

import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDetailDTO implements ContentDetailDTO {
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
}
