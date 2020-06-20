package com.lvpb.miaosha.model.db;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MiaoshaUser {
    private Long id;
    private String password;
    private String nickname;
    private String salt;
    private String head;
    private String registerDate;
    private String lastLoginDate;
    private Integer loginCount;
}