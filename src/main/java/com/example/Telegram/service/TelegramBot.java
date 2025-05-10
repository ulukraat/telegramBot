package com.example.Telegram.service;

import com.example.Telegram.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;

    private TelegramBot(BotConfig config) {
        this.config = config;
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
            long chatId = update.getMessage().getChatId();
            switch(message){
                case "/start":
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName());
                    break;
                default:
                    sendMessage(chatId,"fsdfds");
            }
        }
    }
    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет уцлуц";
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
}
