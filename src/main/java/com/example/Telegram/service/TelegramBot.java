package com.example.Telegram.service;

import com.example.Telegram.config.BotConfig;
import com.example.Telegram.model.Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EnableScheduling
@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    final Repository repository;
    final BotConfig config;
    private final Map<Long,Boolean> activeUsers = new ConcurrentHashMap<>();
    private int currentWishIndex = 0;

    private TelegramBot(BotConfig config, Repository repository) {
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
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
             long userChatId = update.getMessage().getChatId();
            switch(message){
                case "/start":
                    activeUsers.put(userChatId,true);
                    startCommandReceived(userChatId,update.getMessage().getChat().getFirstName());
                    break;
                default:
                    sendMessage(userChatId,"\uD83D\uDCAB");
                    log.info("Пользователем было отправлено [" + message + "]");
            }
        }
    }
    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет Амира ,решил создать такого простого бота который будет тебе желать доброго утра и спокойной ночи,(каждый день желать в отличие от меня)\uD83D\uDC8C";
        log.info("Replied to user :" + name);
        sendMessage(chatId, answer);

    }
    private void sendMessage(long chatId,String message) {
        SendMessage message1 = new SendMessage();
        message1.setChatId(String.valueOf(chatId));
        message1.setText(message);
        try {
            execute(message1);
        }catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Bishkek")
    public void sendMorningWishes(){
        List<String> mWishes = repository.getMorningWish();
        if (mWishes.isEmpty()) {
            log.info("В листе нет пожеланий");
            return;
        }
        String toDayWish = mWishes.get(currentWishIndex);
        for (Long userId : activeUsers.keySet()) {;
            sendMessage(userId,toDayWish);
            log.info("Пожелание отправлено");
        }
    }
    @Scheduled(cron = "0 0 12 * * ?", zone = "Asia/Bishkek")
    public void sendNightWishes(){
        List<String> nWishes = repository.getNightWish();
        if (nWishes.isEmpty()) {
            log.info("В листе нет пожеланий");
            return;
        }
        String toDayWish = nWishes.get(currentWishIndex);
        for (Long userId : activeUsers.keySet()) {
            sendMessage(userId,toDayWish);
            log.info("Пожелание отправлено");
        }
        currentWishIndex = (currentWishIndex + 1) % nWishes.size();
    }

}
