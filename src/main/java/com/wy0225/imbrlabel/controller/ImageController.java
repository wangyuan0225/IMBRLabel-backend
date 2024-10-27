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
            // TODO:（建议）上传图片取消同名文件检测，否则会发生多用户上传重名限制等问题
            //  - 方案1：采用添加时间戳后缀区分
            //  - 方案2：采用添加_1、_2等后缀区分
            //  - 方案3：不进行区分，由系统默认添加后缀(1)、(2)
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
            // TODO：imageDTO增加用户ID字段，保存用户上传图片的信息
            //  - 通过ThreadLocalUtil.get()获取用户ID
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
        // 获取文件名（不含扩展名）
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
        // TODO：传入参数，获取用户上传的图片列表
        //  - 同样地，通过ThreadLocalUtil.get()获取用户ID
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
        // TODO：删除当前用户上传的图片
        //  - 同样地，通过ThreadLocalUtil.get()获取用户ID
        imageService.delete(id);
        return Result.success();
    }
}
