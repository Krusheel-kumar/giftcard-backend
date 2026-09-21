package com.popobob.giftcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GiftcardBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GiftcardBackendApplication.class, args);
	}

}
