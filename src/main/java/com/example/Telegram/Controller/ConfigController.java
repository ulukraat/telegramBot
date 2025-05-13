package com.example.Telegram.Controller;

import com.example.Telegram.service.TelegramBot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/")
public class ConfigController {
    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);
    private final TelegramBot telegramBot;

    public ConfigController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    // Заглушка для Google Cloud
    @GetMapping("/")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Бот работает!");
    }


    // ВАЖНО: Так как это заглушка, возможно Telegram отправляет на корневой путь
    // Добавляем обработку и для корневого пути
    @PostMapping("/")
    public ResponseEntity<?> onRootUpdateReceived(@RequestBody Update update) {
        try {
            logger.info("Получено обновление на корневом пути: {}", update);
            // Проверяем в логах, приходят ли сюда запросы от Telegram
            logger.info("Информация о сообщении: {}",
                    update.hasMessage() ? update.getMessage().getText() : "Нет сообщения");

            BotApiMethod<?> response = telegramBot.onWebhookUpdateReceived(update);

            if (response != null) {
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            logger.error("Ошибка обработки запроса: ", e);
            return ResponseEntity.status(500).body("Ошибка сервера: " + e.getMessage());
        }
    }
}