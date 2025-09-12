package com.ticket.app.user_service.repository;

import com.ticket.app.user_service.enums.Role;
import com.ticket.app.user_service.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

    Optional<UserInfo> findByEmail(String email);
    @Query("SELECT u.email FROM UserInfo u WHERE u.role = :role")
    List<String> findEmailsByRole(@Param("role") Role role);
}