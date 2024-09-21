package com.wy0225.imbrlabel.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationVO {
    private Long id;
    private String name;
    private String label;
    private String strokeStyle;
    private String fillStyle;
    private Integer lineWidth;
}
