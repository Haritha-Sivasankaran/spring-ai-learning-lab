package com.example.studybuddy.repository;

import com.example.studybuddy.model.QuizResult;
import com.example.studybuddy.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, String> {
    List<QuizResult> findByUserOrderByCompletedAtDesc(User user);
}
