package com.wy0225.imbrlabel.pojo.DO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDO {
    private int id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String token;
}
