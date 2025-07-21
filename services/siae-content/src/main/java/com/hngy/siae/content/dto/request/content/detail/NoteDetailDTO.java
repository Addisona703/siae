package com.hngy.siae.content.dto.request.content.detail;

import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteDetailDTO implements ContentDetailDTO {
    private String content;
    private String format;
}
