package com.example.Telegram.service;

import com.example.Telegram.config.BotConfig;
import com.example.Telegram.model.Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        log.info("Телеграм бот инициализирован");
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
            log.info("Получено сообщение: '{}' от пользователя: {}", message, userChatId);

            switch (message) {
                case "/start":
                    activeUsers.put(userChatId, true);
                    log.info("Пользователь {} добавлен в список активных пользователей", userChatId);
                    return startCommandReceived(userChatId, update.getMessage().getChat().getFirstName());
                default:
                    return sendMessage(userChatId, "\uD83D\uDCAB");
            }
        }
        return null;
    }

    private BotApiMethod<?> startCommandReceived(long chatId, String name) {
        String answer = "Привет Амира, решил создать такого простого бота который будет тебе желать доброго утра и спокойной ночи, (каждый день желать в отличие от меня)\uD83D\uDC8C ";
        log.info("Ответ пользователю: " + name);
        return sendMessage(chatId, answer);
    }

    private BotApiMethod<?> sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        return sendMessage;
    }

    // Метод для фактической отправки сообщений
    public void executeMessage(SendMessage message) {
        try {
            execute(message);
            log.info("Сообщение успешно отправлено пользователю: {}", message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения пользователю {}: {}", message.getChatId(), e.getMessage());
        }
    }

    // Публичные методы для вызова из SchedulerService
    public void sendMorningWishes() {
        List<String> morningWishes = repository.getMorningWish();
        if (morningWishes == null || morningWishes.isEmpty()) {
            log.warn("В списке нет утренних пожеланий.");
            return;
        }

        log.info("Начало отправки утренних пожеланий. Активных пользователей: {}", activeUsers.size());

        if (currentWishIndex >= morningWishes.size()) {
            currentWishIndex = 0;
        }

        String todayWish = morningWishes.get(currentWishIndex);
        for (Long userId : activeUsers.keySet()) {
            try {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(userId));
                message.setText(todayWish);
                executeMessage(message);
            } catch (Exception e) {
                log.error("Ошибка при отправке утреннего пожелания пользователю {}: {}", userId, e.getMessage());
            }
        }
    }

    public void sendNightWishes() {
        List<String> nightWishes = repository.getNightWish();
        if (nightWishes == null || nightWishes.isEmpty()) {
            log.warn("В списке нет ночных пожеланий.");
            return;
        }

        log.info("Начало отправки ночных пожеланий. Активных пользователей: {}", activeUsers.size());

        if (currentWishIndex >= nightWishes.size()) {
            currentWishIndex = 0;
        }

        String todayWish = nightWishes.get(currentWishIndex);
        for (Long userId : activeUsers.keySet()) {
            try {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(userId));
                message.setText(todayWish);
                executeMessage(message);
            } catch (Exception e) {
                log.error("Ошибка при отправке ночного пожелания пользователю {}: {}", userId, e.getMessage());
            }
        }

        currentWishIndex = (currentWishIndex + 1) % Math.max(1, nightWishes.size());
        log.info("Текущий индекс пожелания обновлен до: {}", currentWishIndex);
    }
}