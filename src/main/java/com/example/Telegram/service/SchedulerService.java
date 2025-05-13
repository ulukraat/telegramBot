package com.example.Telegram.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulerService {

    private final TelegramBot telegramBot;

    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Bishkek")
    public void scheduleMorningWishes() {
        log.info("Запуск запланированной задачи отправки утренних пожеланий");
        telegramBot.sendMorningWishes();
    }

    @Scheduled(cron = "0 25 23 * * ?", zone = "Asia/Bishkek")
    public void scheduleNightWishes() {
        log.info("Запуск запланированной задачи отправки ночных пожеланий");
        telegramBot.sendNightWishes();
    }
}