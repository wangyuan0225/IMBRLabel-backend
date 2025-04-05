package com.wy0225.imbrlabel.pojo.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wy0225.imbrlabel.context.BaseContext;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentationRequestDTO {
    @NotNull(message = "文件不能为空")
    private MultipartFile file;

    @Min(value = 1, message = "点击次数必须大于0")
    private int numClicks = 3;  // 默认值

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    private String dotList;  // 可选参数

    @JsonIgnore  // 用于获取转换后的dotList
    public List<List<Integer>> getDotListAsObject() {
        if (dotList == null || dotList.trim().isEmpty()) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(dotList, new TypeReference<List<List<Integer>>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("dotList格式错误: " + e.getMessage());
        }
    }

}
