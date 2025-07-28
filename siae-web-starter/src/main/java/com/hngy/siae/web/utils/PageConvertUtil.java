package com.hngy.siae.web.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.utils.BeanConvertUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分页转换工具类
 * 提供 MyBatis Plus 分页对象与通用分页对象的转换
 * 
 * @author SIAE开发团队
 */
public class PageConvertUtil {

    /**
     * 将 PageDTO 转换为 MyBatis-Plus 的 Page 对象
     *
     * @param pageDTO 分页请求参数
     * @param <T>     数据类型
     * @return MyBatis-Plus Page 对象
     */
    public static <T> Page<T> toPage(PageDTO<?> pageDTO) {
        return new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
    }

    
    /**
     * 将 MyBatis-Plus 的分页对象和记录列表构建为 PageVO
     *
     * @param page    MyBatis-Plus 分页对象
     * @param records 记录列表
     * @param <T>     数据类型
     * @return PageVO<T>
     */
    public static <T> PageVO<T> build(IPage<?> page, List<T> records) {
        PageVO<T> result = new PageVO<>();
        result.setTotal(page.getTotal());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setRecords(records != null ? records : List.of());
        return result;
    }

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
     * @param page    MyBatis-Plus 原始分页对象（实体类）
     * @param voClass VO 类型的 Class 对象
     * @param <E>     Entity 类型
     * @param <V>     VO 类型
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

    /**
     * 将 MyBatis-Plus 的 IPage<E> 使用自定义转换函数转换为 PageVO<V>
     *
     * @param page      MyBatis-Plus 原始分页对象（实体类）
     * @param converter 自定义转换函数
     * @param <E>       Entity 类型
     * @param <V>       VO 类型
     * @return PageVO<V>
     */
    public static <E, V> PageVO<V> convert(IPage<E> page, java.util.function.Function<E, V> converter) {
        List<V> converted = page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());

        // 构造一个新的 Page<V> 作为中转
        Page<V> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(converted);

        // 调用已有的通用分页转换方法
        return convert(voPage);
    }

    /**
     * 创建空的分页结果
     *
     * @param pageDTO 分页请求参数
     * @param <T>     数据类型
     * @return 空的 PageVO
     */
    public static <T> PageVO<T> empty(PageDTO<?> pageDTO) {
        PageVO<T> result = new PageVO<>();
        result.setTotal(0L);
        result.setPageNum(pageDTO.getPageNum());
        result.setPageSize(pageDTO.getPageSize());
        result.setRecords(List.of());
        return result;
    }

    /**
     * 创建空的分页结果
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @param <T>      数据类型
     * @return 空的 PageVO
     */
    public static <T> PageVO<T> empty(int pageNum, int pageSize) {
        PageVO<T> result = new PageVO<>();
        result.setTotal(0L);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setRecords(List.of());
        return result;
    }
}
