package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.UserAwardCreateDTO;
import com.hngy.siae.user.dto.request.UserAwardQueryDTO;
import com.hngy.siae.user.dto.request.UserAwardUpdateDTO;
import com.hngy.siae.user.dto.response.UserAwardVO;
import com.hngy.siae.user.entity.UserAward;
import com.hngy.siae.user.mapper.UserAwardMapper;
import com.hngy.siae.user.service.AwardLevelService;
import com.hngy.siae.user.service.AwardTypeService;
import com.hngy.siae.user.service.UserAwardService;
import com.hngy.siae.user.service.UserService;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户获奖记录服务实现类
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class UserAwardServiceImpl 
        extends ServiceImpl<UserAwardMapper, UserAward> 
        implements UserAwardService {
    
    private final UserService userService;
    private final AwardTypeService awardTypeService;
    private final AwardLevelService awardLevelService;

    @Override
    public UserAwardVO createUserAward(UserAwardCreateDTO userAwardCreateDTO) {
        // 检查用户是否存在
        userService.assertUserExists(userAwardCreateDTO.getUserId());
        
        // 检查奖项类型是否存在
        AssertUtils.notNull(awardTypeService.getAwardTypeById(userAwardCreateDTO.getAwardTypeId()),
                UserResultCodeEnum.AWARD_TYPE_NOT_FOUND);
        
        // 检查奖项级别是否存在
        AssertUtils.notNull(awardLevelService.getAwardLevelById(userAwardCreateDTO.getAwardLevelId()),
                UserResultCodeEnum.AWARD_LEVEL_NOT_FOUND);

        // 构建获奖记录实体，设置默认值
        UserAward userAward = BeanConvertUtil.to(userAwardCreateDTO, UserAward.class);
        userAward.setIsDeleted(0);
        save(userAward); 

        return BeanConvertUtil.to(userAward, UserAwardVO.class);
    }

    @Override
    public UserAwardVO updateUserAward(UserAwardUpdateDTO userAwardUpdateDTO) {
        // 检查获奖记录是否存在
        UserAward userAward = getById(userAwardUpdateDTO.getId());
        AssertUtils.notNull(userAward, UserResultCodeEnum.AWARD_NOT_FOUND);
        
        // 如果更新了奖项类型ID，检查奖项类型是否存在
        if (userAwardUpdateDTO.getAwardTypeId() != null && !userAwardUpdateDTO.getAwardTypeId().equals(userAward.getAwardTypeId())) {
            AssertUtils.notNull(awardTypeService.getAwardTypeById(userAwardUpdateDTO.getAwardTypeId()),
                    UserResultCodeEnum.AWARD_TYPE_NOT_FOUND);
        }
        
        // 如果更新了奖项级别ID，检查奖项级别是否存在
        if (userAwardUpdateDTO.getAwardLevelId() != null && !userAwardUpdateDTO.getAwardLevelId().equals(userAward.getAwardLevelId())) {
            AssertUtils.notNull(awardLevelService.getAwardLevelById(userAwardUpdateDTO.getAwardLevelId()),
                    UserResultCodeEnum.AWARD_LEVEL_NOT_FOUND);
        }
        
        // 更新获奖记录信息
        BeanConvertUtil.to(userAwardUpdateDTO, userAward, "id");
        updateById(userAward); 

        return BeanConvertUtil.to(userAward, UserAwardVO.class);
    }

    @Override
    public UserAwardVO getUserAwardById(Long id) {
        UserAward userAward = getById(id); 
        AssertUtils.notNull(userAward, UserResultCodeEnum.AWARD_NOT_FOUND);
        return BeanConvertUtil.to(userAward, UserAwardVO.class);
    }

    @Override
    public List<UserAwardVO> listUserAwardsByUserId(Long userId) {
        // 检查用户是否存在
        userService.assertUserExists(userId);
        
        // 查询用户的获奖记录
        List<UserAward> userAwards = lambdaQuery()
            .eq(UserAward::getUserId, userId)
            .eq(UserAward::getIsDeleted, 0)
            .orderByDesc(UserAward::getAwardedAt)
            .list();

        return BeanConvertUtil.toList(userAwards, UserAwardVO.class);
    }

    @Override
    public PageVO<UserAwardVO> listUserAwardsByPage(PageDTO<UserAwardQueryDTO> pageDTO) {
        UserAwardQueryDTO dto = pageDTO.getParams();
        QueryWrapper<UserAward> wrapper = new QueryWrapper<>();

        if (dto != null) {
            wrapper.eq(dto.getUserId() != null, "user_id", dto.getUserId())
                    .eq(dto.getAwardTypeId() != null, "award_type_id", dto.getAwardTypeId())
                    .eq(dto.getAwardLevelId() != null, "award_level_id", dto.getAwardLevelId())
                    .like(StringUtils.hasText(dto.getAwardTitle()), "award_title", dto.getAwardTitle())
                    .like(StringUtils.hasText(dto.getAwardedBy()), "awarded_by", dto.getAwardedBy())
                    .ge(dto.getAwardDateStart() != null, "awarded_at", dto.getAwardDateStart())
                    .le(dto.getAwardDateEnd() != null, "awarded_at", dto.getAwardDateEnd());
        }

        wrapper.eq("is_deleted", 0);

        String sortField = (dto != null && StringUtils.hasText(dto.getOrderByField()))
                ? getColumnByField(dto.getOrderByField())
                : "awarded_at";

        String sortDirection = (dto != null && StringUtils.hasText(dto.getOrderDirection()))
                ? dto.getOrderDirection().toLowerCase()
                : "desc";

        if ("asc".equals(sortDirection)) {
            wrapper.orderByAsc(sortField);
        } else {
            wrapper.orderByDesc(sortField);
        }

        Page<UserAward> page = PageConvertUtil.toPage(pageDTO);
        Page<UserAward> resultPage = page(page, wrapper);

        return PageConvertUtil.convert(resultPage, UserAwardVO.class);
    }

    /**
     * 根据前端排序字段返回数据库对应列名，默认返回 "awarded_at"
     */
    private String getColumnByField(String fieldName) {
        return switch (fieldName) {
            case "awardTitle" -> "award_title";
            case "awardLevelId" -> "award_level_id";
            case "awardTypeId" -> "award_type_id";
            case "score" -> "score";
            case "rank" -> "rank";
            default -> "awarded_at";
        };
    }

    @Override
    public boolean deleteUserAward(Long id) {
        // 检查获奖记录是否存在
        UserAward userAward = getById(id); 
        AssertUtils.notNull(userAward, UserResultCodeEnum.AWARD_NOT_FOUND);
        userAward.setIsDeleted(1);
        return updateById(userAward); 
    }
}
