package com.hngy.siae.content.strategy;


import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;

import java.util.List;

public interface ContentStrategy {

    ContentDetailVO insert(Long contentId, ContentDetailDTO dto);

    ContentDetailVO update(Long contentId, ContentDetailDTO dto);

    void delete(Long contentId);

    boolean batchDelete(List<Long> contentIds);

    ContentDetailVO getDetail(Long contentId);
}

