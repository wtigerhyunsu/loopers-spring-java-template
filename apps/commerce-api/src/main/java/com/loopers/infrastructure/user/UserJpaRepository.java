package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<UserModel, Long> {
    @Query("SELECT COUNT(u) > 0 FROM UserModel u WHERE u.loginId.loginId = :loginId")
    boolean existsByLoginId_LoginId(@Param("loginId") String loginId);
}
