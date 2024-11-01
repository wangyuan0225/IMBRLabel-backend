package com.wy0225.imbrlabel.service.impl;

import com.wy0225.imbrlabel.context.BaseContext;
import com.wy0225.imbrlabel.mapper.ImageMapper;
import com.wy0225.imbrlabel.pojo.DO.ImageDO;
import com.wy0225.imbrlabel.pojo.DTO.ImageDTO;
import com.wy0225.imbrlabel.pojo.VO.ImageVO;
import com.wy0225.imbrlabel.service.ImageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangy
 */
@Service
public class ImageServiceImpl implements ImageService {
    @Resource
    private ImageMapper imageMapper;

    @Override
    public void upload(ImageDTO imageDTO) {
        ImageDO image = new ImageDO();
        image.setName(imageDTO.getName());
        image.setType(imageDTO.getType());
        image.setPath(imageDTO.getPath());
        image.setCreateTime(LocalDateTime.now());
        image.setUpdateTime(LocalDateTime.now());
        image.setUserId(BaseContext.getCurrentId());
        imageMapper.insert(image);
    }

    @Override
    public List<ImageVO> list(Long userId) {
        List<ImageDO> images = imageMapper.list(userId);
        List<ImageVO> imageVos = new ArrayList<>();
        for (ImageDO image : images) {
            ImageVO imageVo = new ImageVO();
            imageVo.setId(image.getId());
            imageVo.setName(image.getName());
            imageVo.setType(image.getType());
            imageVo.setPath(image.getUserId() + "/" + image.getPath());
            imageVo.setAnnotations(image.getAnnotations());
            imageVos.add(imageVo);
        }
        return imageVos;
    }

    @Override
    public void delete(Long id, Long userId) {
        imageMapper.deleteById(id, userId);
    }

    @Override
    public ImageDTO getImageById(Long id, Long userId) {
        ImageDO image = imageMapper.selectById(id, userId);
        ImageDTO imageDTO = new ImageDTO();
        imageDTO.setName(image.getName());
        imageDTO.setType(image.getType());
        imageDTO.setPath(userId + "/" + image.getPath());
        imageDTO.setAnnotations(image.getAnnotations());
        return imageDTO;
    }

    @Override
    public Long getPreviousImageId(Long currentImageId,Long userId) {
        return imageMapper.getPreviousImageIdById(currentImageId, userId);
    }

    @Override
    public Long getNextImageId(Long currentImageId,Long userId) {
        return imageMapper.getNextImageIdById(currentImageId, userId);
    }
}
