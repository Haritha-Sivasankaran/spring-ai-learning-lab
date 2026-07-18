package com.example.studybuddy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "usage_quotas")
public class UsageQuota {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "quota_date", nullable = false)
    private LocalDate quotaDate;

    @Column(name = "call_count", nullable = false)
    private int callCount;

    public UsageQuota() {
    }

    public UsageQuota(String id, User user, LocalDate quotaDate, int callCount) {
        this.id = id;
        this.user = user;
        this.quotaDate = quotaDate;
        this.callCount = callCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getQuotaDate() {
        return quotaDate;
    }

    public void setQuotaDate(LocalDate quotaDate) {
        this.quotaDate = quotaDate;
    }

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }
}
