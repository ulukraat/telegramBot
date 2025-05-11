package com.example.Telegram.service;

import com.example.Telegram.config.BotConfig;
import com.example.Telegram.model.Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EnableScheduling
@Component
@Slf4j
public class TelegramBot extends TelegramWebhookBot {
    final Repository repository;
    final BotConfig config;
    private final Map<Long, Boolean> activeUsers = new ConcurrentHashMap<>();
    private int currentWishIndex = 0;

    public TelegramBot(BotConfig config, Repository repository) {
        this.config = config;
        this.repository = repository;
    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotPath() {
        return "/webhook";
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long userChatId = update.getMessage().getChatId();

            switch (message) {
                case "/start":
                    activeUsers.put(userChatId, true);
                    return startCommandReceived(userChatId, update.getMessage().getChat().getFirstName());
                default:
                    return sendMessage(userChatId, "\uD83D\uDCAB");
            }
        }
        return null;
    }

    private BotApiMethod<?> startCommandReceived(long chatId, String name) {
        String answer = "Привет Амира ,решил создать такого простого бота который будет тебе желать доброго утра и спокойной ночи,(каждый день желать в отличие от меня)\uD83D\uDC8C ";
        log.info("Ответ пользователю: " + name);
        return sendMessage(chatId, answer);
    }

    private BotApiMethod<?> sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        return sendMessage;
    }

    // 🔥 **Запланированные отправки пожеланий**
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Bishkek")
    public void sendMorningWishes() {
        List<String> morningWishes = repository.getMorningWish();
        if (morningWishes.isEmpty()) {
            log.info("В списке нет утренних пожеланий.");
            return;
        }

        String todayWish = morningWishes.get(currentWishIndex);
        for (Long userId : activeUsers.keySet()) {
            sendMessage(userId, todayWish);
            log.info("Утреннее пожелание отправлено.");
        }
    }

    @Scheduled(cron = "0 0 23 * * ?", zone = "Asia/Bishkek")
    public void sendNightWishes() {
        List<String> nightWishes = repository.getNightWish();
        if (nightWishes.isEmpty()) {
            log.info("В списке нет ночных пожеланий.");
            return;
        }

        String todayWish = nightWishes.get(currentWishIndex);
        for (Long userId : activeUsers.keySet()) {
            sendMessage(userId, todayWish);
            log.info("Ночное пожелание отправлено.");
        }

        currentWishIndex = (currentWishIndex + 1) % nightWishes.size();
    }
}
