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
public class ImageVO {
    private Long id;
    private String name;
    private String type;
    private String path;
    private String annotations;
}
