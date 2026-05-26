package com.example.quiz.service;

import com.example.quiz.entity.User;
import com.example.quiz.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizProcessor {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final EntityManager entityManager;

    private static final int CHUNK_SIZE = 500;

    /**
     * 프록시 문제를 해결하고 청크 단위 트랜잭션을 보장함.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean processChunk(int pageNumber, String today, String wordIds) {
        Slice<User> userSlice = userRepository.findAll(PageRequest.of(pageNumber, CHUNK_SIZE, Sort.by("id")));
        List<User> users = userSlice.getContent();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            byte[] serializedValue = serializer.serialize(wordIds);
            for (User user : users) {
                String key = QuizService.QUIZ_KEY_PREFIX + today + ":" + user.getId();
                connection.setEx(serializer.serialize(key), 
                               Duration.ofHours(24).getSeconds(), 
                               serializedValue);
            }
            return null;
        });

        users.forEach(entityManager::detach);
        log.info("Processed chunk page: {} ({} users)", pageNumber, users.size());

        return userSlice.hasNext();
    }
}
