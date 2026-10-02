
import java.util.*;
import java.net.*;
import java.io.*;

public class TestWhatsApp {
    public static void main(String[] args) throws Exception {
        String MSG91_AUTH_KEY = "557539A3jnNLJWr6a73367fP1";
        String WHATSAPP_API_URL = "https://api.msg91.com/api/v5/whatsapp/whatsapp-outbound-message/bulk/";

        String mobileNumber = "7794971935";
        String customerName = "Test User";
        String rewardName = "Buy 1 Get 1";
        String bogoCode = "TEST-CODE";
        String expiryDate = "2026-10-10";

        String formattedPhone = mobileNumber.replaceAll("[^0-9]", "");
        if (!formattedPhone.startsWith("91") && formattedPhone.length() == 10) {
            formattedPhone = "91" + formattedPhone;
        }

        String imageUrl = "https://raw.githubusercontent.com/Krusheel-kumar/giftcard-customer-ui/main/src/assets/buyonegetone.jpeg";

        String payload = String.format("{\"integrated_number\":\"917794971935\",\"content_type\":\"template\",\"payload\":{\"messaging_product\":\"whatsapp\",\"type\":\"template\",\"template\":{\"name\":\"gift_card_delivery\",\"language\":{\"code\":\"en\",\"policy\":\"deterministic\"},\"to_and_components\":[{\"to\":[\"%s\"],\"components\":{\"header_1\":{\"type\":\"image\",\"value\":\"%s\"},\"body_1\":{\"type\":\"text\",\"value\":\"%s\"},\"body_2\":{\"type\":\"text\",\"value\":\"%s\"},\"body_3\":{\"type\":\"text\",\"value\":\"%s\"},\"body_4\":{\"type\":\"text\",\"value\":\"%s\"}}}]}}}", 
            formattedPhone, imageUrl, customerName, rewardName, bogoCode, expiryDate);

        URL url = new URL(WHATSAPP_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("authkey", MSG91_AUTH_KEY);
        conn.setDoOutput(true);

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        System.out.println("Response Code: " + code);
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(
                code >= 400 ? conn.getErrorStream() : conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }
    }
}

