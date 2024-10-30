package com.wy0225.imbrlabel.method;

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
}
