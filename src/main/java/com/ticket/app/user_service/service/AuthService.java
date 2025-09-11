package com.ticket.app.user_service.service;

import com.ticket.app.eventdto.UserEvents;
import com.ticket.app.user_service.customs.AppUserDetails;
import com.ticket.app.user_service.dto.request.*;
import com.ticket.app.user_service.dto.response.ProfileResponse;
import com.ticket.app.user_service.dto.response.RoleResponse;
import com.ticket.app.user_service.dto.response.UserInfoResponse;
import com.ticket.app.user_service.dto.response.UserResponse;
import com.ticket.app.user_service.enums.Role;
import com.ticket.app.user_service.exceptions.*;
import com.ticket.app.user_service.jwts.JwtUtils;
import com.ticket.app.user_service.model.PasswordResetToken;
import com.ticket.app.user_service.model.UserInfo;
import com.ticket.app.user_service.model.UserProfile;
import com.ticket.app.user_service.repository.PasswordResetTokenRepository;
import com.ticket.app.user_service.repository.UserInfoRepository;
import com.ticket.app.user_service.repository.UserProfileRepository;
import com.ticket.app.user_service.util.EmailSubject;
import com.ticket.app.user_service.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;


@Service
public class AuthService {

    private final UserInfoRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordResetTokenRepository  resetTokenRepository;
    final KafkaTemplate<String, Object> kafkaTemplate;
    private static final SecureRandom secureRandom = new SecureRandom();
    private final UserProfileRepository userProfileRepository;
    private final EventUtil eventUtil;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    public AuthService(UserInfoRepository repository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                       KafkaTemplate<String, Object> kafkaTemplate,
                       PasswordResetTokenRepository resetTokenRepository,
                       UserProfileRepository userProfileRepository, EventUtil eventUtil) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;

        this.kafkaTemplate = kafkaTemplate;
        this.resetTokenRepository = resetTokenRepository;
        this.userProfileRepository = userProfileRepository;
        this.eventUtil = eventUtil;
    }

    @Transactional
    public UserResponse register(SignupDto dto) {
        if (repository.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("Email taken");

        UserInfo user = new UserInfo();
        user.setEmail(dto.getEmail());
        if (dto.getPassword().equals(dto.getConfirmPassword()))
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        else throw new PasswordMismatchException("Passwords do not match");

        UserProfile profile = new UserProfile();
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setPhoneNumber(dto.getPhoneNumber());

        user.setRole(Role.ROLE_USER);
        user.setActive(false);
        user.setCreatedAt(LocalDate.now());
        user.setUpdatedAt(LocalDate.now());
        user.setUserProfile(profile);

    log.info("Saving user-----------------");
        UserInfo savedUser = repository.save(user);
        log.info("User saved {}",savedUser);
        System.out.println("User id : "+ savedUser.getUserId());
        eventUtil.sendNormalEvent(savedUser,EmailSubject.USER_REGISTERED_SUBJECT);
        return new UserResponse(jwtUtils.generateToken(savedUser),
                "User registered successfully", LocalDateTime.now());
    }

    public String activateAccount(String userId){
        Optional<UserInfo> user = repository.findById(userId);
        if (user.isEmpty()){
            throw new UserNotFoundException("User with id does not exist");
        }
        UserInfo userInfo = user.get();
        userInfo.setActive(true);
        repository.save(userInfo);
        return "Account activated";
    }

    public String deactivateAccount(String userId){
        Optional<UserInfo> user = repository.findById(userId);
        if (user.isEmpty()){
            throw new UserNotFoundException("User with id does not exist");
        }
        UserInfo userInfo = user.get();
        userInfo.setActive(false);
        repository.save(userInfo);
        return "Account Deactivated";
    }

    public UserResponse login(LoginDto dto) {
        try {
            String token = null;
            Authentication authentication = authenticationManager.
                    authenticate(new UsernamePasswordAuthenticationToken(
                            dto.getEmail(), dto.getPassword()
                    ));
            if (authentication.isAuthenticated()) {
                AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
                UserInfo user = userDetails.getUserInfo();
                if (!user.getActive()) {
                    throw new UserAccountNotActivatedException("Account not activated");
                }
                user.setLastLoginAt(LocalDate.now());
                repository.save(user);
                eventUtil.sendLastLoggedEvent(user, EmailSubject.USER_LOGGED_IN_SUBJECT);
                token = jwtUtils.generateToken(user);
            }
            return new UserResponse(token, "User Logged In Successfully", LocalDateTime.now());
        } catch (BadCredentialsException ex){
            throw new InvalidCredentialsException("Password or Email is Incorrect");
        }
    }


    public void deleteUser(String userId, Authentication authentication) throws AccessDeniedException {
        String currentUserEmail = authentication.getName();
        UserInfo loggedInUser = repository.findByEmail(currentUserEmail).orElseThrow(() ->
                new UserNotFoundException("User was not found"));
        UserInfo userInfo = repository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("User not found"));

        if(!loggedInUser.getRoleType().equals(Role.ROLE_ADMIN))
            throw new AccessDeniedException("Access Denied: Admin Privileges required");

        log.info("Deleting User with the ID: {}-------------------", userId);
        repository.deleteById(userId);
        log.info("User deleted successfully");
            UserEvents events = new UserEvents();
            events.setSubject(EmailSubject.USER_DELETED_SUBJECT);
            events.setUserId(userId);
            events.setName(userInfo.getUserProfile().getLastName()+" "+userInfo.getUserProfile().getFirstName());
            events.setEmail(userInfo.getEmail());
            kafkaTemplate.send("user-topic",events);

    }
       // repository.deleteById(userId);

        //UserInfo user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));



    public UserResponse updateUserRole(RoleUpdatedRequest updatedRequest, String userId, Authentication authentication){
        String email = authentication.getName();
        UserInfo loggedInUser = repository.findByEmail(email).orElseThrow();
        if(!loggedInUser.getRoleType().equals(Role.ROLE_ADMIN))
            throw new AccessDeniedException("Access Denied: Admin privileges required");
        UserInfo user = repository.findById(userId).orElseThrow(
                () -> new InvalidTokenException("Invalid or expired token"));
    user.setRole(updatedRequest.getRole());
    repository.save(user);
   eventUtil.sendNormalEvent(user,EmailSubject.USER_ROLE_UPDATED_SUBJECT);
    return new UserResponse("Role updated",LocalDateTime.now());
    }

     @Transactional
     public RoleResponse createAdmin(CreateAdminRequest request, Authentication authentication){
         String email = authentication.getName();
         UserInfo loggedInUser = repository.findByEmail(email).orElseThrow();
         if(!loggedInUser.getRoleType().equals(Role.ROLE_ADMIN))
             throw new AccessDeniedException("Access Denied: Admin privileges required");
        UserInfo admin = new UserInfo();

        admin.setEmail(request.getEmail());
        admin.setRole(Role.ROLE_ADMIN);
        admin.setActive(true);
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setCreatedAt(LocalDate.now());
        admin.setUpdatedAt(LocalDate.now());

        UserProfile adminProfile = new UserProfile();
        adminProfile.setFirstName(request.getFirstName());
        adminProfile.setLastName(request.getLastName());
        admin.setUserProfile(adminProfile);
        repository.save(admin);
        eventUtil.sendAminCreatedEvent(admin,EmailSubject.ADMIN_CREATED_SUBJECT);
            return new RoleResponse("Admin Created Successfully",LocalDateTime.now());
        }

        public UserInfoResponse getUserInfo(String userId){
            UserInfo user = repository.findById(userId).orElseThrow(()->
                    new UserNotFoundException(userId));
            UserInfoResponse userInfoResponse = new UserInfoResponse();
            userInfoResponse.setUserId(user.getUserId());
            userInfoResponse.setEmail(user.getEmail());
            userInfoResponse.setRole(user.getRoleType());
            userInfoResponse.setProfile(new ProfileResponse(user.getUserProfile().getFirstName(),user.getUserProfile().getLastName(),
                    user.getUserProfile().getPhoneNumber(),user.getUserProfile().getAddress(),user.getUserProfile().getDateOfBirth(),user.getUserProfile().getProfilePicture(),
                    "Profile updated successfully",LocalDateTime.now()));
        userInfoResponse.setTimestamp(LocalDateTime.now());
        return userInfoResponse;
        }

        public RoleResponse resetPassword(ResetPasswordRequest request){
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
            eventUtil.sendPasswordResetEvent(user,EmailSubject.PASSWORD_RESET_SUBJECT,token);
            return new RoleResponse("Reset Link sent to mail",LocalDateTime.now());
        }

        public RoleResponse confirmReset(ResetPasswordConfirmRequest request){
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
            return new RoleResponse("Password reset successful", LocalDateTime.now());
        }
        @Transactional
        public ProfileResponse updateProfile(String userId,ProfileUpdateRequest request){

            UserInfo user = repository.findById(userId).orElseThrow(()->
                    new UserNotFoundException("User not found"));

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

        public RoleResponse requestAccountDeletion(String userId, AccountDeletionRequest request) {
            UserInfo user = repository.findById(userId).orElseThrow(() ->
                    new UserNotFoundException("User not found"));

            if (Boolean.TRUE.equals(user.getDeletionRequested())) {
                throw new IllegalStateException("You already requested for Account deletion");

            }
            user.setDeletionRequested(true);
            user.setDeletionRequestedAt(LocalDateTime.now());
            repository.save(user);

            //we need a kafka event here, so maybe a lil change to our event dtos

            return new RoleResponse("Account deletion request submitted", LocalDateTime.now());
        }
    public static String generateToken() {
        int number = secureRandom.nextInt(1_000_000); // 0 to 999999
        return String.format("%06d", number);
    }
}
