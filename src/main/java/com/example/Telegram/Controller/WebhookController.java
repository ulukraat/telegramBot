package com.example.Telegram.Controller;

import com.example.Telegram.service.TelegramBot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/")
public class WebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final TelegramBot telegramBot;

    public WebhookController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping
    public ResponseEntity<?> onUpdateReceived(@RequestBody Update update) {
        logger.info("Получено обновление: {}", update);
        telegramBot.onWebhookUpdateReceived(update);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Бот работает!");
    }

}
