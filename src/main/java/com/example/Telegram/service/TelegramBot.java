package com.example.Telegram.service;

import com.example.Telegram.config.BotConfig;
import com.example.Telegram.model.Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
        log.info("TelegramBot инициализирован с токеном: {}", config.getToken().substring(0, 5) + "...");
    }

    // Геттер для получения списка активных пользователей
    public Map<Long, Boolean> getActiveUsers() {
        return activeUsers;
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
                    log.info("Пользователь {} добавлен в список активных пользователей. Текущие активные пользователи: {}",
                            userChatId, activeUsers.keySet());
                    return startCommandReceived(userChatId, update.getMessage().getChat().getFirstName());
                case "/test":
                    // Тестовая команда для проверки отправки сообщений
                    log.info("Запущена тестовая отправка сообщения пользователю {}", userChatId);
                    SendMessage testMessage = new SendMessage();
                    testMessage.setChatId(String.valueOf(userChatId));
                    testMessage.setText("Тестовое сообщение. Бот работает!");
                    executeMessage(testMessage);
                    return sendMessage(userChatId, "Тест выполнен");
                case "/sendnight":
                    // Команда для ручного запуска отправки ночных пожеланий
                    log.info("Запущена ручная отправка ночных пожеланий для пользователя {}", userChatId);
                    sendNightWishToUser(userChatId);
                    return sendMessage(userChatId, "Команда выполнена");
                case "/users":
                    // Показать всех активных пользователей
                    return sendMessage(userChatId, "Активные пользователи: " + activeUsers.keySet());
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
    private void executeMessage(SendMessage message) {
        try {
            log.info("Отправка сообщения пользователю {}: '{}'",
                    message.getChatId(), message.getText().substring(0, Math.min(20, message.getText().length())) + "...");
            execute(message);
            log.info("Сообщение успешно отправлено пользователю: {}", message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения пользователю {}: {}", message.getChatId(), e.getMessage());
            e.printStackTrace(); // Более детальный лог ошибки
        }
    }

    // Метод для отправки ночного пожелания конкретному пользователю
    private void sendNightWishToUser(long userId) {
        List<String> nightWishes = repository.getNightWish();
        if (nightWishes == null || nightWishes.isEmpty()) {
            log.warn("В списке нет ночных пожеланий.");
            return;
        }

        log.info("Список ночных пожеланий содержит {} элементов", nightWishes.size());

        if (currentWishIndex >= nightWishes.size()) {
            currentWishIndex = 0;
            log.info("Индекс пожелания сброшен на 0, так как превысил размер списка");
        }

        String todayWish = nightWishes.get(currentWishIndex);
        log.info("Выбрано пожелание с индексом {}: '{}'",
                currentWishIndex, todayWish.substring(0, Math.min(30, todayWish.length())) + "...");

        try {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(userId));
            message.setText(todayWish);
            executeMessage(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке ночного пожелания пользователю {}: {}", userId, e.getMessage());
            e.printStackTrace();
        }
    }

    // Запланированная задача для логирования списка активных пользователей
    @Scheduled(cron = "0 */5 * * * ?") // Каждые 5 минут
    public void logActiveUsers() {
        log.info("Текущие активные пользователи: {}", activeUsers.keySet());
        log.info("Общее количество активных пользователей: {}", activeUsers.size());
    }

    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Bishkek")
    public void sendMorningWishes() {
        log.info("Запуск запланированной задачи отправки утренних пожеланий");
        List<String> morningWishes = repository.getMorningWish();
        if (morningWishes == null || morningWishes.isEmpty()) {
            log.warn("В списке нет утренних пожеланий.");
            return;
        }

        log.info("Список утренних пожеланий содержит {} элементов", morningWishes.size());
        log.info("Начало отправки утренних пожеланий. Активных пользователей: {}", activeUsers.size());

        if (activeUsers.isEmpty()) {
            log.warn("Нет активных пользователей для отправки сообщений");
            return;
        }

        if (currentWishIndex >= morningWishes.size()) {
            currentWishIndex = 0;
            log.info("Индекс пожелания сброшен на 0, так как превысил размер списка");
        }

        String todayWish = morningWishes.get(currentWishIndex);
        log.info("Выбрано пожелание с индексом {}: '{}'",
                currentWishIndex, todayWish.substring(0, Math.min(30, todayWish.length())) + "...");

        for (Long userId : activeUsers.keySet()) {
            try {
                log.info("Отправка утреннего пожелания пользователю {}", userId);
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(userId));
                message.setText(todayWish);
                executeMessage(message);
            } catch (Exception e) {
                log.error("Ошибка при отправке утреннего пожелания пользователю {}: {}", userId, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Scheduled(cron = "0 40 00 * * ?", zone = "Asia/Bishkek")
    public void sendNightWishes() {
        log.info("======== ЗАПУСК ЗАПЛАНИРОВАННОЙ ЗАДАЧИ ОТПРАВКИ НОЧНЫХ ПОЖЕЛАНИЙ ========");
        List<String> nightWishes = repository.getNightWish();

        if (nightWishes == null) {
            log.error("Список ночных пожеланий равен null!");
            return;
        }

        if (nightWishes.isEmpty()) {
            log.warn("В списке нет ночных пожеланий (пустой список).");
            return;
        }

        log.info("Список ночных пожеланий содержит {} элементов", nightWishes.size());
        log.info("Начало отправки ночных пожеланий. Активных пользователей: {}", activeUsers.size());

        if (activeUsers.isEmpty()) {
            log.warn("Нет активных пользователей для отправки сообщений");
            return;
        }

        log.info("Список активных пользователей: {}", activeUsers.keySet());

        if (currentWishIndex >= nightWishes.size()) {
            currentWishIndex = 0;
            log.info("Индекс пожелания сброшен на 0, так как превысил размер списка");
        }

        String todayWish = nightWishes.get(currentWishIndex);
        log.info("Выбрано пожелание с индексом {}: '{}'",
                currentWishIndex, todayWish.substring(0, Math.min(30, todayWish.length())) + "...");

        for (Long userId : activeUsers.keySet()) {
            try {
                log.info("Отправка ночного пожелания пользователю {}", userId);
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(userId));
                message.setText(todayWish);
                executeMessage(message);
            } catch (Exception e) {
                log.error("Ошибка при отправке ночного пожелания пользователю {}: {}", userId, e.getMessage());
                e.printStackTrace();
            }
        }

        currentWishIndex = (currentWishIndex + 1) % Math.max(1, nightWishes.size());
        log.info("Текущий индекс пожелания обновлен до: {}", currentWishIndex);
        log.info("======== ЗАВЕРШЕНИЕ ОТПРАВКИ НОЧНЫХ ПОЖЕЛАНИЙ ========");
    }
}