package com.lvpb.miaosha.base;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BaseMapper<T>
{
    /** 插入实体 */
    int insert(T t);

    /** 插入数据部分值 */
    int insertSelective(T t);

    /** 批插入 */
    int insertBatch(List<T> list);

    /** 更新一条主键，全量更新 */
    int updateByPrimaryKey(T t);

    /** 更新数据 部分更新 */
    int updateByPrimaryKeySelective(T t);

    /** 批量更新 */
    int updateBatch(List<T> list);

    /** 根据id删除一条记录 */
    int deleteByPrimaryKey(Object id);

    /** 根据id批量删除 */
    int deleteBatch(@Param("id") Object... id);

    /** 查询所有记录数目 */
    int selectCount();

    /** 查询满足条件的数目 */
    int selectCountByCon(Map<String,Object> map);

    /** 返回所有数据 */
    List<T> selectAll();

    /** 查询满足条件得数据 */
    List<T> selectListByCon(Map<String,Object> map);

    /** 根据id 查询具体的数据 */
    T selectByPrimaryKey(Object id);

    /** 根据ids查询数据 */
    List<T> selectByIds(@Param("list") List<Object> list);
}
