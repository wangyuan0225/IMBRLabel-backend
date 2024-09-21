package com.wy0225.imbrlabel.mapper;

import com.wy0225.imbrlabel.pojo.DO.AnnotationDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author wangy
 */
@Mapper
public interface AnnotationMapper {
    /**
     * 添加标注模板
     * @param annotationDO 标注模板数据对象
     */
    @Insert("insert into annotation(name, label, stroke_style, fill_style, line_width, create_time, update_time) " +
            "values(#{name}, #{label}, #{strokeStyle}, #{fillStyle}, #{lineWidth}, #{createTime}, #{updateTime})")
    void insert(AnnotationDO annotationDO);

    /**
     * 获取标注模板列表
     * @return 标注模板列表
     */
    @Select("select id, name, label, stroke_style, fill_style, line_width, create_time, update_time from annotation")
    List<AnnotationDO> list();

    /**
     * 为当前图像保存标注
     * @param imageId 图像ID
     * @param annotations 标注信息
     */
    @Update("update image set annotations = #{annotations}, update_time = NOW() " +
            "where id = #{imageId}")
    void updateAnnotation(Long imageId, String annotations);

    /**
     * 获取图像标注信息
     * @param imageId 图像ID
     * @return 图像标注信息
     */
    @Select("select annotations from image where id = #{imageId}")
    String getAnnotations(Long imageId);

    /**
     * 保存标注信息到图像
     * @param annotationDO 标注模板数据对象
     */
    @Update("update annotation set name = #{name}, label = #{label}, stroke_style = #{strokeStyle}, " +
            "fill_style = #{fillStyle}, line_width = #{lineWidth}, update_time = NOW() " +
            "where id = #{id}")
    void update(AnnotationDO annotationDO);

    /**
     * 删除标注模板
     * @param id 标注模板ID
     */
    @Delete("delete from annotation where id = #{id}")
    void delete(Long id);
}
