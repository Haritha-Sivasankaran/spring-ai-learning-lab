package com.example.studybuddy.service;

import com.example.studybuddy.model.UsageQuota;
import com.example.studybuddy.model.User;
import com.example.studybuddy.repository.UsageQuotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class QuotaService {

    private final UsageQuotaRepository quotaRepository;

    public QuotaService(UsageQuotaRepository quotaRepository) {
        this.quotaRepository = quotaRepository;
    }

    @Transactional
    public boolean incrementAndCheckQuota(User user, int limit) {
        LocalDate today = LocalDate.now();
        UsageQuota quota = quotaRepository.findByUserAndQuotaDate(user, today)
                .orElseGet(() -> new UsageQuota(UUID.randomUUID().toString(), user, today, 0));

        if (quota.getCallCount() >= limit) {
            return false;
        }

        quota.setCallCount(quota.getCallCount() + 1);
        quotaRepository.save(quota);
        return true;
    }
}
