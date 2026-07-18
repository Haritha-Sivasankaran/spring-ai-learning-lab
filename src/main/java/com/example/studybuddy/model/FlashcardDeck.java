package com.example.studybuddy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard_decks")
public class FlashcardDeck {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String level;

    @Column(name = "cards_json", nullable = false, columnDefinition = "TEXT")
    private String cardsJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public FlashcardDeck() {
    }

    public FlashcardDeck(String id, User user, String topic, String level, String cardsJson, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.topic = topic;
        this.level = level;
        this.cardsJson = cardsJson;
        this.createdAt = createdAt;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCardsJson() {
        return cardsJson;
    }

    public void setCardsJson(String cardsJson) {
        this.cardsJson = cardsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
