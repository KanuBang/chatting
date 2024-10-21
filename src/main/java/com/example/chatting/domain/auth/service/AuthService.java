package com.example.chatting.domain.auth.service;

import com.example.chatting.common.exception.CustomException;
import com.example.chatting.common.exception.ErrorCode;
import com.example.chatting.domain.auth.model.request.CreateUserRequest;
import com.example.chatting.domain.auth.model.request.LoginRequest;
import com.example.chatting.domain.auth.model.response.CreateUserResponse;
import com.example.chatting.domain.auth.model.response.LoginReponse;
import com.example.chatting.repository.UserRepository;
import com.example.chatting.repository.entity.User;
import com.example.chatting.repository.entity.UserCredentials;
import com.example.chatting.security.Hasher;
import com.example.chatting.security.JWTProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final Hasher hasher;
    @Transactional(transactionManager = "createUserTransactionManager")
    public CreateUserResponse createUser(CreateUserRequest request) {
        Optional<User> user = userRepository.findByName(request.name());

        System.out.println(request.name());
        System.out.println(request.password());

        if(user.isPresent()) {
            log.error("USER_ALREADY_EXISTS: {}", request.name());
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        try {
            User newUser = this.newUser(request.name());
            UserCredentials userCredentials = this.newUserCredentials(request.password(), newUser);
            newUser.setCredentials(userCredentials);

            User savedUser = userRepository.save(newUser);

            if(savedUser == null) {
                System.out.println("------------");
                throw new CustomException(ErrorCode.USER_SAVED_FAILED);
            }
        } catch (Exception e) {
            throw  new CustomException(ErrorCode.USER_SAVED_FAILED, e.getMessage());
        }

        return new CreateUserResponse(request.name());
    }

    public LoginReponse login(LoginRequest request) {
        Optional<User> user = userRepository.findByName(request.name());
        // 회원가입 되지 않은 사용자 에외처리
        if(!user.isPresent()) {
            log.error("NOT_EXIST_USER: {}", request.name());
            throw new CustomException(ErrorCode.NOT_EXIST_USER);
        }

        user.map(u -> {
            String hashedValue = hasher.getHashingValue(request.password());

            if(!u.getUserCredentials().getHashed_password().equals(hashedValue)) {
                throw new CustomException(ErrorCode.MIS_MATCH_PASSWORD);
            }

            return hashedValue;
        }).orElseThrow(() -> {
            throw new CustomException(ErrorCode.MIS_MATCH_PASSWORD);
        });

        String token = JWTProvider.createRefreshToken(request.name());
        return new LoginReponse(ErrorCode.SUCCESS, token);
    }

    public String getUserFromToken(String token) {
        return JWTProvider.getUserFromToken(token);
    }
    // User 생성 메서드
    private User newUser(String name){
        User newUser = User.builder()
                .name(name)
                .created_at(new Timestamp(System.currentTimeMillis()))
                .build();
        return newUser;
    }

    // UserCredentials 생성 메서드
    private UserCredentials newUserCredentials(String password, User user) {
        String hashedValue = hasher.getHashingValue(password);
        UserCredentials cre = UserCredentials.builder()
                .user(user)
                .hashed_password(hashedValue)
                .build();
        return cre;
    }
}