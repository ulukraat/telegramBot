package com.example.Telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.time.LocalDateTime;

@SpringBootApplication
public class TelegramApplication {

	public static void main(String[] args) throws IOException {
		HttpServerStub.startServer();
		SpringApplication.run(TelegramApplication.class, args);
	}

}
