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

    /**
     * 根据用户ID和当前图像ID获取上一张图像的ID
     * @param userId 用户ID
     * @param currentId 当前图像的ID
     * @return 上一张图像的ID
     */
    Long getPreviousImageId(Long userId, Long currentId);

    /**
     * 根据用户ID和当前图像ID获取下一张图像的ID
     * @param userId 用户ID
     * @param currentId 当前图像的ID
     * @return 下一张图像的ID
     */
    Long getNextImageId(Long userId, Long currentId);
}
