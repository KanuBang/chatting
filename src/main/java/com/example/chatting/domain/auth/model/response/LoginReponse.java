package com.example.chatting.domain.auth.model.response;

import com.example.chatting.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유저 생성 response")
public record LoginReponse (
    @Schema(description = "error code")
    ErrorCode description,

    @Schema(description = "jwt token")
    String token
) { }
