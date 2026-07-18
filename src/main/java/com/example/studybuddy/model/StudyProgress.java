package com.example.studybuddy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "study_progress")
public class StudyProgress {

    @Id
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private int explains;

    @Column(nullable = false)
    private int quizzes;

    @Column(nullable = false)
    private int flashcards;

    @Column(name = "module_progress_json", nullable = false, columnDefinition = "TEXT")
    private String moduleProgressJson;

    public StudyProgress() {
    }

    public StudyProgress(String id, User user, int explains, int quizzes, int flashcards, String moduleProgressJson) {
        this.id = id;
        this.user = user;
        this.explains = explains;
        this.quizzes = quizzes;
        this.flashcards = flashcards;
        this.moduleProgressJson = moduleProgressJson;
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

    public int getExplains() {
        return explains;
    }

    public void setExplains(int explains) {
        this.explains = explains;
    }

    public int getQuizzes() {
        return quizzes;
    }

    public void setQuizzes(int quizzes) {
        this.quizzes = quizzes;
    }

    public int getFlashcards() {
        return flashcards;
    }

    public void setFlashcards(int flashcards) {
        this.flashcards = flashcards;
    }

    public String getModuleProgressJson() {
        return moduleProgressJson;
    }

    public void setModuleProgressJson(String moduleProgressJson) {
        this.moduleProgressJson = moduleProgressJson;
    }
}
