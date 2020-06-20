package com.lvpb.miaosha.vo;

import com.lvpb.miaosha.validator.IsMobile;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class LoginVo
{
    @NotNull
    @IsMobile
    private String mobile;

    @NotNull
    private String password;
}
