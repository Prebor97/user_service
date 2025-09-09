package com.ticket.app.user_service.service;

import com.ticket.app.user_service.customs.AppUserDetails;
import com.ticket.app.user_service.dto.eventdto.*;
import com.ticket.app.user_service.dto.request.*;
import com.ticket.app.user_service.dto.response.ProfileResponse;
import com.ticket.app.user_service.dto.response.RoleResponse;
import com.ticket.app.user_service.dto.response.UserInfoResponse;
import com.ticket.app.user_service.dto.response.UserResponse;
import com.ticket.app.user_service.enums.Role;
import com.ticket.app.user_service.exceptions.InvalidCredentialsException;
import com.ticket.app.user_service.exceptions.InvalidTokenException;
import com.ticket.app.user_service.exceptions.PasswordMismatchException;
import com.ticket.app.user_service.exceptions.UserNotFoundException;
import com.ticket.app.user_service.jwts.JwtUtils;
import com.ticket.app.user_service.model.PasswordResetToken;
import com.ticket.app.user_service.model.UserInfo;
import com.ticket.app.user_service.model.UserProfile;
import com.ticket.app.user_service.repository.PasswordResetTokenRepository;
import com.ticket.app.user_service.repository.UserInfoRepository;
import com.ticket.app.user_service.repository.UserProfileRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {

    private final UserInfoRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordResetTokenRepository  resetTokenRepository;
    final KafkaTemplate<String, Object> kafkaTemplate;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();
    private final UserProfileRepository userProfileRepository;


    public AuthService(UserInfoRepository repository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                       KafkaTemplate<String, Object> kafkaTemplate,
                       UserInfoRepository userInfoRepository, PasswordResetTokenRepository resetTokenRepository,
                       UserProfileRepository userProfileRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;

        this.kafkaTemplate = kafkaTemplate;
        this.resetTokenRepository = resetTokenRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserResponse register(SignupDto dto) {
        if (repository.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("Email taken");

        UserInfo user = new UserInfo();
        user.setEmail(dto.getEmail());
        if (dto.getPassword().equals(dto.getConfirmPassword()))
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        else throw new IllegalArgumentException("Passwords do not match");

        UserProfile profile = new UserProfile();
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setPhoneNumber(dto.getPhoneNumber());

        user.setRole(Role.USER);
        user.setActive(true);
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        user.setUserProfile(profile);


        UserInfo savedUser = repository.save(user);
        UserRegisteredEvent userRegisteredEvent = new UserRegisteredEvent(savedUser.getUserId(),savedUser.getEmail(), profile.getFirstName());
       // kafkaTemplate.send("user-registered-topic", userRegisteredEvent);

        return new UserResponse(jwtUtils.generateToken(savedUser),
                "User registered successfully", LocalDateTime.now());

    }

    public UserResponse login(LoginDto dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getEmail(), dto.getPassword()
                    )
            );

            AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
            UserInfo user = userDetails.getUserInfo();


            user.setLastLoginAt(LocalDate.now());
            repository.save(user);


            String token = jwtUtils.generateToken(user);


            UserLoggedInEvent event = new UserLoggedInEvent(
                    user.getUserId(),
                    user.getEmail(),
                    user.getUserProfile().getFirstName(),
                    LocalDateTime.now()
            );
            // kafkaTemplate.send("user-logged-in-topic", event);

            return new UserResponse(token, "User Logged In Successfully", LocalDateTime.now());

        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Password or email incorrect");
        }
    }

    public void deleteUser(String userId, Authentication authentication) throws AccessDeniedException {

        String currentUserEmail = authentication.getName();

        UserInfo currentUser = repository.findByEmail(currentUserEmail).orElseThrow(() ->
                new UserNotFoundException("User not found"));

        if(currentUser.getRole() == Role.ADMIN){
            repository.deleteById(userId);
        return;

    }

        if(!currentUser.getUserId().equals(userId)){
        throw new AccessDeniedException("You can only delete your own account");
        }

        repository.deleteById(userId);
        UserInfo user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        UserDeletedEvent userDeletedEvent = new UserDeletedEvent(user.getUserId(),user.getEmail(),user.getUserProfile().getFirstName());
    kafkaTemplate.send("user-deleted-topic", userDeletedEvent);
    }

     @Transactional
     public RoleResponse createAdmin(CreateAdminRequest request, Authentication authentication){
         String email = authentication.getName();

         UserInfo loggedInUser = repository.findByEmail(email).orElseThrow();

         if(!loggedInUser.getRole().equals(Role.ADMIN))
             throw new AccessDeniedException("Access Denied: Admin privileges required");

        UserInfo admin = new UserInfo();

        admin.setEmail(request.getEmail());
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setCreatedAt(LocalDate.now());
        admin.setUpdatedAt(LocalDate.now());

        UserProfile adminProfile = new UserProfile();
        adminProfile.setFirstName(request.getFirstName());
        adminProfile.setLastName(request.getLastName());

        admin.setUserProfile(adminProfile);

        UserInfo savedAdmin = repository.save(admin);
            AdminCreatedEvent adminCreatedEvent = new AdminCreatedEvent(savedAdmin.getUserId(),
                    savedAdmin.getEmail(),
                    savedAdmin.getUserProfile().getFirstName(),
                    savedAdmin.getPassword());
            kafkaTemplate.send("admin-created-topic",adminCreatedEvent);

            return new RoleResponse("Admin Created Successfully",LocalDateTime.now());
        }

        public UserInfoResponse getUserInfo(String userId){
            UserInfo user = repository.findById(userId).orElseThrow(()->
                    new UserNotFoundException(userId));

            UserInfoResponse userInfoResponse = new UserInfoResponse();
            userInfoResponse.setUserId(user.getUserId());
            userInfoResponse.setEmail(user.getEmail());
            userInfoResponse.setRole(user.getRole());
            userInfoResponse.setProfile(new ProfileResponse(user.getUserProfile().getFirstName(),user.getUserProfile().getLastName(),
                    user.getUserProfile().getPhoneNumber(),user.getUserProfile().getAddress(),user.getUserProfile().getDateOfBirth(),user.getUserProfile().getProfilePicture(),
                    "Profile updated successfully",LocalDateTime.now()));

        userInfoResponse.setTimestamp(LocalDateTime.now());

        return userInfoResponse;


        }

        public UserResponse resetPassword(ResetPasswordRequest request){
         UserInfo user = repository.findByEmail(request.getEmail()).orElseThrow(()->
                 new UserNotFoundException("User not found"));
         String userId = user.getUserId();
             String token = generateToken();
    LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

            PasswordResetToken resetToken = new PasswordResetToken();

            resetToken.setToken(token);
            resetToken.setExpiryDate(expiry);
            resetToken.setUserId(userId);
            resetTokenRepository.save(resetToken);
            PasswordResetRequestEvent passwordResetRequestEvent = new PasswordResetRequestEvent(userId, user.getEmail(),
                    user.getUserProfile().getFirstName(),token);

            kafkaTemplate.send("password-reset-requested-topic", passwordResetRequestEvent);

            return new UserResponse("Reset Link sent to your email",LocalDateTime.now());


        }

        public UserResponse confirmReset(ResetPasswordConfirmRequest request){
            PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken())
                    .orElseThrow(() -> new InvalidTokenException("Invalid or expired token"));

            if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new InvalidTokenException("Token is expired or already used");
            }

            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new PasswordMismatchException("Passwords do not match");
            }

            UserInfo user = repository.findById(resetToken.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            user.setPassword(passwordEncoder.encode(request.getPassword()));
            repository.save(user);

            resetToken.setUsed(true);
            resetTokenRepository.save(resetToken);

            return new UserResponse("Password reset successful", LocalDateTime.now());

        }
        @Transactional
        public ProfileResponse updateProfile(String userId,ProfileUpdateRequest request){


            //String email = authentication.getName();
            UserInfo user = repository.findById(userId).orElseThrow(()-> new UserNotFoundException("User not found"));
//            if(!user.getRole().equals(Role.ADMIN)){
//                throw new AccessDeniedException("Access Denied: Only Admin can update profiles for users");
//
//            }
//            if(!user.getUserId().equals(userId)){
//                throw new AccessDeniedException("Access Denied");
//            }

            UserProfile profile = user.getUserProfile();
            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());
            profile.setAddress(request.getAddress());
            profile.setDateOfBirth(request.getDateOfBirth());
            profile.setProfilePicture(request.getProfilePictureUrl());
            profile.setPhoneNumber(request.getPhoneNumber());
            profile.setUpdatedAt(LocalDate.now());

            userProfileRepository.save(profile);

            return new ProfileResponse(user.getUserProfile().getFirstName(),user.getUserProfile().getLastName(),
                    user.getUserProfile().getPhoneNumber(), user.getUserProfile().getAddress(),user.getUserProfile().getDateOfBirth(),
                    user.getUserProfile().getProfilePicture(),"profile updated successfully",LocalDateTime.now());

        }
    public static String generateToken() {
        int number = secureRandom.nextInt(1_000_000);  // 0 to 999999
        return String.format("%06d", number);
    }


}
