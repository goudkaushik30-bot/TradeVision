package com.tradevision.userservice.service;

import com.tradevision.common.exception.TradeVisionException;
import com.tradevision.common.security.JwtUtil;
import com.tradevision.userservice.dto.AuthResponse;
import com.tradevision.userservice.dto.LoginRequest;
import com.tradevision.userservice.dto.RegisterRequest;
import com.tradevision.userservice.model.Role;
import com.tradevision.userservice.model.User;
import com.tradevision.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new TradeVisionException("Username already exists", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new TradeVisionException("Email already registered", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user: {}", saved.getUsername());

        String accessToken = jwtUtil.generateToken(saved.getUsername(), saved.getRole().name(), saved.getId());
        String refreshToken = jwtUtil.generateRefreshToken(saved.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userService.mapToDto(saved))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new TradeVisionException("User not found", HttpStatus.NOT_FOUND));

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        log.info("User logged in: {}", username);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userService.mapToDto(user))
                .build();
    }
}
