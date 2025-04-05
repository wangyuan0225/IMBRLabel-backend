package com.wy0225.imbrlabel.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SegmentationResponseVO {
    private String message;        // 响应消息
    private String output;         // Python脚本输出
    private String filePath;       // 结果文件路径
    private boolean success;       // 处理是否成功
    private String error;          // 错误信息（如果有）

    // 使用建造者模式创建实例的静态方法
    public static SegmentationResponseVO success(String message, String output, String filePath) {
        return SegmentationResponseVO.builder()
                .success(true)
                .message(message)
                .output(output)
                .filePath(filePath)
                .build();
    }

    public static SegmentationResponseVO error(String error) {
        return SegmentationResponseVO.builder()
                .success(false)
                .error(error)
                .build();
    }
}
