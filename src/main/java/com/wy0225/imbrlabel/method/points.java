package com.wy0225.imbrlabel.method;

import com.wy0225.imbrlabel.pojo.Result;
import org.apache.ibatis.jdbc.Null;

import java.util.ArrayList;
import java.util.List;

public class points {
    /**
     * 计算两点之间的距离
     */
    public static double distance(List<Integer> p1, List<Integer> p2) {
        double dx = p1.get(0) - p2.get(0);
        double dy = p1.get(1) - p2.get(1);
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 判断两点是否接近
     */
    public static boolean isClosePoints(List<Integer> p1, List<Integer> p2) {
        return distance(p1, p2) < 5.0;
    }

    /**
     * 在两点之间进行线性插值
     */
    public static List<Integer> interpolatePoint(List<Integer> p1, List<Integer> p2, double ratio) {
        List<Integer> point = new ArrayList<>();
        point.add((int) Math.round(p1.get(0) + (p2.get(0) - p1.get(0)) * ratio));
        point.add((int) Math.round(p1.get(1) + (p2.get(1) - p1.get(1)) * ratio));
        return point;
    }


    /**
     * 从所有的点allPoints中选取polygonsides个
     * @param allPoints
     * @param polygonsides
     * @return
     */
    public static List<List<Integer>> choicePointCounts(List<List<Integer>> allPoints,Integer polygonsides) {
        List<List<Integer>> coordinates=new ArrayList<>();
        // 如果没有指定 polygonsides，使用所有点
        if (polygonsides == null) {
            coordinates = allPoints;
        } else {
            if (polygonsides > allPoints.size()) {
                return null;
            }

            // 确保曲线闭合
            if (!allPoints.isEmpty() && !points.isClosePoints(allPoints.get(0), allPoints.get(allPoints.size() - 1))) {
                allPoints.add(new ArrayList<>(allPoints.get(0)));
            }

            // 计算累积距离
            List<Double> cumulativeDistances = new ArrayList<>();
            cumulativeDistances.add(0.0);
            double totalDistance = 0.0;

            for (int i = 1; i < allPoints.size(); i++) {
                totalDistance += points.distance(allPoints.get(i-1), allPoints.get(i));
                cumulativeDistances.add(totalDistance);
            }

            // 均匀采样
            coordinates = new ArrayList<>();
            coordinates.add(allPoints.get(0));

            double step = totalDistance / (polygonsides - 1);
            double currentDistance = step;
            int currentIndex = 0;

            for (int i = 1; i < polygonsides - 1; i++) {
                while (currentIndex < allPoints.size() - 1 &&
                        cumulativeDistances.get(currentIndex) < currentDistance) {
                    currentIndex++;
                }

                if (currentIndex > 0) {
                    List<Integer> p1 = allPoints.get(currentIndex - 1);
                    List<Integer> p2 = allPoints.get(currentIndex);
                    double d1 = cumulativeDistances.get(currentIndex - 1);
                    double d2 = cumulativeDistances.get(currentIndex);
                    double ratio = (currentDistance - d1) / (d2 - d1);

                    List<Integer> interpolatedPoint = points.interpolatePoint(p1, p2, ratio);
                    coordinates.add(interpolatedPoint);
                }

                currentDistance += step;
            }
            coordinates.add(allPoints.get(allPoints.size() - 1));
        }
        return coordinates;
    }
}
