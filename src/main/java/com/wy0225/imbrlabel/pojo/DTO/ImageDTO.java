package com.wy0225.imbrlabel.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDTO {
    private String name;
    private String type;
    private String path;
    private String annotations;
}
