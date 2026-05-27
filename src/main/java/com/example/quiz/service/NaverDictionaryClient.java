package com.example.quiz.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverDictionaryClient {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean checkSimilarity(String question, String answer) {
        try {
            // 네이버 백과사전 검색 API 호출
            URI uri = UriComponentsBuilder
                    .fromUriString("https://openapi.naver.com")
                    .path("/v1/search/encyc.json")
                    .queryParam("query", question)
                    .queryParam("display", 10)
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<NaverSearchResponse> response = restTemplate.exchange(
                    uri, HttpMethod.GET, entity, NaverSearchResponse.class);

            if (response.getBody() == null || response.getBody().getItems() == null) {
                return false;
            }

            // 검색 결과 항목들 중 사용자가 입력한 답이 포함되어 있는지 확인
            String cleanAnswer = answer.replaceAll("<[^>]*>", "").toLowerCase();
            
            for (NaverSearchResponse.Item item : response.getBody().getItems()) {
                String title = item.getTitle().replaceAll("<[^>]*>", "").toLowerCase();
                String description = item.getDescription().replaceAll("<[^>]*>", "").toLowerCase();
                
                if (title.contains(cleanAnswer) || description.contains(cleanAnswer)) {
                    log.info("Similarity found in Naver Dictionary for [{}]: {}", question, answer);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Naver API error: {}", e.getMessage());
        }
        return false;
    }

    @Data
    public static class NaverSearchResponse {
        private List<Item> items;

        @Data
        public static class Item {
            private String title;
            private String description;
        }
    }
}
