package com.popobob.giftcard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.HashMap;

@Service
public class OtpService {
    @Value("${msg91.auth-key}")
    private String MSG91_AUTH_KEY;

    private static final String VERIFY_URL = "https://control.msg91.com/api/v5/widget/verifyAccessToken";
    
    public void verifyToken(String token) {

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> body = new HashMap<>();
            body.put("authkey", MSG91_AUTH_KEY);
            body.put("access-token", token);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            String response = restTemplate.postForObject(VERIFY_URL, request, String.class);
            if (response != null && response.toLowerCase().contains("\"type\":\"error\"")) {
                throw new RuntimeException("MSG91 returned error: " + response);
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("MSG91 HTTP Error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Verification failed: " + e.getMessage());
        }
    }
}
