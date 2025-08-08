package com.mdt.aisproject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpRequestBody {

    private String email;
    private String password;

}
