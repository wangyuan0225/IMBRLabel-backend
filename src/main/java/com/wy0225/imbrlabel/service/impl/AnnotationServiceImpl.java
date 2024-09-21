package com.wy0225.imbrlabel.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.wy0225.imbrlabel.mapper.AnnotationMapper;
import com.wy0225.imbrlabel.pojo.DO.AnnotationDO;
import com.wy0225.imbrlabel.pojo.DTO.AnnotationDTO;
import com.wy0225.imbrlabel.pojo.VO.AnnotationVO;
import com.wy0225.imbrlabel.service.AnnotationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangy
 */
@Service
@Slf4j
public class AnnotationServiceImpl implements AnnotationService {
    @Resource
    private AnnotationMapper annotationMapper;

    @Override
    public void addAnnotation(AnnotationDTO annotationDTO) {
        AnnotationDO annotationDO = new AnnotationDO();
        annotationDO.setName(annotationDTO.getName());
        annotationDO.setLabel(annotationDTO.getLabel());
        annotationDO.setStrokeStyle(annotationDTO.getStrokeStyle());
        annotationDO.setFillStyle(annotationDTO.getFillStyle());
        annotationDO.setLineWidth(annotationDTO.getLineWidth());
        annotationDO.setCreateTime(LocalDateTime.now());
        annotationDO.setUpdateTime(LocalDateTime.now());
        annotationMapper.insert(annotationDO);
    }

    @Override
    public List<AnnotationVO> list() {
        List<AnnotationDO> annotations = annotationMapper.list();
        List<AnnotationVO> annotationVos = new java.util.ArrayList<>();
        for (AnnotationDO annotation : annotations) {
            AnnotationVO annotationVo = new AnnotationVO();
            annotationVo.setId(annotation.getId());
            annotationVo.setName(annotation.getName());
            annotationVo.setLabel(annotation.getLabel());
            annotationVo.setStrokeStyle(annotation.getStrokeStyle());
            annotationVo.setFillStyle(annotation.getFillStyle());
            annotationVo.setLineWidth(annotation.getLineWidth());
            annotationVos.add(annotationVo);
        }
        return annotationVos;
    }

    @Override
    public void addAnnotationToImage(Long imageId, String annotations) {
        annotationMapper.updateAnnotation(imageId, annotations);
    }

    private void filterFields(JsonNode node) {
        if (node.isArray()) {
            for (JsonNode childNode : node) {
                if (childNode.isObject()) {
                    ((ObjectNode) childNode).remove(Arrays.asList("active", "creating", "dragging", "uuid"));
                }
            }
        } else if (node.isObject()) {
            ((ObjectNode) node).remove(Arrays.asList("active", "creating", "dragging", "uuid"));
        }
    }

    @Override
    public String exportCsv(Long imageId) {
        String encodedAnnotations = annotationMapper.getAnnotations(imageId);
        if (encodedAnnotations == null) {
            return "";
        }
        String decodedAnnotations = URLDecoder.decode(encodedAnnotations, StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonTree = objectMapper.readTree(decodedAnnotations);
            filterFields(jsonTree);

            List<Map<String, String>> records = new ArrayList<>();
            if (jsonTree.isArray()) {
                for (JsonNode node : jsonTree) {
                    Map<String, String> record = new LinkedHashMap<>();
                    node.fields().forEachRemaining(entry -> {
                        if ("coor".equals(entry.getKey())) {
                            record.put("coor", entry.getValue().toString());
                        } else {
                            record.put(entry.getKey(), entry.getValue().asText());
                        }
                    });
                    records.add(record);
                }
            }

            // 获取所有列名
            Set<String> headers = records.stream()
                    .flatMap(map -> map.keySet().stream())
                    .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

            // 生成 CSV
            StringWriter out = new StringWriter();
            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])))) {
                for (Map<String, String> record : records) {
                    List<String> row = headers.stream()
                            .map(header -> record.getOrDefault(header, ""))
                            .collect(Collectors.toList());
                    printer.printRecord(row);
                }
            }
            return out.toString();
        } catch (Exception e) {
            log.error("Failed to export annotations to CSV", e);
            return "";
        }
    }

    @Override
    public String exportJson(Long imageId) {
        String encodedAnnotations = annotationMapper.getAnnotations(imageId);
        if (encodedAnnotations == null) {
            return "";
        }
        String decodedAnnotations = URLDecoder.decode(encodedAnnotations, StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(decodedAnnotations);
            filterFields(jsonNode);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (IOException e) {
            log.error("Failed to export annotations", e);
            return "";
        }
    }

    @Override
    public String exportXml(Long imageId) {
        String encodedAnnotations = annotationMapper.getAnnotations(imageId);
        if (encodedAnnotations == null) {
            return "";
        }
        String decodedAnnotations = URLDecoder.decode(encodedAnnotations, StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(decodedAnnotations);
            filterFields(jsonNode);

            // 如果是数组，包装在一个根节点下
            if (jsonNode.isArray()) {
                ObjectNode root = objectMapper.createObjectNode();
                root.set("annotations", jsonNode);
                jsonNode = root;
            }

            // 保持 `coor` 字段为整体
            if (jsonNode.has("annotations")) {
                ArrayNode arrayNode = (ArrayNode) jsonNode.get("annotations");
                for (JsonNode node : arrayNode) {
                    if (node.has("coor")) {
                        ((ObjectNode) node).put("coor", node.get("coor").toString());
                    }
                }
            }

            return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (IOException e) {
            log.error("Failed to export annotations as XML", e);
            return "";
        }
    }

    @Override
    public void updateAnnotation(AnnotationVO annotationVO) {
        AnnotationDO annotationDO = new AnnotationDO();
        annotationDO.setId(annotationVO.getId());
        annotationDO.setName(annotationVO.getName());
        annotationDO.setLabel(annotationVO.getLabel());
        annotationDO.setStrokeStyle(annotationVO.getStrokeStyle());
        annotationDO.setFillStyle(annotationVO.getFillStyle());
        annotationDO.setLineWidth(annotationVO.getLineWidth());
        annotationDO.setUpdateTime(LocalDateTime.now());
        annotationMapper.update(annotationDO);
    }

    @Override
    public void deleteAnnotation(Long id) {
        annotationMapper.delete(id);
    }
}
