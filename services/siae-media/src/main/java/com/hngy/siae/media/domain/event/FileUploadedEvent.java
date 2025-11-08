package com.hngy.siae.media.domain.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 文件上传完成事件
 *
 * @author SIAE Team
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileUploadedEvent extends FileEvent {

    public static final String EVENT_TYPE = "file.uploaded";

    public FileUploadedEvent(FileEvent event) {
        super(event.getEventId(), EVENT_TYPE, event.getFileId(), 
              event.getTenantId(), event.getFileInfo(), 
              event.getTimestamp(), event.getMetadata());
    }

}
