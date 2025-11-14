package com.hngy.siae.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.PositionCreateDTO;
import com.hngy.siae.user.dto.response.PositionVO;
import com.hngy.siae.user.entity.Position;
import com.hngy.siae.user.mapper.PositionMapper;
import com.hngy.siae.user.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 职位服务实现类
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class PositionServiceImpl 
    extends ServiceImpl<PositionMapper, Position>
        implements PositionService {

    /**
     * 创建职位
     *
     * @param createDTO 职位创建参数
     * @return 创建成功的职位信息
     */
    @Override
    public PositionVO createPosition(PositionCreateDTO createDTO) {
        // 检查职位名称是否已存在
        boolean exists = lambdaQuery()
                .eq(Position::getName, createDTO.getName())
                .exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.POSITION_ALREADY_EXISTS);

        // 创建职位
        Position position = BeanConvertUtil.to(createDTO, Position.class);
        save(position);

        return BeanConvertUtil.to(position, PositionVO.class);
    }

    /**
     * 更新职位
     *
     * @param id 职位ID
     * @param name 职位名称
     * @return 更新后的职位信息
     */
    @Override
    public PositionVO updatePosition(Long id, String name) {
        // 检查职位是否存在
        Position position = getById(id);
        AssertUtils.notNull(position, UserResultCodeEnum.POSITION_NOT_FOUND);

        // 如果更新名称，检查是否与其他职位冲突
        if (StrUtil.isNotBlank(name) && !name.equals(position.getName())) {
            boolean exists = lambdaQuery()
                    .eq(Position::getName, name)
                    .ne(Position::getId, id)
                    .exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.POSITION_ALREADY_EXISTS);
            position.setName(name);
        }

        updateById(position);
        return BeanConvertUtil.to(position, PositionVO.class);
    }

    /**
     * 根据ID查询职位
     *
     * @param id 职位ID
     * @return 职位信息
     */
    @Override
    public PositionVO getPositionById(Long id) {
        Position position = getById(id);
        AssertUtils.notNull(position, UserResultCodeEnum.POSITION_NOT_FOUND);
        return BeanConvertUtil.to(position, PositionVO.class);
    }

    /**
     * 查询所有职位
     *
     * @return 职位列表
     */
    @Override
    public List<PositionVO> listAllPositions() {
        List<Position> positions = list();
        return BeanConvertUtil.toList(positions, PositionVO.class);
    }

    /**
     * 删除职位
     *
     * @param id 职位ID
     * @return 删除结果
     */
    @Override
    public Boolean deletePosition(Long id) {
        // 检查职位是否存在
        Position position = getById(id);
        AssertUtils.notNull(position, UserResultCodeEnum.POSITION_NOT_FOUND);

        return removeById(id);
    }
}
