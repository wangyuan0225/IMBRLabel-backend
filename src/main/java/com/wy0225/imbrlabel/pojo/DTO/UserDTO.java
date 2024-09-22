package com.wy0225.imbrlabel.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//用户登录数据传到后端
public class UserDTO {
    private String username;
    private String password;
}
