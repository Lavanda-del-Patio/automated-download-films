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
    // @Scheduled(fixedDelay = 1000000)
    public void sendTorrentsToCheck() {
        log.info("Task scheduled sendTorrentsToCheck at midnight");
        filmsServiceImpl.checkTorrents();
    }
}
