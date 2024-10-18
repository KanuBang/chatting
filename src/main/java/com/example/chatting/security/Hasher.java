package com.example.chatting.security;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class Hasher {
    public String getHashingValue(String password) {

        try {
            // 해싱 알고리즘
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 문자열 password를 UTF-8 형식의 바이트 배열로 변환하고 SHA-256 알고리즘을 적용하여 해싱된 바이트 배열을 만듭니다.
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            // 해싱된 바이트 배열을 Base64 형식의 문자열로 인코딩하여 반환한다.
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash Failed", e);
        }
    }
}