package com.wy0225.imbrlabel.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wy0225.imbrlabel.pojo.DTO.AnnotationDTO;
import com.wy0225.imbrlabel.pojo.Result;
import com.wy0225.imbrlabel.pojo.VO.AnnotationVO;
import com.wy0225.imbrlabel.service.AnnotationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author wangy
 */
@RestController
@Slf4j
@RequestMapping("/annotations")
public class AnnotationController {
    @Resource
    private AnnotationService annotationService;

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
     * 为当前图像保存标注
     * @param imageId 图像ID
     * @param annotations 标注信息
     * @return 保存结果
     */
    @PatchMapping
    public Result<?> addAnnotationToImage(@RequestParam Long imageId, @RequestParam String annotations) {
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

    @PatchMapping("/auto")
    public Result<?> autoAnnotation(@RequestParam String annotations) {
        // 读取坐标文件
        List<List<Integer>> coordinates = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("src/main/resources/static/output_coordinates.txt"));
            for (String line : lines) {
                String[] parts = line.split(", ");
                for (int i = 0; i < parts.length; i += 2) {
                    List<Integer> point = new ArrayList<>();
                    point.add(Integer.parseInt(parts[i]));
                    point.add(Integer.parseInt(parts[i + 1]));
                    coordinates.add(point);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Failed to read coordinates");
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
            e.printStackTrace();
            return Result.error("Failed to parse annotations");
        }

        // 创建新的 annotation，使用 LinkedHashMap 保持插入顺序
        Map<String, Object> newAnnotation = new LinkedHashMap<>();
        newAnnotation.put("label", "");
        newAnnotation.put("coor", coordinates);
        newAnnotation.put("active", false);
        newAnnotation.put("creating", false);
        newAnnotation.put("dragging", false);
        newAnnotation.put("uuid", UUID.randomUUID().toString());
        newAnnotation.put("index", annotationsList.size());
        newAnnotation.put("type", 2);

        // 将新 annotation 添加到 annotations 列表的末尾
        annotationsList.add(newAnnotation);

        // 将更新后的 annotations 转换回字符串
        String updatedAnnotations;
        try {
            updatedAnnotations = objectMapper.writeValueAsString(annotationsList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Failed to serialize annotations");
        }

        // 返回结果
        return Result.success(updatedAnnotations);
    }
}
