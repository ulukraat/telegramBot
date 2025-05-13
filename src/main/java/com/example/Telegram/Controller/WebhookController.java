package com.example.Telegram.Controller;

import com.example.Telegram.service.TelegramBot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/webhook")
public class WebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final TelegramBot telegramBot;

    public WebhookController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping
    public ResponseEntity<?> onWebhookUpdateReceived(@RequestBody Update update) {
        try {
            logger.info("Получено обновление через webhook: {}", update);
            BotApiMethod<?> response = telegramBot.onWebhookUpdateReceived(update);

            // Если есть ответ, возвращаем его
            if (response != null) {
                return ResponseEntity.ok(response);
            }

            // Иначе просто подтверждаем получение
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка обработки webhook: ", e);
            return ResponseEntity.status(500).body("Ошибка обработки webhook: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<String> webhookHealthCheck() {
        return ResponseEntity.ok("Webhook работает!");
    }
}