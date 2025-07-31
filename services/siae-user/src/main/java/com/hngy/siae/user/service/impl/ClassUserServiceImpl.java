package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.ClassUserCreateDTO;
import com.hngy.siae.user.dto.request.ClassUserQueryDTO;
import com.hngy.siae.user.dto.request.ClassUserUpdateDTO;
import com.hngy.siae.user.dto.response.ClassUserVO;
import com.hngy.siae.user.entity.ClassUser;
import com.hngy.siae.user.entity.User;
import com.hngy.siae.user.mapper.ClassUserMapper;
import com.hngy.siae.user.mapper.UserMapper;
import com.hngy.siae.user.service.ClassInfoService;
import com.hngy.siae.user.service.ClassUserService;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 班级用户关联服务实现类
 * <p>
 * 提供班级用户关联的增删改查功能，包括添加用户到班级、更新关联信息、查询和删除关联关系。
 * 支持分页查询和条件查询，支持按班级、成员类型、用户状态等条件筛选。
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class ClassUserServiceImpl 
        extends ServiceImpl<ClassUserMapper, ClassUser> 
        implements ClassUserService {

    private final UserMapper userMapper;
    private final ClassInfoService classInfoService;

    /**
     * 添加用户到班级
     *
     * @param classUserCreateDTO 班级用户关联创建参数
     * @return 创建成功的班级用户关联信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassUserVO addUserToClass(ClassUserCreateDTO classUserCreateDTO) {
        // 检查班级是否存在
        classInfoService.getClassById(classUserCreateDTO.getClassId());
        
        // 检查用户是否存在
        assertUserExists(classUserCreateDTO.getUserId());
        
        // 检查用户是否已经在该班级中
        boolean exists = lambdaQuery()
                .eq(ClassUser::getClassId, classUserCreateDTO.getClassId())
                .eq(ClassUser::getUserId, classUserCreateDTO.getUserId())
                .eq(ClassUser::getIsDeleted, 0)
                .exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.USER_ALREADY_IN_CLASS);

        // 创建班级用户关联
        ClassUser classUser = BeanConvertUtil.to(classUserCreateDTO, ClassUser.class);
        classUser.setIsDeleted(0);
        save(classUser);

        return buildClassUserVO(classUser);
    }

    public void assertUserExists(Long userId) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getId, userId)
        );
        AssertUtils.isTrue(count != null && count > 0, UserResultCodeEnum.USER_NOT_FOUND);
    }

    /**
     * 更新用户班级关联信息
     *
     * @param classUserUpdateDTO 班级用户关联更新参数
     * @return 更新后的班级用户关联信息
     */
    @Override
    public ClassUserVO updateClassUser(ClassUserUpdateDTO classUserUpdateDTO) {
        // 检查关联记录是否存在
        ClassUser classUser = getById(classUserUpdateDTO.getId());
        AssertUtils.notNull(classUser, UserResultCodeEnum.CLASS_USER_NOT_FOUND);

        // 更新关联信息
        BeanConvertUtil.to(classUserUpdateDTO, classUser, "id", "classId", "userId", "isDeleted");
        updateById(classUser);

        return buildClassUserVO(classUser);
    }

    /**
     * 根据ID获取班级用户关联信息
     *
     * @param id 关联记录ID
     * @return 班级用户关联详细信息
     */
    @Override
    public ClassUserVO getClassUserById(Long id) {
        ClassUser classUser = getById(id);
        AssertUtils.notNull(classUser, UserResultCodeEnum.CLASS_USER_NOT_FOUND);
        
        return buildClassUserVO(classUser);
    }

    /**
     * 分页查询班级用户关联列表
     *
     * @param pageDTO 分页查询参数，包含分页信息和查询条件
     * @return 分页班级用户关联列表
     */
    @Override
    public PageVO<ClassUserVO> listClassUsersByPage(PageDTO<ClassUserQueryDTO> pageDTO) {
        ClassUserQueryDTO queryDTO = pageDTO.getParams();
        LambdaQueryWrapper<ClassUser> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO != null) {
            wrapper.eq(queryDTO.getId() != null, ClassUser::getId, queryDTO.getId())
                    .eq(queryDTO.getClassId() != null, ClassUser::getClassId, queryDTO.getClassId())
                    .eq(queryDTO.getUserId() != null, ClassUser::getUserId, queryDTO.getUserId())
                    .eq(queryDTO.getMemberType() != null, ClassUser::getMemberType, queryDTO.getMemberType())
                    .eq(queryDTO.getStatus() != null, ClassUser::getStatus, queryDTO.getStatus());
        }

        wrapper.eq(ClassUser::getIsDeleted, 0)
                .orderByDesc(ClassUser::getCreatedAt);

        Page<ClassUser> page = PageConvertUtil.toPage(pageDTO);
        Page<ClassUser> resultPage = page(page, wrapper);

        // 先进行基本转换
        PageVO<ClassUserVO> pageVO = PageConvertUtil.convert(resultPage, ClassUserVO.class);

        // 对转换结果进行后处理，设置名称显示
        pageVO.getRecords().forEach(record -> {
            setMemberTypeName(record);
            setStatusName(record);
        });

        return pageVO;
    }

    /**
     * 根据班级ID获取用户列表
     *
     * @param classId 班级ID
     * @return 班级下的用户列表
     */
    @Override
    public List<ClassUserVO> listUsersByClassId(Long classId) {
        // 检查班级是否存在
        classInfoService.getClassById(classId);

        List<ClassUser> classUsers = lambdaQuery()
                .eq(ClassUser::getClassId, classId)
                .eq(ClassUser::getIsDeleted, 0)
                .orderByAsc(ClassUser::getCreatedAt)
                .list();

        return classUsers.stream()
                .map(this::buildClassUserVO)
                .toList();
    }

    /**
     * 从班级移除用户
     *
     * @param classId 班级ID
     * @param userId 用户ID
     * @return 移除结果，true表示移除成功，false表示移除失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUserFromClass(Long classId, Long userId) {
        // 检查班级是否存在
        classInfoService.getClassById(classId);
        
        // 检查用户是否存在
        assertUserExists(userId);

        // 查找关联记录
        ClassUser classUser = lambdaQuery()
                .eq(ClassUser::getClassId, classId)
                .eq(ClassUser::getUserId, userId)
                .eq(ClassUser::getIsDeleted, 0)
                .one();
        AssertUtils.notNull(classUser, UserResultCodeEnum.CLASS_USER_NOT_FOUND);

        // 逻辑删除
        classUser.setIsDeleted(1);
        return updateById(classUser);
    }

    /**
     * 根据ID删除班级用户关联（逻辑删除）
     *
     * @param id 关联记录ID
     * @return 删除结果，true表示删除成功，false表示删除失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteClassUser(Long id) {
        ClassUser classUser = getById(id);
        AssertUtils.notNull(classUser, UserResultCodeEnum.CLASS_USER_NOT_FOUND);

        classUser.setIsDeleted(1);
        return updateById(classUser);
    }

    /**
     * 构建班级用户关联视图对象
     *
     * @param classUser 班级用户关联实体
     * @return 班级用户关联视图对象
     */
    private ClassUserVO buildClassUserVO(ClassUser classUser) {
        ClassUserVO vo = BeanConvertUtil.to(classUser, ClassUserVO.class);
        setMemberTypeName(vo);
        setStatusName(vo);
        return vo;
    }

    /**
     * 设置成员类型名称
     *
     * @param vo 班级用户关联视图对象
     */
    private void setMemberTypeName(ClassUserVO vo) {
        if (vo.getMemberType() != null) {
            switch (vo.getMemberType()) {
                case 0 -> vo.setMemberTypeName("非协会成员");
                case 1 -> vo.setMemberTypeName("预备成员");
                case 2 -> vo.setMemberTypeName("正式成员");
                default -> vo.setMemberTypeName("未知");
            }
        }
    }

    /**
     * 设置状态名称
     *
     * @param vo 班级用户关联视图对象
     */
    private void setStatusName(ClassUserVO vo) {
        if (vo.getStatus() != null) {
            switch (vo.getStatus()) {
                case 1 -> vo.setStatusName("在读");
                case 2 -> vo.setStatusName("转班");
                case 3 -> vo.setStatusName("毕业");
                default -> vo.setStatusName("未知");
            }
        }
    }
}
