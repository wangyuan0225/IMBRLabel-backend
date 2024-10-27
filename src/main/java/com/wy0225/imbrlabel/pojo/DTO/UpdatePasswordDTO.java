package com.wy0225.imbrlabel.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordDTO {
    private String old_pwd;
    private String new_pwd;
    private String re_pwd;

}
