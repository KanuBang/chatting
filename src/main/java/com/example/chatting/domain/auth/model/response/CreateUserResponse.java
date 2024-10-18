package com.example.chatting.domain.auth.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "user 생성 response")
public record CreateUserResponse (
        @Schema(description = "성공 유무")
        String code
){
}
