package com.popobob.giftcard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.*;

@Service
public class WhatsAppService {
    @Value("${msg91.auth-key}")
    private String MSG91_AUTH_KEY;
    private static final String WHATSAPP_API_URL = "https://api.msg91.com/api/v5/whatsapp/whatsapp-outbound-message/bulk/";
    
    @Async
    public void sendGiftCard(String mobileNumber, String customerName, String bogoCode) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authkey", MSG91_AUTH_KEY);
            
            String formattedPhone = mobileNumber.replaceAll("[^0-9]", "");
            if (!formattedPhone.startsWith("91") && formattedPhone.length() == 10) {
                formattedPhone = "91" + formattedPhone;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("integrated_number", "917794971935");
            payload.put("content_type", "template");
            
            Map<String, Object> innerPayload = new HashMap<>();
            innerPayload.put("messaging_product", "whatsapp");
            innerPayload.put("type", "template");
            
            Map<String, Object> template = new HashMap<>();
            template.put("name", "gift_card_delivery");
            
            Map<String, String> language = new HashMap<>();
            language.put("code", "en");
            language.put("policy", "deterministic");
            template.put("language", language);
            template.put("namespace", "a5a7cc47_8d2f_438b_ae71_db1e67e3edbe");
            
            List<Map<String, Object>> componentsList = new ArrayList<>();
            Map<String, Object> componentsWrapper = new HashMap<>();
            componentsWrapper.put("to", Collections.singletonList(formattedPhone));
            
            Map<String, Object> components = new HashMap<>();
            
            // Header Image
            Map<String, String> header1 = new HashMap<>();
            header1.put("type", "image");
            header1.put("value", "https://raw.githubusercontent.com/Krusheel-kumar/giftcard-customer-ui/main/src/assets/rakshilandingpage.png"); 
            components.put("header_1", header1);
            
            // Body Variables
            components.put("body_1", Map.of("type", "text", "value", customerName != null ? customerName : "Valued Customer"));
            components.put("body_2", Map.of("type", "text", "value", "Buy 1 Get 1 Free"));
            components.put("body_3", Map.of("type", "text", "value", bogoCode));
            components.put("body_4", Map.of("type", "text", "value", "31st August"));
            
            componentsWrapper.put("components", components);
            componentsList.add(componentsWrapper);
            
            template.put("to_and_components", componentsList);
            innerPayload.put("template", template);
            payload.put("payload", innerPayload);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            String response = restTemplate.postForObject(WHATSAPP_API_URL, request, String.class);
            System.out.println("WhatsApp MSG91 Response: " + response);
            
        } catch (Exception e) {
            System.err.println("Failed to send WhatsApp message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
