package com.hngy.siae.user.service.impl;

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
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.entity.UserAward;
import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.request.BatchUrlDTO;
import com.hngy.siae.api.media.dto.response.BatchUrlVO;
import com.hngy.siae.user.mapper.UserAwardMapper;
import com.hngy.siae.user.service.AwardLevelService;
import com.hngy.siae.user.service.AwardTypeService;
import com.hngy.siae.user.service.UserAwardService;
import com.hngy.siae.user.service.UserService;
import com.hngy.siae.core.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户获奖记录服务实现类
 * <p>
 * 提供用户获奖记录的增删改查功能，包括创建、更新、查询和删除用户获奖记录信息。
 * 支持分页查询和条件查询，支持按用户ID查询获奖记录列表。
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAwardServiceImpl 
        extends ServiceImpl<UserAwardMapper, UserAward> 
        implements UserAwardService {
    
    private final UserService userService;
    private final AwardTypeService awardTypeService;
    private final AwardLevelService awardLevelService;
    private final MediaFeignClient mediaFeignClient;

    /**
     * 创建用户获奖记录
     *
     * @param userAwardCreateDTO 用户获奖记录创建参数
     * @return 创建成功的用户获奖记录信息
     */
    @Override
    public UserAwardVO createUserAward(UserAwardCreateDTO userAwardCreateDTO) {
        // 批量检查团队成员是否存在
        userService.assertUsersExist(userAwardCreateDTO.getTeamMembers());

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

    /**
     * 更新用户获奖记录信息
     *
     * @param userAwardUpdateDTO 用户获奖记录更新参数
     * @return 更新后的用户获奖记录信息
     */
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

    /**
     * 根据ID获取用户获奖记录信息
     *
     * @param id 获奖记录ID
     * @return 用户获奖记录详细信息
     */
    @Override
    public UserAwardVO getUserAwardById(Long id) {
        UserAward userAward = getById(id);
        AssertUtils.notNull(userAward, UserResultCodeEnum.AWARD_NOT_FOUND);
        
        UserAwardVO vo = BeanConvertUtil.to(userAward, UserAwardVO.class);
        
        // 收集需要获取URL的文件ID
        List<String> fileIdsToFetch = new java.util.ArrayList<>();
        
        // 证书文件ID
        if (StrUtil.isNotBlank(vo.getCertificateFileId())) {
            fileIdsToFetch.add(vo.getCertificateFileId());
        }
        
        // 填充团队成员信息
        if (StrUtil.isNotBlank(vo.getTeamMembers())) {
            try {
                // 解析团队成员ID列表
                List<Long> memberIds = cn.hutool.json.JSONUtil.parseArray(vo.getTeamMembers())
                        .stream()
                        .map(obj -> Long.valueOf(obj.toString()))
                        .collect(Collectors.toList());
                
                if (!memberIds.isEmpty()) {
                    // 批量查询用户信息
                    List<UserVO> members = baseMapper.selectUsersByIds(memberIds);
                    
                    // 收集头像文件ID
                    members.stream()
                            .map(UserVO::getAvatarFileId)
                            .filter(StrUtil::isNotBlank)
                            .distinct()
                            .forEach(fileIdsToFetch::add);
                    
                    // 批量获取文件URL
                    Map<String, String> urlMap = new java.util.HashMap<>();
                    if (!fileIdsToFetch.isEmpty()) {
                        try {
                            BatchUrlDTO request = new BatchUrlDTO();
                            request.setFileIds(fileIdsToFetch);
                            request.setExpirySeconds(86400); // 24小时
                            BatchUrlVO urlResult = mediaFeignClient.batchGetFileUrls(request);
                            
                            if (urlResult != null && urlResult.getUrls() != null) {
                                urlMap = urlResult.getUrls();
                            }
                        } catch (Exception e) {
                            log.warn("批量获取文件URL失败: {}", e.getMessage());
                        }
                    }
                    
                    // 设置证书URL
                    if (StrUtil.isNotBlank(vo.getCertificateFileId())) {
                        vo.setCertificateUrl(urlMap.get(vo.getCertificateFileId()));
                    }
                    
                    // 设置头像URL
                    for (UserVO user : members) {
                        if (StrUtil.isNotBlank(user.getAvatarFileId())) {
                            user.setAvatarUrl(urlMap.get(user.getAvatarFileId()));
                        }
                    }
                    
                    // 按照原始ID顺序排列成员
                    List<UserVO> orderedMembers = new java.util.ArrayList<>();
                    Map<Long, UserVO> userMap = members.stream()
                            .collect(Collectors.toMap(UserVO::getId, user -> user));
                    for (Long memberId : memberIds) {
                        UserVO user = userMap.get(memberId);
                        if (user != null) {
                            orderedMembers.add(user);
                        }
                    }
                    
                    vo.setTeamMemberList(orderedMembers);
                }
            } catch (Exception e) {
                log.error("填充团队成员信息失败: {}", e.getMessage(), e);
                vo.setTeamMemberList(new java.util.ArrayList<>());
            }
        } else {
            vo.setTeamMemberList(new java.util.ArrayList<>());
            
            // 即使没有团队成员，也要获取证书URL
            if (StrUtil.isNotBlank(vo.getCertificateFileId())) {
                try {
                    BatchUrlDTO request = new BatchUrlDTO();
                    request.setFileIds(List.of(vo.getCertificateFileId()));
                    request.setExpirySeconds(86400);
                    BatchUrlVO urlResult = mediaFeignClient.batchGetFileUrls(request);
                    
                    if (urlResult != null && urlResult.getUrls() != null) {
                        vo.setCertificateUrl(urlResult.getUrls().get(vo.getCertificateFileId()));
                    }
                } catch (Exception e) {
                    log.warn("获取证书URL失败: {}", e.getMessage());
                }
            }
        }
        
        return vo;
    }

    /**
     * 根据用户ID分页获取用户获奖记录列表
     *
     * @param userId 用户ID
     * @param pageDTO 分页参数
     * @return 分页用户获奖记录列表
     */
    @Override
    public PageVO<UserAwardVO> pageUserAwardsByUserId(Long userId, PageDTO<Void> pageDTO) {
        userService.assertUserExists(userId);

        Page<UserAward> page = PageConvertUtil.toPage(pageDTO);
        Page<UserAward> resultPage = lambdaQuery()
                .apply("JSON_CONTAINS(team_members, CAST({0} AS JSON))", userId)
                .eq(UserAward::getIsDeleted, 0)
                .orderByDesc(UserAward::getAwardedAt)
                .page(page);

        PageVO<UserAwardVO> pageVO = PageConvertUtil.convert(resultPage, UserAwardVO.class);
        
        // 填充团队成员信息（已删除用户会被过滤）
        fillTeamMemberInfo(pageVO.getRecords());
        
        // 过滤掉没有有效团队成员的获奖记录
        List<UserAwardVO> filteredRecords = pageVO.getRecords().stream()
                .filter(award -> award.getTeamMemberList() != null && !award.getTeamMemberList().isEmpty())
                .collect(Collectors.toList());
        pageVO.setRecords(filteredRecords);
        
        return pageVO;
    }
    
    /**
     * 填充团队成员信息（包含头像URL）和证书URL
     *
     * @param awards 获奖记录列表
     */
    private void fillTeamMemberInfo(List<UserAwardVO> awards) {
        if (awards == null || awards.isEmpty()) {
            return;
        }
        
        // 1. 收集所有需要查询的用户ID
        List<Long> allUserIds = awards.stream()
                .filter(award -> StrUtil.isNotBlank(award.getTeamMembers()))
                .flatMap(award -> {
                    try {
                        return cn.hutool.json.JSONUtil.parseArray(award.getTeamMembers())
                                .stream()
                                .map(obj -> Long.valueOf(obj.toString()));
                    } catch (Exception e) {
                        return java.util.stream.Stream.empty();
                    }
                })
                .distinct()
                .collect(Collectors.toList());
        
        if (allUserIds.isEmpty()) {
            awards.forEach(award -> award.setTeamMemberList(new java.util.ArrayList<>()));
            return;
        }
        
        // 2. 批量查询用户信息
        List<UserVO> users = baseMapper.selectUsersByIds(allUserIds);
        Map<Long, UserVO> userMap = users.stream()
                .collect(Collectors.toMap(UserVO::getId, user -> user));
        
        // 3. 收集所有需要获取URL的文件ID（头像 + 证书）
        List<String> allFileIds = new java.util.ArrayList<>();
        
        // 头像文件ID
        users.stream()
                .map(UserVO::getAvatarFileId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .forEach(allFileIds::add);
        
        // 证书文件ID
        awards.stream()
                .map(UserAwardVO::getCertificateFileId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .forEach(allFileIds::add);
        
        // 4. 批量获取文件URL
        Map<String, String> urlMap = new java.util.HashMap<>();
        if (!allFileIds.isEmpty()) {
            try {
                BatchUrlDTO request = new BatchUrlDTO();
                request.setFileIds(allFileIds);
                request.setExpirySeconds(86400); // 24小时
                BatchUrlVO urlResult = mediaFeignClient.batchGetFileUrls(request);
                
                if (urlResult != null && urlResult.getUrls() != null) {
                    urlMap = urlResult.getUrls();
                }
            } catch (Exception e) {
                log.warn("批量获取文件URL失败: {}", e.getMessage());
            }
        }
        
        // 5. 设置头像URL
        for (UserVO user : users) {
            if (StrUtil.isNotBlank(user.getAvatarFileId())) {
                user.setAvatarUrl(urlMap.get(user.getAvatarFileId()));
            }
        }
        
        // 6. 组装团队成员信息和证书URL到每个奖项
        for (UserAwardVO award : awards) {
            // 设置证书URL
            if (StrUtil.isNotBlank(award.getCertificateFileId())) {
                award.setCertificateUrl(urlMap.get(award.getCertificateFileId()));
            }
            
            // 设置团队成员
            if (StrUtil.isNotBlank(award.getTeamMembers())) {
                try {
                    List<Long> memberIds = cn.hutool.json.JSONUtil.parseArray(award.getTeamMembers())
                            .stream()
                            .map(obj -> Long.valueOf(obj.toString()))
                            .collect(Collectors.toList());
                    
                    List<UserVO> members = new java.util.ArrayList<>();
                    for (Long memberId : memberIds) {
                        UserVO user = userMap.get(memberId);
                        if (user != null) {
                            members.add(user);
                        }
                    }
                    award.setTeamMemberList(members);
                } catch (Exception e) {
                    award.setTeamMemberList(new java.util.ArrayList<>());
                }
            } else {
                award.setTeamMemberList(new java.util.ArrayList<>());
            }
        }
    }

    /**
     * 分页查询用户获奖记录列表
     *
     * @param pageDTO 分页查询参数，包含分页信息和查询条件
     * @return 分页用户获奖记录列表
     */
    @Override
    public PageVO<UserAwardVO> listUserAwardsByPage(PageDTO<UserAwardQueryDTO> pageDTO) {
        // 1. 先分页查询奖项基本信息（包含等级和类型名称）
        Page<UserAwardVO> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<UserAwardVO> resultPage = baseMapper.selectUserAwardPageWithDetails(page, pageDTO.getParams());
        
        // 2. 填充团队成员信息（已删除用户会被过滤）
        fillTeamMemberInfo(resultPage.getRecords());
        
        // 3. 过滤掉没有有效团队成员的获奖记录
        List<UserAwardVO> filteredRecords = resultPage.getRecords().stream()
                .filter(award -> award.getTeamMemberList() != null && !award.getTeamMemberList().isEmpty())
                .collect(Collectors.toList());
        resultPage.setRecords(filteredRecords);
        
        return PageConvertUtil.convert(resultPage);
    }

    /**
     * 根据ID删除用户获奖记录
     *
     * @param id 获奖记录ID
     * @return 删除结果，true表示删除成功，false表示删除失败
     */
    @Override
    public boolean deleteUserAward(Long id) {
        // 检查获奖记录是否存在
        UserAward userAward = getById(id);
        AssertUtils.notNull(userAward, UserResultCodeEnum.AWARD_NOT_FOUND);
        userAward.setIsDeleted(1);
        return updateById(userAward);
    }
}
