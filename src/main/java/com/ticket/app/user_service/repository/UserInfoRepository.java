package com.ticket.app.user_service.repository;

import com.ticket.app.user_service.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

    Optional<UserInfo> findByEmail(String email);
}