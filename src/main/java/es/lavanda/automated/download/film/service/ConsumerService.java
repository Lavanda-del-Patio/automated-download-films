package es.lavanda.automated.download.film.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import es.lavanda.automated.download.film.exception.AutomatedDownloadFilmsException;
import es.lavanda.lib.common.model.MediaODTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumerService {

    private final FilmsServiceImpl filmsServiceImpl;

    @RabbitListener(queues = "agent-tmdb-feed-films-resolution")
    public void consumeMessageTMDBFilmResolution(MediaODTO mediaODTO) throws AutomatedDownloadFilmsException {
        log.debug("Reading message of the queue agent-tmdb-feed-films-resolution: {}", mediaODTO);
        filmsServiceImpl.updateFilmWithMediaDTO(mediaODTO);
        log.debug("Work message finished");
    }


}
