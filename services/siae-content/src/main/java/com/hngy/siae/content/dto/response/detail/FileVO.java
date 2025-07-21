package com.hngy.siae.content.dto.response.detail;

import com.hngy.siae.content.dto.response.ContentDetailVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileVO implements ContentDetailVO {
    private Long id;
    private Long contentId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Integer downloadCount;
    private Date createTime;
    private Date updateTime;
}