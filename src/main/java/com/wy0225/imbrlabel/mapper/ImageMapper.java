package com.wy0225.imbrlabel.mapper;

import com.wy0225.imbrlabel.pojo.DO.ImageDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wangy
 */
@Mapper
public interface ImageMapper {
    /**
     * 添加图像
     * @param image 图像数据对象
     */
    @Insert("insert into image(name, type, path, create_time, update_time,user_id) " +
            "values(#{name}, #{type}, #{path}, #{createTime}, #{updateTime},#{userId})")
    void insert(ImageDO image);

    /**
     * 获取图像列表
     * @return 图像列表
     */
    @Select("select id, name, type, path, annotations, create_time, update_time from image where user_id = #{userId}")
    List<ImageDO> list(Long userId);

    /**
     * 获取图像信息
     * @param id 图像ID
     * @return 图像信息
     */
    @Select("select id, name, type, path, annotations, create_time, update_time from image where id = #{id} AND user_id = #{userId}")
    ImageDO selectById(Long id,Long userId);

    /**
     * 删除图像
     * @param id 图像ID
     */
    @Delete("delete from image where id = #{id} AND user_id = #{userId}")
    void deleteById(Long id,Long userId);
}
