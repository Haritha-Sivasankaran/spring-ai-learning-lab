package com.example.studybuddy.repository;

import com.example.studybuddy.model.UsageQuota;
import com.example.studybuddy.model.User;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsageQuotaRepository extends JpaRepository<UsageQuota, String> {
    Optional<UsageQuota> findByUserAndQuotaDate(User user, LocalDate quotaDate);
}
