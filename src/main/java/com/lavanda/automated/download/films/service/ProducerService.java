package com.lavanda.automated.download.films.service;

import com.lavanda.automated.download.films.exception.AutomatedDownloadFilmsException;

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

    public void sendTorrent(Object message) throws AutomatedDownloadFilmsException {
        try {
            log.debug("Sending message to queue transmission {}", message);
            messagingTemplate.convertAndSend(QUEUE_TRANSMISSION, message);
            log.debug("Sended message to queue transmission");
        } catch (Exception e) {
            log.error("Failed send message to queue transmission", e);
            throw new AutomatedDownloadFilmsException("Failed send message to queue transmission", e);
        }
    }

    public void sendToFeedAgentTMDB(Object message) throws AutomatedDownloadFilmsException {
        try {
            log.info("Sending message to queue agent-tmdb-feed-films {}", message);
            rabbitTemplate.convertAndSend("agent-tmdb-feed-films", message);
            log.debug("Sended message to queue agent-tmdb-feed-films");
        } catch (Exception e) {
            log.error("Failed send message to queue agent-tmdb-feed-films", e);
            throw new AutomatedDownloadFilmsException("Failed send message to queue agent-tmdb-feed-films", e);
        }
    }
}
