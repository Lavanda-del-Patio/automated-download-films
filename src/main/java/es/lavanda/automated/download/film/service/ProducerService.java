package es.lavanda.automated.download.film.service;

import es.lavanda.automated.download.film.exception.AutomatedDownloadFilmsException;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProducerService {

    private final QueueMessagingTemplate messagingTemplate;

    private final RabbitTemplate rabbitTemplate;

    @Value("${cloud.aws.sqs.endpoint.uri.transmission}-${spring.profiles.active}")
    private String QUEUE_TRANSMISSION;

    @Value("${cloud.aws.sqs.endpoint.uri.torrent-check}-${spring.profiles.active}")
    private String QUEUE_TORRENT_CHECK;

    public void sendTorrentToDownload(Object message) throws AutomatedDownloadFilmsException {
        try {
            log.debug("Sending message to queue transmission {}", message);
            messagingTemplate.convertAndSend(QUEUE_TRANSMISSION, message);
            log.debug("Sended message to queue transmission");
        } catch (Exception e) {
            log.error("Failed send message to queue transmission", e);
            throw new AutomatedDownloadFilmsException("Failed send message to queue transmission", e);
        }
    }

    public void sendTorrentToValidate(Object message) throws AutomatedDownloadFilmsException {
        try {
            log.debug("Sending message to queue torrent-check {}", message);
            messagingTemplate.convertAndSend(QUEUE_TORRENT_CHECK, message);
            log.debug("Sended message to queue torrent-check");
        } catch (Exception e) {
            log.error("Failed send message to queue torrent-check", e);
            throw new AutomatedDownloadFilmsException("Failed send message to queue torrent-check", e);
        }
    }

    public void sendToFeedAgentTMDB(Object message) throws AutomatedDownloadFilmsException {
        try {
            log.info("Sending message to queue agent-tmdb-feed-films {}", message);
            rabbitTemplate.convertAndSend("agent-tmdb-feed-films", message);
            log.info("Sended message to queue agent-tmdb-feed-films {}", message);
        } catch (Exception e) {
            log.error("Failed send message to queue agent-tmdb-feed-films", e);
            throw new AutomatedDownloadFilmsException("Failed send message to queue agent-tmdb-feed-films", e);
        }
    }
}
