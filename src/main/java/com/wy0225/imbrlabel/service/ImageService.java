package com.wy0225.imbrlabel.service;

import com.wy0225.imbrlabel.pojo.DTO.ImageDTO;
import com.wy0225.imbrlabel.pojo.VO.ImageVO;

import java.util.List;

/**
 * @author wangy
 */
public interface ImageService {
    /**
     * 上传图片
     * @param imageDTO 图片信息
     */
    void upload(ImageDTO imageDTO);

    /**
     * 获取图片列表
     * @return 图片列表
     */
    List<ImageVO> list(Long userId);

    /**
     * 删除图片
     * @param id 图片id
     */
    void delete(Long id,Long userId);

    /**
     * 获取图片信息
     * @param id 图片id
     * @return 图片信息
     */
    ImageDTO getImageById(Long id,Long userId);
}
