package org.ikaansh.taskmanagement.service;

import org.ikaansh.taskmanagement.dto.AuthResponse;
import org.ikaansh.taskmanagement.dto.LoginRequest;
import org.ikaansh.taskmanagement.dto.RegisterRequest;
import org.ikaansh.taskmanagement.entity.Role;
import org.ikaansh.taskmanagement.entity.User;
import org.ikaansh.taskmanagement.repository.RoleRepository;
import org.ikaansh.taskmanagement.repository.UserRepository;
import org.ikaansh.taskmanagement.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.ikaansh.taskmanagement.exception.ConflictException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already taken");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() ->
                        new IllegalStateException("Default user role is missing"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        var userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .authorities("ROLE_USER")
                        .build();

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, "Bearer", user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        var userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(request.getEmail())
                        .password("")
                        .authorities(
                                userRepository.findByEmail(request.getEmail())
                                        .orElseThrow()
                                        .getRoles().stream()
                                        .map(Role::getName)
                                        .toArray(String[]::new))
                        .build();

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, "Bearer", request.getEmail());
    }
}

