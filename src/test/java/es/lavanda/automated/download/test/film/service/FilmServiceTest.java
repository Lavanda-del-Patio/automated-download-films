package com.lavanda.automated.download.test.films.service;

import com.lavanda.automated.download.films.repository.FilmModelRepository;
import com.lavanda.automated.download.films.service.FilmsServiceImpl;
import com.lavanda.automated.download.films.service.ProducerService;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class FilmServiceTest {

        @InjectMocks
        private FilmsServiceImpl showsServiceImpl;

        @Mock
        private FilmModelRepository showModelRepository;

        @Mock
        private ProducerService producerService;

        @Test
        @SneakyThrows
        @Disabled
        public void test() {
                // FilmModel showModelToDownload = new FilmModel();
                // showModelToDownload.setImage("https://pctfenix.com/uploads/i/thumbs/0_1583302664-Blindspot.jpg");
                // showModelToDownload.setTitle("blindspot");
                // Assertions.assertNotNull(showsServiceImpl);

                // List<FilmModel> showModels = new ArrayList<>();
                // showModels.add(new FilmModel("12315-123562", "Blindspot", null, null));
                // when(showModelRepository.findAll()).thenReturn(showModels);
                // showsServiceImpl.executeShow(showModelToDownload);
        }
}
