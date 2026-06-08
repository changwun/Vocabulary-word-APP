package com.example.quiz.repository;

import com.example.quiz.entity.User;
import com.example.quiz.entity.Word;
import com.example.quiz.entity.WrongAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface WrongAnswerRepository extends JpaRepository<WrongAnswer, Long> {
    
    @Query("SELECT wa FROM WrongAnswer wa JOIN FETCH wa.word WHERE wa.user.id = :userId ORDER BY wa.lastAttemptAt DESC")
    List<WrongAnswer> findAllByUserIdWithWord(@Param("userId") Long userId);
    
    Optional<WrongAnswer> findByUserAndWord(User user, Word word);
    
    void deleteByIdAndUser(Long id, User user);
}
