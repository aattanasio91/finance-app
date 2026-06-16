package com.finance.app.auth;

import com.finance.app.auth.dto.LoginRequest;
import com.finance.app.auth.dto.LoginResponse;
import com.finance.app.auth.dto.RegisterRequest;
import com.finance.app.auth.jwt.JwtTokenProvider;
import com.finance.app.user.User;
import com.finance.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Registration failed - email already exists: {}", request.email());
            throw new BadCredentialsException("Email already registered");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getName());

        log.info("User registered: id={}, email={}, name={}", user.getId(), user.getEmail(), user.getName());
        return new LoginResponse(token, user.getId(), user.getName(), user.getEmail());
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed - email not found: {}", request.email());
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed - wrong password for: {}", request.email());
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getName());

        log.info("User logged in: id={}, email={}", user.getId(), user.getEmail());
        return new LoginResponse(token, user.getId(), user.getName(), user.getEmail());
    }
}
