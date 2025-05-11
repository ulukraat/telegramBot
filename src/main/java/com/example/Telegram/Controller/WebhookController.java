    package com.example.Telegram.Controller;

    import com.example.Telegram.service.TelegramBot;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import org.telegram.telegrambots.meta.api.objects.Update;

    @RestController
    @RequestMapping("/webhook")
    public class WebhookController {
        private final TelegramBot telegramBot;

        public WebhookController(TelegramBot telegramBot) {
            this.telegramBot = telegramBot;
        }

        @PostMapping
        public ResponseEntity<?> onUpdateReceived(@RequestBody Update update) {
            telegramBot.onWebhookUpdateReceived(update);
            return ResponseEntity.ok("OK");
        }
    }
