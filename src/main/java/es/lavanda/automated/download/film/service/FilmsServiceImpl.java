package es.lavanda.automated.download.film.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import es.lavanda.automated.download.film.exception.AutomatedDownloadFilmsException;
import es.lavanda.automated.download.film.model.FilmModel;
import es.lavanda.automated.download.film.model.TransmissionModelRequest;
import es.lavanda.automated.download.film.repository.FilmModelRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import es.lavanda.lib.common.model.FilmModelTorrent;
import es.lavanda.lib.common.model.MediaIDTO;
import es.lavanda.lib.common.model.MediaIDTO.Type;
import es.lavanda.lib.common.model.MediaODTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmsServiceImpl {

    private final FilmModelRepository filmModelRepository;

    private final ProducerService producerService;

    public Page<FilmModel> getAllFilms(Pageable pageable) {
        return filmModelRepository.findAll(pageable);
    }

    public FilmModel getFilmByTitle(String title) {
        log.debug("Finding film by title");
        return filmModelRepository.findByTitle(title)
                .orElseThrow(() -> new AutomatedDownloadFilmsException("Not found film by title"));
    }

    public FilmModel save(FilmModel filmModel) {
        return filmModelRepository.save(filmModel);
    }

    public FilmModel updateFilm(String filmModelId, FilmModel filmModel) {
        getFilm(filmModelId,false);
        FilmModel filmModelUpdated = save(filmModel);
        sendToAgent(filmModelUpdated);
        return filmModelUpdated;
    }

    public void deleteFilmById(String id) {
        filmModelRepository.deleteById(id);
    }

    public List<FilmModel> getLastFilms() {
        log.debug("getThreeLastFilms");
        return filmModelRepository.findTop6ByOrderByCreatedAtDesc();
    }

    public Page<FilmModel> getAllFilmsOrderedByCreated(Pageable pageable) {
        return filmModelRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<FilmModel> getAllFilmsByTorrentTitle(String title) {
        log.debug("Finding film by title");
        return filmModelRepository.findAllByTitle(title);
    }

    public FilmModel getFilm(String id, boolean force) {
        log.info("Finding film with id {}", id);
        Optional<FilmModel> optShowModel = filmModelRepository.findById(id);
        if (!optShowModel.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
        }
        if (force) {
            log.info("Force update metadata for  film {}", id);
            sendToAgent(optShowModel.get());
        }
        return optShowModel.get();
    }

    public FilmModel createFilm(FilmModel showModel) {
        return filmModelRepository.save(showModel);
    }

    public FilmModel editFilm(FilmModel showModel) {
        return filmModelRepository.save(showModel);
    }

    // public void cleanDuplicates() {
    // Iterable<FilmModel> withDuplicatesIterable = filmModelRepository.findAll();
    // List<FilmModel> withDuplicates = new ArrayList<>();
    // withDuplicatesIterable.forEach(withDuplicates::add);
    // List<FilmModel> withoutDuplicates = new ArrayList<>();
    // for (FilmModel filmModel : withDuplicates) {
    // boolean toAggregate = true;
    // for (FilmModel filmModel2 : withoutDuplicates) {
    // if (Objects.nonNull(filmModel.getTorrentUrl()) &&
    // Objects.nonNull(filmModel2.getTorrentUrl())
    // && filmModel2.getTorrentUrl().equals(filmModel.getTorrentUrl())) {
    // toAggregate = false;
    // }
    // }
    // if (toAggregate) {
    // withoutDuplicates.add(filmModel);
    // }
    // }
    // log.info("Tamaño withDuplicates {}", withDuplicates.size());
    // log.info("Tamaño withoutDuplicates {}", withoutDuplicates.size());
    // filmModelRepository.deleteAll();
    // filmModelRepository.saveAll(withoutDuplicates);
    // }

    public void executeFilm(FilmModelTorrent filmModelTorrent) {
        log.info("Execute film {}", filmModelTorrent.toString());
        if (Boolean.FALSE.equals(filmModelRepository.existsByTorrentsTorrentUrl(filmModelTorrent.getTorrentUrl()))) {
            try {
                createNewFilmModel(filmModelTorrent);
            } catch (AutomatedDownloadFilmsException e) {
                log.error("Not sended torrent to agent", e);
                throw e;
            }
        }
    }

    private void createNewFilmModel(FilmModelTorrent filmModelTorrent) {
        FilmModel filmModel = new FilmModel();
        filmModel.getTorrents().add(filmModelTorrent);
        // filmModel.setTorrentCroppedTitle(filmModelTorrent.getTorrentCroppedTitle());
        // filmModel.setTorrentUrl(filmModelTorrent.getTorrentUrl());
        // filmModel.setTorrentImage(filmModelTorrent.getTorrentImage());
        // filmModel.setTorrentTitle(filmModelTorrent.getTorrentTitle());
        // filmModel.setTorrentSize(filmModelTorrent.getTorrentSize());
        // filmModel.setTorrentQuality(filmModelTorrent.getTorrentQuality());
        // filmModel.setTorrentPage(filmModelTorrent.getTorrentPage());
        // filmModel.setTorrentYear(filmModelTorrent.getTorrentYear());
        // filmModel.setType(null);
        sendToAgent(save(filmModel));
    }

    private void sendToAgent(FilmModel filmModel) {
        MediaIDTO mediaIDTO = new MediaIDTO();
        mediaIDTO.setId(filmModel.getId());
        String torrentCroppedTitle = filmModel.getTorrents().stream()
                .filter(torrent -> StringUtils.hasText(torrent.getTorrentCroppedTitle()))
                .map(FilmModelTorrent::getTorrentCroppedTitle).findFirst().orElse(null);
        if (Objects.nonNull(torrentCroppedTitle)) {
            mediaIDTO.setPossibleType(getPossibleType(torrentCroppedTitle));
        }
        mediaIDTO.setTorrentCroppedTitle(torrentCroppedTitle);
        mediaIDTO.setTorrentTitle(
                filmModel.getTorrents().stream().filter(torrent -> StringUtils.hasText(torrent.getTorrentTitle()))
                        .map(FilmModelTorrent::getTorrentTitle).findFirst().orElse(null));
        mediaIDTO.setTorrentYear(filmModel.getTorrents().stream().filter(torrent -> torrent.getTorrentYear() >= 0)
                .map(FilmModelTorrent::getTorrentYear).findFirst().orElse(null));
        if (Objects.nonNull(filmModel.getType())) {
            mediaIDTO.setType(filmModel.getType());
        }
        producerService.sendToFeedAgentTMDB(mediaIDTO);
    }

    private Type getPossibleType(String torrentCroppedTitle) {
        return torrentCroppedTitle.contains("Temporada") ? Type.SHOW : Type.FILM;
    }

    public void updateLibrary() {
        Iterable<FilmModel> withDuplicatesIterable = filmModelRepository.findAll();
        List<FilmModel> filmModels = new ArrayList<>();
        withDuplicatesIterable.forEach(filmModels::add);
        for (FilmModel filmModel : filmModels) {
            sendToAgent(filmModel);
            // filmModelRepository.save(filmModel);
            // updateFilm(filmModel);
        }
    }

    public void updateFilmWithMediaDTO(MediaODTO mediaODTO) {
        FilmModel filmModel = getFilm(mediaODTO.getId(),false);
        List<FilmModel> filmModelsWithSameOriginalId = filmModelRepository.findByIdOriginal(mediaODTO.getIdOriginal());
        for (FilmModel filmModelIterator : filmModelsWithSameOriginalId) {
            filmModel.getTorrents().addAll(filmModelIterator.getTorrents());
            filmModelRepository.deleteAll(filmModelsWithSameOriginalId);
        }
        log.info("Update film {} with id {}", filmModel.getTorrents().stream().map(torrent -> torrent.getTorrentTitle())
                .findFirst().orElse("NO NAME"), filmModel.getId());
        filmModel.setImage(mediaODTO.getImage());
        filmModel.setTitle(mediaODTO.getTitle());
        filmModel.setTitleOriginal(mediaODTO.getTitleOriginal());
        filmModel.setReleaseDate(mediaODTO.getReleaseDate());
        filmModel.setIdOriginal(mediaODTO.getIdOriginal());
        filmModel.setBackdropImage(mediaODTO.getBackdropImage());
        filmModel.setOverview(mediaODTO.getOverview());
        filmModel.setVoteAverage(mediaODTO.getVoteAverage());
        save(filmModel);
    }

    public FilmModelTorrent updateTorrent(String torrentId, FilmModelTorrent filmModelTorrent) {
        FilmModel filmModel = filmModelRepository.findByTorrentsTorrentId(torrentId).orElseThrow(
                () -> new AutomatedDownloadFilmsException("Not exists film with this torrentId " + torrentId));
        if (Objects.isNull(filmModel)) {
            throw new AutomatedDownloadFilmsException("Not exists film with this torrentId " + torrentId);
        }
        if (Boolean.FALSE.equals(filmModelTorrent.isDownloaded())
                && Boolean.TRUE.equals(filmModelTorrent.isAssignToDownload())) {
            sendToDownloadTorrent(filmModelTorrent);
        }
        filmModel.getTorrents().remove(filmModelTorrent);
        filmModel.getTorrents().add(filmModelTorrent);
        filmModelRepository.save(filmModel);
        return filmModelTorrent;
    }

    public FilmModelTorrent deleteFilmByTorrentId(String torrentId) {
        FilmModel filmModel = filmModelRepository.findByTorrentsTorrentId(torrentId).orElseThrow(
                () -> new AutomatedDownloadFilmsException("Not exists film with this torrentId " + torrentId));
        FilmModelTorrent filmModelTorrent = filmModel.getTorrents().stream()
                .filter(torrent -> torrent.getTorrentId().toString().equals(torrentId)).findFirst().orElseThrow(
                        () -> new AutomatedDownloadFilmsException("Not exists film with this  torrentId " + torrentId));
        filmModel.getTorrents().remove(filmModelTorrent);
        filmModelRepository.save(filmModel);
        return filmModelTorrent;
    }

    private void sendToDownloadTorrent(FilmModelTorrent filmModel) {
        log.info("Checking if needs to be downloaded:  {}", filmModel.getTorrentUrl());
        producerService.sendTorrent(new TransmissionModelRequest(filmModel.getTorrentUrl()));
        filmModel.setDownloaded(true);
    }

    public List<FilmModel> searchFilms(String search) {
        return filmModelRepository.findByTitleContainsIgnoreCaseOrderByCreatedAtDesc(search);
    }

}