package com.cupk.healthy_diet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Integer id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String role;
    private String token;

    public UserVO(Integer id, String username, String nickname, String avatarUrl, String role) {
        this(id, username, nickname, avatarUrl, role, null);
    }
}
