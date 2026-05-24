package com.social.minisocialplatform.controller;

import com.social.minisocialplatform.auth.AuthResponse;
import com.social.minisocialplatform.auth.LoginRequest;
import com.social.minisocialplatform.auth.SignupRequest;
import com.social.minisocialplatform.auth.User;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.web.bind.annotation.*;

import com.social.minisocialplatform.auth.JwtUtil;
import com.social.minisocialplatform.auth.UserRepository;
import com.social.minisocialplatform.model.Post;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return "Username already exists";
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), encodedPassword, request.getRole());
        userRepository.save(user);

        return "Signup successfully";
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole());

        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        
        return new AuthResponse(accessToken, refreshToken);
    }
}
