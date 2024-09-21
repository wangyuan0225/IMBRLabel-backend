package com.wy0225.imbrlabel.pojo.DO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author wangy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationDO {
    private Long id;
    private String name;
    private String label;
    private String strokeStyle;
    private String fillStyle;
    private Integer lineWidth;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
