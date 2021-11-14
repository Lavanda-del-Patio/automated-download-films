package es.lavanda.automated.download.film.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.lavanda.automated.download.film.exception.AutomatedDownloadFilmsException;
import es.lavanda.automated.download.film.model.LambdaDTO;
import es.lavanda.automated.download.film.model.TorrentCheckedResponse;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

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

    @SqsListener(value = "feed-films-${spring.profiles.active}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void consumeMessage(String lambdaDestination) throws AutomatedDownloadFilmsException {
        log.info("Reading message of the queue feed-films: {}", lambdaDestination);
        ObjectMapper mapper = new ObjectMapper();
        LambdaDTO lambda = new LambdaDTO();
        try {
            lambda = mapper.readValue(lambdaDestination, LambdaDTO.class);
        } catch (JsonProcessingException e) {
            log.error("The message cannot convert to FilmModelTorrent", e);
            throw new AutomatedDownloadFilmsException("The message cannot convert to FilmModelTorrent", e);
        }
        lambda.getFilmModelTorrents().forEach(filmsServiceImpl::executeFilm);
        log.debug("Work message finished");
    }

    @SqsListener(value = "torrent-checked-${spring.profiles.active}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void consumeTorrentChecked(String string) throws AutomatedDownloadFilmsException {
        log.info("Reading message of the queue torrent-checked");
        ObjectMapper mapper = new ObjectMapper();
        TorrentCheckedResponse torrentChecked = new TorrentCheckedResponse();
        try {
            torrentChecked = mapper.readValue(string, TorrentCheckedResponse.class);
        } catch (JsonProcessingException e) {
            log.error("The message cannot convert to FilmModelTorrent", e);
            throw new AutomatedDownloadFilmsException("The message cannot convert to FilmModelTorrent", e);
        }
        filmsServiceImpl.checkedTorrent(torrentChecked);
        log.debug("Work message finished");
    }

}
