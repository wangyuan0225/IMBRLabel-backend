package com.wy0225.imbrlabel.controller;

import com.wy0225.imbrlabel.pojo.Result;
import com.wy0225.imbrlabel.pojo.DTO.ImageDTO;
import com.wy0225.imbrlabel.pojo.VO.ImageVO;
import com.wy0225.imbrlabel.service.ImageService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            return Result.error("文件不能为空");
        }
        try {
            Path fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(fileStorageLocation);
            Path targetLocation = fileStorageLocation.resolve(Objects.requireNonNull(file.getOriginalFilename()));
            // 重名文件检测
            if (Files.exists(targetLocation)) {
                return Result.error("同名文件已存在");
            }
            // 图像格式支持
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("文件类型只能为图片");
            }
            Files.copy(file.getInputStream(), targetLocation);
            // 获取相对路径
            String relativePath = fileStorageLocation.relativize(targetLocation).toString().replace("\\", "/");
            // 创建并保存 ImageDTO 对象
            ImageDTO imageDTO = getImageDTO(file, contentType, relativePath);
            imageService.upload(imageDTO);
            return Result.success("文件上传成功: " + relativePath);
        } catch (IOException ex) {
            return Result.error("文件上传失败: " + ex.getMessage());
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
        List<ImageVO> images = imageService.list();
        return Result.success(images);
    }

    /**
     * 删除图片
     * @param id 图片 ID
     * @return 删除结果
     */
    @DeleteMapping
    public Result<?> delete(@RequestParam Long id) {
        // 从本地删除
        String filePath = imageService.getImageById(id).getPath();
        if (filePath != null) {
            try {
                // 从本地删除文件
                Path path = Paths.get(uploadDir).resolve(filePath).toAbsolutePath().normalize();
                Files.deleteIfExists(path);
            } catch (IOException e) {
                return Result.error("文件删除失败: " + e.getMessage());
            }
        }
        // 从数据库删除记录
        imageService.delete(id);
        return Result.success();
    }
}