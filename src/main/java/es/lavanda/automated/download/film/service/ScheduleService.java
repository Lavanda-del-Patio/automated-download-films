package es.lavanda.automated.download.film.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScheduleService {

    @Autowired
    private FilmsServiceImpl filmsServiceImpl;

    @Scheduled(cron = "@midnight")
    public void sendTorrentsToCheck() {
        log.info("Task scheduled sendTorrentsToCheck at midnight");
        filmsServiceImpl.checkTorrents();
        log.info("Finished Task scheduled sendTorrentsToCheck at midnight");
    }

    @Scheduled(cron = "@midnight")
    public void cleanEmptys() {
        log.info("Task scheduled cleanEmptys at midnight");
        filmsServiceImpl.cleanEmptys();
        log.info("Finished Task scheduled cleanEmptys at midnight");

    }
}
