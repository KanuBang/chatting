package com.example.chatting.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.chatting.common.constants.Constants;
import com.example.chatting.common.exception.CustomException;
import com.example.chatting.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JWTProvider {
    private static String secretKey;  // JWT 토큰을 서명하고 검증하는데 사용되는 비밀키
    private static String refreshSecretKey; // 리프레시 토큰을 서명하고 검증하는데 사용되는 비밀키
    private static long tokenTimeForMinute; // 엑세스 토큰의 분 단위 유효 시간
    private static long refreshTokenTimeForMinute; // 리프레시 토큰의 분 단위 유효 시간

    // @Value 어노테이션을 사용하여 application.yml 파일에서 설정된 값을 주입한다.
    @Value("${token.secret-key}")
    public static void setSecretKey(String secretKey) {
        JWTProvider.secretKey = secretKey;
    }

    @Value("${token.refresh-secret-key}")
    public static void setRefreshSecretKey(String refreshSecretKey) {
        JWTProvider.refreshSecretKey = refreshSecretKey;
    }

    @Value("${token.token-time}")
    public static void setTokenTimeForMinute(long tokenTimeForMinute) {
        JWTProvider.tokenTimeForMinute = tokenTimeForMinute;
    }

    @Value("${token.refresh-token-time}")
    public static void setRefreshTokenTimeForMinute(long refreshTokenTimeForMinute) {
        JWTProvider.refreshTokenTimeForMinute = refreshTokenTimeForMinute;
    }

    // 사용자의 이름을 받아 JWT 엑세스 토큰을 생성
    // 서명할 때 secretkey를 사용하여 비밀키로 한다.
    public static String createToken(String name) {
        return JWT.create().withSubject(name)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenTimeForMinute * Constants.ON_MINUTE_TO_MILES))
                .sign(Algorithm.HMAC256(secretKey));
    }

    // 사용자의 이름을 받아 JWT 엑세스 토큰을 생성
    // 서명할 때 refreshSecretKey를 사용하여 비밀키로 한다.
    public static String createRefreshToken(String name) {
        return JWT.create().withSubject(name)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenTimeForMinute * Constants.ON_MINUTE_TO_MILES))
                .sign(Algorithm.HMAC256(refreshSecretKey));
    }

    // refresh 토큰이 만료되었느 지 확인
    public static DecodedJWT checkTokenForRefresh(String token) {
        try {
            DecodedJWT decoded = JWT.require(Algorithm.HMAC256(token)).build().verify(token);
            log.error("token must be expired : {}", decoded.getSubject());
            throw new CustomException(ErrorCode.ACCESS_TOKEN_IS_NOT_EXPIRED);
        } catch (AlgorithmMismatchException | SignatureVerificationException | InvalidClaimException e) {
            throw new CustomException(ErrorCode.TOKEN_IS_INVALID);
        } catch (TokenExpiredException e) {
            return JWT.decode(token);
        }
    }

    // 토큰을 주어진 비밀키(key)를 사용해 검증하고 디코딩한다.
    public static DecodedJWT decodeTokenAfterVerify(String token, String key) {
        try {
            return JWT.require(Algorithm.HMAC256(key)).build().verify(token);
        } catch (AlgorithmMismatchException | SignatureVerificationException | InvalidClaimException e) {
            throw new CustomException(ErrorCode.TOKEN_IS_INVALID);
        } catch (TokenExpiredException e) {
            throw new CustomException(ErrorCode.TOKEN_IS_EXPIRED);
        }
    }

    // 엑시스 토큰을 검증 및 디코딩한다.
    public static DecodedJWT decodeAccessToken(String token){
        return decodeTokenAfterVerify(token, secretKey);
    }

    // 리프레시 토큰을 검증 및 디코딩한다.
    public static DecodedJWT decodeRefreshToken(String token){
        return decodeTokenAfterVerify(token, refreshSecretKey);
    }


    // 토큰을 단순히 디코딩한다.
    public static DecodedJWT decodedJWT(String token) {
        return JWT.decode(token);
    }

    // 토큰에서 사용자의 식별자를 추출한다.
    public static String getUserFromToken(String token) {
        DecodedJWT jwt = decodedJWT(token);
        return jwt.getSubject();
    }
}
