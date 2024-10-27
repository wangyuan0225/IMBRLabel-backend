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
public class ImageDO {
    private Long id;
    private String name;
    private String type;
    private String path;
    private String annotations;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
