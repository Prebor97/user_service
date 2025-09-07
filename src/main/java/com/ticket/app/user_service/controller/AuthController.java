package com.ticket.app.user_service.controller;

import com.ticket.app.user_service.dto.eventdto.UserRegisteredEvent;
import com.ticket.app.user_service.dto.request.*;
import com.ticket.app.user_service.dto.response.ProfileResponse;
import com.ticket.app.user_service.dto.response.RoleResponse;
import com.ticket.app.user_service.dto.response.UserInfoResponse;
import com.ticket.app.user_service.dto.response.UserResponse;
import com.ticket.app.user_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController

@RequestMapping("/v1/api/auth")
public class AuthController {

   final KafkaTemplate<String, Object> kafkaTemplate;
   private final AuthService authService;

    public AuthController(KafkaTemplate<String, Object> kafkaTemplate, AuthService authService) {
        this.kafkaTemplate = kafkaTemplate;
        this.authService = authService;
    }

    @PostMapping("/register")
   public ResponseEntity<UserResponse> register(@RequestBody SignupDto request){
      return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));

   }

   @PostMapping("/login")
   public ResponseEntity<UserResponse> authenticate(@RequestBody LoginDto request){
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
   }


   @DeleteMapping("/users/{userId}")
   public ResponseEntity<?> deleteUser(@PathVariable String userId, Authentication authentication) throws AccessDeniedException {

        authService.deleteUser(userId,authentication);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully",
                "timestamp", LocalDateTime.now()));

   }
   @PreAuthorize("hasRole('ADMIN')")
   @PostMapping("/admins")
   public ResponseEntity<RoleResponse> createAdmin(@RequestBody CreateAdminRequest request,Authentication authentication){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createAdmin(request,authentication));
   }
   @GetMapping("/users/{userId}/info")
   @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
   public ResponseEntity<UserInfoResponse> getUserInfo(@PathVariable String userId) {
       return ResponseEntity.status(HttpStatus.OK).body(authService.getUserInfo(userId));
   }
    @PostMapping("/reset-password/request")
   public ResponseEntity<UserResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.resetPassword(request));

    }
        @PostMapping("/reset-password/confirm")
        public ResponseEntity<UserResponse> confirmReset(@RequestBody ResetPasswordConfirmRequest request){
            return ResponseEntity.status(HttpStatus.OK).body(authService.confirmReset(request));

        }
        @PutMapping("profiles/{userId}")
        @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
        public ResponseEntity<ProfileResponse> updateProfile(@RequestBody ProfileUpdateRequest request, @PathVariable String userId){
            return ResponseEntity.status(HttpStatus.OK).body(authService.updateProfile(userId,request));
        }

}
