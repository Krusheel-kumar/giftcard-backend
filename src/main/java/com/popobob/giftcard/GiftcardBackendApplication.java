package com.popobob.giftcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class GiftcardBackendApplication {

    @PostConstruct
    public void init() {
        // Force the entire application (and database timestamps) to use Indian Standard Time
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

	public static void main(String[] args) {
		SpringApplication.run(GiftcardBackendApplication.class, args);
	}

}
