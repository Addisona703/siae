package com.hngy.siae.content.dto.request;

import com.hngy.siae.content.common.enums.ActionTypeEnum;
import com.hngy.siae.content.common.enums.TypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long targetId;
    @NotNull
    private TypeEnum targetType;
    @NotNull
    private ActionTypeEnum actionType;
}
