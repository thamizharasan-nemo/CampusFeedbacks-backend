package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstitutionRegistrationRequest;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserLoginReqDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.security.dto.JwtRefreshTokenDTO;
import com.feedbacks.FeedbackSystem.security.dto.JwtResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.model.RefreshToken;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.UserRepository;
import com.feedbacks.FeedbackSystem.security.CustomUserDetailsService;
import com.feedbacks.FeedbackSystem.security.JwtUtils;
import com.feedbacks.FeedbackSystem.service.interfaces.InstitutionService;
import com.feedbacks.FeedbackSystem.service.interfaces.RefreshTokenService;
import com.feedbacks.FeedbackSystem.service.interfaces.UserService;
import com.feedbacks.FeedbackSystem.service.serviceImple.RefreshTokenServiceImpl;
import com.feedbacks.FeedbackSystem.service.serviceImple.UserServiceImpl;
import com.feedbacks.FeedbackSystem.service.other_services.HtmlEmailBody;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepo;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final InstitutionService institutionService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final HtmlEmailBody emailBody;

    public AuthController(UserService userService, UserRepository userRepo, JwtUtils jwtUtils, CustomUserDetailsService userDetailsService, InstitutionService institutionService, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService, HtmlEmailBody emailBody) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.institutionService = institutionService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.emailBody = emailBody;
    }

    @PostMapping("/register/institution")
    public ResponseEntity<ApiResponse<UserResponseDTO>> registerInstitution(
            @Valid @RequestBody InstitutionRegistrationRequest request
    ) {
        UserResponseDTO admin = institutionService.registerInstitution(request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Institution and admin registered successfully", admin)
        );
    }

    @PostMapping("/register/{institutionId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> registerUser(@Valid @RequestBody UserRequestDTO user,
                                                                     @PathVariable Integer institutionId) {
        UserResponseDTO savedUser = userService.addUser(user, institutionId);
        emailBody.registrationEmail(savedUser);
        // because of Async in this method, savedUser returned before the mail sent
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User registered successfully",
                        savedUser
                )
        );
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponseDTO>> login(@RequestBody @Valid UserLoginReqDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // generate access token
        // as well as refresh token
        String accessToken = jwtUtils.generateToken(userDetails);

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email! Please provide user registered email"));

        RefreshToken refreshToken = refreshTokenService.issue(user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Login successful",
                        new JwtResponseDTO(accessToken, refreshToken.getToken())
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponseDTO>> refresh(
            @RequestBody JwtRefreshTokenDTO request
    ) {
        RefreshToken current = refreshTokenService.verifyUsable(request.getRefreshToken());
        User user = current.getUser();
        // Rotate old refresh token with a new one
        RefreshToken rotated = refreshTokenService.rotate(current);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        // Generate new access token
        String newAccessToken = jwtUtils.generateToken(userDetails);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Token refreshed successfully",
                        new JwtResponseDTO(newAccessToken, rotated.getToken())
                )
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/tokens/{userId}")
    public ResponseEntity<ApiResponse<Void>> revokeTokens(@PathVariable int userId) {
        refreshTokenService.revokeAllForUser(userId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "All refresh tokens revoked", null)
        );
    }


}
