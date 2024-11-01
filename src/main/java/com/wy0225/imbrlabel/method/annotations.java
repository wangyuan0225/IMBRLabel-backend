package com.wy0225.imbrlabel.method;

import java.util.*;

public class annotations {

    // 解析坐标字符串为点列表
    public static List<List<Integer>> parseCoordinates(String line) {
        List<List<Integer>> points = new ArrayList<>();
        String[] parts = line.split(",");

        for (int i = 0; i < parts.length; i += 2) {
            List<Integer> point = new ArrayList<>();
            point.add(Integer.parseInt(parts[i].trim()));
            point.add(Integer.parseInt(parts[i + 1].trim()));
            points.add(point);
        }
        return points;
    }

    // 创建新的标注对象
    public static Map<String, Object> createAnnotation(String label, List<List<Integer>> coordinates, int index) {
        Map<String, Object> annotation = new LinkedHashMap<>();
        annotation.put("label", label);
        annotation.put("coor", coordinates);
        annotation.put("active", false);
        annotation.put("creating", false);
        annotation.put("dragging", false);
        annotation.put("uuid", UUID.randomUUID().toString());
        annotation.put("index", index);
        annotation.put("type", 2);
        return annotation;
    }


    // 在annotationsList根据selectedId更新对应标注列表项为newPoints
    public static void updateAnnotation(List<Map<String, Object>> annotationsList, Integer selectedId, List<List<Integer>> newPoints) {
        // 查找并更新选中的标注
        boolean found = false;
        for (Map<String, Object> annotation : annotationsList) {
            if (annotation.get("index").equals(selectedId)) {
                annotation.put("coor", newPoints);
                found = true;
                break;
            }
        }

        // 如果没找到对应标注，创建新的
        if (!found) {
            annotationsList.add(createAnnotation(
                    selectedId.toString(),
                    newPoints,
                    annotationsList.size()
            ));
        }
    }
}

