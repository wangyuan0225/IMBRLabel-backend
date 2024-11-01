package com.wy0225.imbrlabel.controller;

import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wy0225.imbrlabel.context.BaseContext;
import com.wy0225.imbrlabel.method.points;
import com.wy0225.imbrlabel.pojo.DTO.AnnotationDTO;
import com.wy0225.imbrlabel.pojo.DTO.ImageDTO;
import com.wy0225.imbrlabel.pojo.Result;
import com.wy0225.imbrlabel.pojo.VO.AnnotationVO;
import com.wy0225.imbrlabel.service.AnnotationService;
import com.wy0225.imbrlabel.service.ImageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.wy0225.imbrlabel.method.annotations.*;
import static com.wy0225.imbrlabel.method.points.*;

/**
 * @author wangy
 */
@RestController
@Slf4j
@RequestMapping("/annotations")
public class AnnotationController {
    @Resource
    private AnnotationService annotationService;

    @Resource
    private ImageService imgService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private ImageService imageService;

    /**
     * 添加标注模板
     * @param annotationDTO 标注模板数据传输对象
     * @return 添加结果
     */
    @PostMapping
    public Result<?> addAnnotation(@RequestBody AnnotationDTO annotationDTO) {
        if (annotationDTO.getName() == null || annotationDTO.getName().isEmpty()) {
            return Result.error("模板名称不能为空");
        }
        annotationService.addAnnotation(annotationDTO);
        return Result.success();
    }

    /**
     * 获取标注模板列表
     * @return 标注模板列表
     */
    @GetMapping
    public Result<?> list() {
        List<AnnotationVO> annotations = annotationService.list();
        return Result.success(annotations);
    }

    /**
     * 为图像添加标注
     * @param payload 请求体
     * @return 添加结果
     */
    @PatchMapping
    public Result<?> addAnnotationToImage(@RequestBody Map<String, Object> payload) {
        Long imageId = Long.parseLong((String) payload.get("imageId"));
        String annotations = (String) payload.get("annotations");
        annotationService.addAnnotationToImage(imageId, annotations);
        return Result.success();
    }

    /**
     * 导出标注信息
     * @param imageId 图像ID
     * @param type 导出类型
     * @return 导出结果
     */
    @GetMapping("/export")
    public Result<?> export(@RequestParam Long imageId, @RequestParam String type) {
        String annotations = "";
        if ("csv".equals(type)) {
            annotations = annotationService.exportCsv(imageId);
        }
        if ("json".equals(type)) {
            annotations = annotationService.exportJson(imageId);
        }
        if ("xml".equals(type)) {
            annotations = annotationService.exportXml(imageId);
        }
        return Result.success(annotations);
    }

    /**
     * 更新标注模板
     * @param annotationVO 标注模板视图对象
     * @return 更新结果
     */
    @PutMapping
    public Result<?> update(@RequestBody AnnotationVO annotationVO) {
        annotationService.updateAnnotation(annotationVO);
        return Result.success();
    }

    /**
     * 删除标注模板
     * @param id 标注模板ID
     * @return 删除结果
     */
    @DeleteMapping
    public Result<?> delete(@RequestParam Long id) {
        annotationService.deleteAnnotation(id);
        return Result.success();
    }

    /**
     * 半自动标注
     * @param payload 请求体
     * @return 自动标注结果
     */
    /**
     * 自动标注
     * @param payload 请求体
     * @return 自动标注结果
     */
    @PatchMapping("/auto")
    public Result<?> autoAnnotation(@RequestBody Map<String, Object> payload) {
        String annotations = (String) payload.get("annotations");
        Integer polygonsides = (Integer) payload.get("polygonSides");

        List<List<Integer>> allPoints = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("src/main/resources/static/output_coordinates.txt"));
            // 先读取所有点
            for (String line : lines) {
                String[] parts = line.split(", ");
                for (int i = 0; i < parts.length; i += 2) {
                    List<Integer> point = new ArrayList<>();
                    point.add(Integer.parseInt(parts[i]));
                    point.add(Integer.parseInt(parts[i + 1]));
                    allPoints.add(point);
                }
            }

            List<List<Integer>> coordinates=choicePointCounts(allPoints,polygonsides);
            if(coordinates==null) {
                return Result.error("采样点数不能超过原始点数：" + allPoints.size());
            }
            // 解析现有的 annotation 字符串
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> annotationsList;
            try {
                // 解码并解析 JSON 字符串
                String decodedAnnotations = java.net.URLDecoder.decode(annotations, StandardCharsets.UTF_8);
                annotationsList = objectMapper.readValue(decodedAnnotations, new TypeReference<>() {
                });
            } catch (Exception e) {
                return Result.error("Failed to parse annotations");
            }

            // 创建新的 annotation
            Map<String, Object> newAnnotation = createAnnotation("", coordinates, annotationsList.size());

            annotationsList.add(newAnnotation);

            String updatedAnnotations;
            try {
                updatedAnnotations = objectMapper.writeValueAsString(annotationsList);
            } catch (Exception e) {
                return Result.error("Failed to serialize annotations");
            }

            return Result.success(updatedAnnotations);
        } catch (Exception e) {
            return Result.error("Failed to process coordinates: " + e.getMessage());
        }
    }



    /**
     * 获取图像详细信息
     * @param imageId 图像ID
     * @return 图像详细信息
     */
    @GetMapping("/details")
    public Result<?> getImageDetails(@RequestParam Long imageId) {
        Long userId = BaseContext.getCurrentId();
        ImageDTO imageDTO = imgService.getImageById(imageId, userId);

        // 获取上一张和下一张图像的ID
        Long prevImageId = imgService.getPreviousImageId(userId, imageId);
        Long nextImageId = imgService.getNextImageId(userId, imageId);
        // 将 ImageDTO 转换为 Map
        Map<String, Object> details = new HashMap<>();
        details.put("path", imageDTO.getPath());
        details.put("name", imageDTO.getName());
        details.put("annotations", imageDTO.getAnnotations());
        details.put("prevImageId", prevImageId); // 添加上一张图像的ID
        details.put("nextImageId", nextImageId); // 添加下一张图像的ID

        System.out.println("Returning image details: " + details);
        return Result.success(details);
    }


    /**
     * 全自动标注
     * 访问一个文档的所有标注，以换行符为分界符
     */
    @PatchMapping("/fullauto")
    public Result<?> fullautoAnnotation(@RequestBody Map<String, Object> payload) {
        String annotations = (String) payload.get("annotations");
        Integer polygonsides = (Integer) payload.get("polygonSides");
//        Long imageId = (Long) payload.get("imageId");
        Integer selectedId = (Integer) payload.get("selectedId");   // 选中的标注id

        try {
            // 获取图像路径
//            Long userId = BaseContext.getCurrentId();
//            String filePath = userId + "/" + imageService.getImageById(imageId, userId).getPath();
//            Path imagePath = Paths.get(uploadDir).resolve(filePath).toAbsolutePath().normalize();
            
            // TODO: 调用Python算法生成坐标
            // 假设我们已经生成了坐标文件，并保存在 coordinatesPath 中
            // 读取坐标文件
            String coordinatesPath = "src/main/resources/static/test1.txt";
            if (!Files.exists(Paths.get(coordinatesPath))) {
                return Result.error("坐标文件不存在");
            }

            // 解析现有的标注
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> annotationsList;
            try {
                String decodedAnnotations = URLDecoder.decode(annotations, StandardCharsets.UTF_8);
                annotationsList = objectMapper.readValue(decodedAnnotations, new TypeReference<>() {});
            } catch (Exception e) {
                log.error("解析标注失败", e);
                return Result.error("解析标注失败: " + e.getMessage());
            }

            // 读取坐标文件
            List<String> lines = Files.readAllLines(Paths.get(coordinatesPath));

            // 选中了某个标注，选点数，更新，去原来，增新（此时一定已经加载完所有坐标了）
            if (selectedId != null) {
                // 获取选中标注的坐标
                String selectedLine = lines.get(selectedId);
                List<List<Integer>> selectedPoints = parseCoordinates(selectedLine);
                
                // 根据指定的点数下采样
                List<List<Integer>> sampledPoints = choicePointCounts(selectedPoints, polygonsides);
                if (sampledPoints == null) {
                    return Result.error("采样点数不能超过原始点数：" + selectedPoints.size());
                }

                // 找到要删除的标注
                Iterator<Map<String, Object>> iterator = annotationsList.iterator();
                String originalLabel = null;
                int originalIndex=-1;
                while (iterator.hasNext()) {
                    Map<String, Object> annotation = iterator.next();
                    // 直接比较 label 和 selectedId
                    if (annotation.get("label").equals(String.valueOf(selectedId))) {
                        originalLabel = (String) annotation.get("label");
                        originalIndex= (int) annotation.get("index");
                        iterator.remove();  // 删除匹配的标注
                        break;
                    }
                }

                // 一定会找到，不用判断
                // 创建新标注，使用原来的label
                Map<String, Object> newAnnotation = createAnnotation(
                        originalLabel,  // 使用原label或selectedId
                        sampledPoints,
                        originalIndex
                );

                // 添加新标注
                annotationsList.add(newAnnotation);
            } else {
                // id=null就是处理所有标注，首先得加载所有标注，不能选点数
                for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                    // 解析第i行坐标
                    List<List<Integer>> points = parseCoordinates(lines.get(lineIndex));
                    // 创建新标注
                    Map<String, Object> newAnnotation = createAnnotation(String.valueOf(lineIndex), points, annotationsList.size());
                    annotationsList.add(newAnnotation);
                }
            }

            String updatedAnnotations;
            // 序列化更新后的标注
            try {
                updatedAnnotations = objectMapper.writeValueAsString(annotationsList);
            } catch (Exception e) {
                return Result.error("Failed to serialize annotations");
            }
            return Result.success(updatedAnnotations);
            
        } catch (Exception e) {
            return Result.error("Failed to process coordinates: " + e.getMessage());
        }
    }
}
