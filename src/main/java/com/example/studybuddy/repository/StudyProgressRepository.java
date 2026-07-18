package com.example.studybuddy.repository;

import com.example.studybuddy.model.StudyProgress;
import com.example.studybuddy.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyProgressRepository extends JpaRepository<StudyProgress, String> {
    Optional<StudyProgress> findByUser(User user);
}
