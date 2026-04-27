package com.hireready.hireready.service;

import com.hireready.hireready.dto.request.LoginRequest;
import com.hireready.hireready.dto.request.RegisterRequest;
import com.hireready.hireready.dto.response.AuthResponse;
import com.hireready.hireready.entity.Role;
import com.hireready.hireready.entity.User;
import com.hireready.hireready.entity.VisaStatus;
import com.hireready.hireready.repository.UserRepository;
import com.hireready.hireready.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository; // to save user to the repo
    private final PasswordEncoder passwordEncoder; //to encode the password
    private final JwtUtil jwtUtil; //to generate a token

    public AuthResponse register(RegisterRequest request) {
        //mapping the DTO to the user object
        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .visaStatus(request.getVisaStatus())
                .role(Role.USER)
                .build();
        userRepository.save(newUser); //save the user in the db
        String userToken = jwtUtil.generateToken(newUser.getEmail()); //generate the token for the new user
        //creating the response DTO
        return AuthResponse.builder()
                .fullName(newUser.getFullName())
                .email(newUser.getEmail())
                .token(userToken)
                .visaStatus(newUser.getVisaStatus())
                .build();
    }
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        //we match the request password and the encoded password which is saved in the db
        //in the db you do not store the raw password
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid Password");
        }
        String token = jwtUtil.generateToken(request.getEmail());
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .visaStatus(user.getVisaStatus())
                .build();
    }
}
