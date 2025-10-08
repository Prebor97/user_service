package com.ticket.app.user_service.controller;

import com.ticket.app.user_service.dto.request.*;
import com.ticket.app.user_service.dto.response.ProfileResponse;
import com.ticket.app.user_service.dto.response.RoleResponse;
import com.ticket.app.user_service.dto.response.UserInfoResponse;
import com.ticket.app.user_service.dto.response.UserResponse;
import com.ticket.app.user_service.jwts.JwtUtils;
import com.ticket.app.user_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
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
   public ResponseEntity<UserResponse> register(@Valid @RequestBody SignupDto request){
      return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
   }

    @GetMapping("/gitCallback")
    public ResponseEntity<UserResponse> callback(@RequestParam("code") String code) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.gitOauthLogin(code));
    }

   @PatchMapping("/activate/{userId}")
   public ResponseEntity<String> activateUser(@PathVariable String userId){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.activateAccount(userId));
   }

   @PostMapping("/login")
   public ResponseEntity<UserResponse> authenticate(@Valid @RequestBody LoginDto request){
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
   }

   @DeleteMapping("/users/{userId}")
   @PreAuthorize("hasRole('ROLE_ADMIN')")
   public ResponseEntity<?> deleteUser(@PathVariable String userId) throws AccessDeniedException {
        authService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully",
                "timestamp", LocalDateTime.now()));
   }
   @PreAuthorize("hasRole('ROLE_ADMIN')")
   @PostMapping("/admins")
   public ResponseEntity<RoleResponse> createAdmin(@Valid @RequestBody CreateAdminRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createAdmin(request));
   }

   @PatchMapping("/users/{userId}/role")
   @PreAuthorize("hasRole('ROLE_ADMIN')")
   public ResponseEntity<UserResponse> roleUpdated(@Valid @RequestBody RoleUpdatedRequest updatedRequest, @PathVariable String userId){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.updateUserRole(updatedRequest,userId));
   }

   @GetMapping("/users/{userId}/info")
   @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.userId")
   public ResponseEntity<UserInfoResponse> getUserInfo(@PathVariable String userId) {
       return ResponseEntity.status(HttpStatus.OK).body(authService.getUserInfo(userId));
   }
    @PostMapping("/reset-password/request")
     public ResponseEntity<RoleResponse> resetPassword( @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.resetPassword(request));

    }
        @PostMapping("/reset-password/confirm")
        public ResponseEntity<RoleResponse> confirmReset(@Valid @RequestBody ResetPasswordConfirmRequest request){
            return ResponseEntity.status(HttpStatus.OK).body(authService.confirmReset(request));

        }
        @PutMapping("profiles/{userId}")
        @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.userId")
        public ResponseEntity<ProfileResponse> updateProfile(@RequestBody ProfileUpdateRequest request, @PathVariable String userId){
            return ResponseEntity.status(HttpStatus.OK).body(authService.updateProfile(userId,request));
        }

        @PreAuthorize("#userId == authentication.principal.userId or hasRole('ROLE_ADMIN')")
        @PatchMapping("/deactivate/{userId}")
        public ResponseEntity<String> deactivateUser(@PathVariable String userId){
         return ResponseEntity.status(HttpStatus.OK).body(authService.deactivateAccount(userId));
        }

        @PostMapping("/{userId}/request-deletion")
        @PreAuthorize("#userId == authentication.principal.userId")
        public ResponseEntity<RoleResponse> requestDeletion(@PathVariable String userId){
        return  ResponseEntity.ok(authService.requestAccountDeletion(userId));
        }


}
