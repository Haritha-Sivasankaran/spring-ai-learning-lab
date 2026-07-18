package com.example.studybuddy.repository;

import com.example.studybuddy.model.FlashcardDeck;
import com.example.studybuddy.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardDeckRepository extends JpaRepository<FlashcardDeck, String> {
    Optional<FlashcardDeck> findByUserAndTopicIgnoreCase(User user, String topic);
    List<FlashcardDeck> findByUser(User user);
}
