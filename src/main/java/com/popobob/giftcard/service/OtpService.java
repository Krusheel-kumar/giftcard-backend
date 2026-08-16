package com.popobob.giftcard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@Service
public class OtpService {
    @Value("${msg91.auth-key}")
    private String MSG91_AUTH_KEY;

    private static final String VERIFY_URL = "https://control.msg91.com/api/v5/widget/verifyAccessToken";
    
    public String verifyToken(String token) {

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> body = new HashMap<>();
            body.put("authkey", MSG91_AUTH_KEY);
            body.put("access-token", token);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(VERIFY_URL, request, Map.class);
            Map<String, Object> response = responseEntity.getBody();
            
            if (response != null && "error".equalsIgnoreCase((String) response.get("type"))) {
                throw new RuntimeException("MSG91 returned error: " + response);
            }
            
            if (response != null && response.containsKey("mobile")) {
                return String.valueOf(response.get("mobile"));
            } else {
                throw new RuntimeException("MSG91 response missing mobile number.");
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("MSG91 HTTP Error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Verification failed: " + e.getMessage());
        }
    }
}
