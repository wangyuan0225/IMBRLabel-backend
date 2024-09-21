package com.wy0225.imbrlabel.service;

import com.wy0225.imbrlabel.pojo.DTO.AnnotationDTO;
import com.wy0225.imbrlabel.pojo.VO.AnnotationVO;

import java.util.List;

/**
 * @author wangy
 */
public interface AnnotationService {
    /**
     * 添加标注模板
     * @param annotationDTO 标注属性信息
     */
    void addAnnotation(AnnotationDTO annotationDTO);

    /**
     * 获取标注模板列表
     * @return 标注模板列表
     */
    List<AnnotationVO> list();

    /**
     * 为图片添加标注
     * @param imageId 图片id
     * @param annotations 标注信息
     */
    void addAnnotationToImage(Long imageId, String annotations);

    /**
     * 导出csv
     * @param imageId 图片id
     * @return csv字符串
     */
    String exportCsv(Long imageId);

    /**
     * 导出json
     * @param imageId 图片id
     * @return json字符串
     */
    String exportJson(Long imageId);

    /**
     * 导出xml
     * @param imageId 图片id
     * @return xml字符串
     */
    String exportXml(Long imageId);

    /**
     * 更新标注模板
     * @param annotationVO 标注属性信息
     */
    void updateAnnotation(AnnotationVO annotationVO);

    /**
     * 删除标注模板
     * @param id 标注id
     */
    void deleteAnnotation(Long id);
}
