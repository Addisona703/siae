package com.hngy.siae.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.common.dto.response.PageVO;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 */
public class PageConvertUtil {

    /**
     * 将 MyBatis-Plus 的 IPage<T> 转换为通用的 PageVO<T>
     *
     * @param page MyBatis-Plus 原始分页对象
     * @param <T>  原始数据类型
     * @return PageVO<T>
     */
    public static <T> PageVO<T> convert(IPage<T> page) {
        PageVO<T> result = new PageVO<>();
        result.setTotal(page.getTotal());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setRecords(page.getRecords());
        return result;
    }

    /**
     * 将 MyBatis-Plus 的 IPage<E> 按目标类类型转换为 PageVO<V>
     *
     * @param page     MyBatis-Plus 原始分页对象（实体类）
     * @param voClass  VO 类型的 Class 对象
     * @param <E>      Entity 类型
     * @param <V>      VO 类型
     * @return PageVO<V>
     */
    public static <E, V> PageVO<V> convert(IPage<E> page, Class<V> voClass) {
        List<V> converted = page.getRecords().stream()
                .map(e -> BeanConvertUtil.to(e, voClass))
                .collect(Collectors.toList());

        // 构造一个新的 Page<V> 作为中转
        Page<V> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(converted);

        // 调用已有的通用分页转换方法
        return convert(voPage);
    }

}
