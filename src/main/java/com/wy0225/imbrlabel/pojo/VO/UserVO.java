package com.wy0225.imbrlabel.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//用户登录数据传到前端
public class UserVO {
    private int id;
    private String username;
    private String nickname;
    private String email;
    private String token;
}
