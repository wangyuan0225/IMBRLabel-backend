package com.wy0225.imbrlabel.controller;

import com.wy0225.imbrlabel.context.BaseContext;
import com.wy0225.imbrlabel.pojo.Result;
import com.wy0225.imbrlabel.pojo.DTO.ImageDTO;
import com.wy0225.imbrlabel.pojo.VO.ImageVO;
import com.wy0225.imbrlabel.service.ImageService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * @author wangy
 */
@RestController
@RequestMapping("/images")
public class ImageController {
    @Resource
    private ImageService imageService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 上传文件
     * @param file 文件
     * @return 上传结果
     */
    @PostMapping
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("File can not be null");
        }
        try {
            Long userId = BaseContext.getCurrentId();

            //创建用户专属文件夹（文件夹名就是UserId） 路径变成user/image
            Path userDir = Paths.get(uploadDir, String.valueOf(userId)).toAbsolutePath().normalize();
            Files.createDirectories(userDir);

            // 获取文件名（去掉路径前缀）
            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String fileName = new File(originalFilename).getName();
            Path targetLocation = userDir.resolve(fileName);
            // 重名文件检测
            if (Files.exists(targetLocation)) {
                return Result.error("A file with the same name already exists");
            }
            // 图像格式支持
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("The file type can only be an image");
            }
            Files.copy(file.getInputStream(), targetLocation);
            // 获取相对路径
            String relativePath = userDir.relativize(targetLocation).toString().replace("\\", "/");
            // 创建并保存 ImageDTO 对象
            ImageDTO imageDTO = getImageDTO(file, contentType, relativePath);
            imageService.upload(imageDTO);
            return Result.success("File uploads successfully: " + relativePath);
        } catch (IOException ex) {
            return Result.error("File uploads failed: " + ex.getMessage());
        }
    }

    /**
     * 获取 ImageDTO 对象
     * @param file 文件
     * @param contentType 文件类型
     * @param relativePath 相对路径
     * @return ImageDTO 对象
     */
    private static ImageDTO getImageDTO(MultipartFile file, String contentType, String relativePath) {
        ImageDTO imageDTO = new ImageDTO();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String nameWithoutExtension = originalFilename.contains(".") ? originalFilename.substring(0, originalFilename.lastIndexOf('.')) : originalFilename;
            imageDTO.setName(nameWithoutExtension);
        }
        imageDTO.setType(contentType.substring("image/".length()));
        imageDTO.setPath(relativePath);
        return imageDTO;
    }

    /**
     * 获取图片列表
     * @return 图片列表
     */
    @GetMapping
    public Result<?> list() {
        Long userId = BaseContext.getCurrentId();
        //新增参数userId
        List<ImageVO> images = imageService.list(userId);
        return Result.success(images);
    }

    /**
     * 删除图片
     * @param id 图片 ID
     * @return 删除结果
     */
    @DeleteMapping
    public Result<?> delete(@RequestParam Long id) {
        Long userId = BaseContext.getCurrentId();
        // 从本地删除，这里加了参数userId
        String filePath = imageService.getImageById(id, userId).getPath();
        try {
            // 从本地删除文件
            Path path = Paths.get(uploadDir).resolve(filePath).toAbsolutePath().normalize();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            return Result.error("File delete failed: " + e.getMessage());
        }
        // 从数据库删除记录
        imageService.delete(id,userId);
        return Result.success();
    }
}
