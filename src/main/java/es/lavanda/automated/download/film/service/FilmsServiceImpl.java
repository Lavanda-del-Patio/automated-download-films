package es.lavanda.automated.download.film.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import es.lavanda.automated.download.film.exception.AutomatedDownloadFilmsException;
import es.lavanda.automated.download.film.model.FilmModel;
import es.lavanda.automated.download.film.model.TorrentCheckedResponse;
import es.lavanda.automated.download.film.model.TorrentModelRequest;
import es.lavanda.automated.download.film.repository.FilmModelRepository;
import es.lavanda.lib.common.model.FilmModelTorrent;
import es.lavanda.lib.common.model.MediaIDTO;
import es.lavanda.lib.common.model.MediaIDTO.Type;
import es.lavanda.lib.common.model.MediaODTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmsServiceImpl implements FilmsService {

    private final FilmModelRepository filmModelRepository;

    private final ProducerService producerService;

    @Override
    public FilmModel updateFilm(String filmModelId, FilmModel filmModel) {
        getFilm(filmModelId, false);
        FilmModel filmModelUpdated = save(filmModel);
        sendToAgent(filmModelUpdated);
        return filmModelUpdated;
    }

    @Override
    public List<FilmModel> searchFilms(String search) {
        return filmModelRepository.findByTitleContainsIgnoreCaseOrderByCreatedAtDesc(search);
    }

    @Override
    public void deleteFilmById(String id) {
        filmModelRepository.deleteById(id);
    }

    @Override
    public List<FilmModel> getLastFilms() {
        log.debug("getThreeLastFilms");
        return filmModelRepository.findTop6ByOrderByCreatedAtDesc();
    }

    @Override
    public Page<FilmModel> getAllFilmsOrderedByCreated(Pageable pageable) {
        return filmModelRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public List<FilmModel> getAllFilmsByTorrentTitle(String title) {
        log.debug("Finding film by title");
        return filmModelRepository.findAllByTitle(title);
    }

    @Override
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

    @Override
    public FilmModel editFilm(FilmModel showModel) {
        return filmModelRepository.save(showModel);
    }

    @Override
    public void checkTorrents() {
        Iterable<FilmModel> withDuplicatesIterable = filmModelRepository.findAll();
        List<FilmModel> withDuplicates = new ArrayList<>();
        withDuplicatesIterable.forEach(withDuplicates::add);
        for (FilmModel filmModel2 : withDuplicates) {
            for (FilmModelTorrent filmModelTorrent : filmModel2.getTorrents()) {
                if (Boolean.FALSE.equals(filmModelTorrent.isTorrentValidate())) {
                    log.info("Send to torrentValidate {}", filmModelTorrent.getTorrentUrl());
                    producerService.sendTorrentToValidate(new TorrentModelRequest(filmModelTorrent.getTorrentUrl()));
                }
            }
        }
    }

    @Override
    public void checkedTorrent(TorrentCheckedResponse torrentChecked) {
        log.info("checkedTorrent {}", torrentChecked.getTorrent());
        Optional<FilmModel> optFilmModel = filmModelRepository.findByTorrentsTorrentUrl(torrentChecked.getTorrent());
        if (optFilmModel.isPresent()) {
            FilmModel filmModel = optFilmModel.get();
            FilmModelTorrent filmModelTorrent = filmModel.getTorrents().stream()
                    .filter(x -> x.getTorrentUrl().equals(torrentChecked.getTorrent())).findFirst()
                    .orElseThrow(() -> new AutomatedDownloadFilmsException(
                            "Not found torrent on database " + torrentChecked.getTorrent()));
            if (torrentChecked.isValidate()) {
                log.info("Torrent checked and validate");
                filmModelTorrent.setTorrentMagnet(torrentChecked.getMagnet());
                filmModelTorrent.setTorrentValidate(true);
                save(filmModel);
            } else {
                log.info("Torrent to remove and validate");
                filmModel.getTorrents().remove(filmModelTorrent);
                if (filmModel.getTorrents().size() == 0) {
                    deleteFilmById(filmModel.getId());
                } else {
                    save(filmModel);
                }
            }
        }

    }

    @Override
    public void executeFilm(FilmModelTorrent filmModelTorrent) {
        log.info("Execute film {}", filmModelTorrent.toString());
        if (Boolean.FALSE.equals(filmModelRepository.existsByTorrentsTorrentUrl(filmModelTorrent.getTorrentUrl()))) {
            try {
                log.info("Torrent {} no exist on database ", filmModelTorrent.getTorrentUrl());
                createNewFilmModel(List.of(filmModelTorrent));
            } catch (AutomatedDownloadFilmsException e) {
                log.error("Not sended torrent to agent", e);
                throw e;
            }
        }
    }

    @Override
    public void updateFilmWithMediaDTO(MediaODTO mediaODTO) {
        FilmModel filmModel = getFilm(mediaODTO.getId(), false);
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
        joinSameIdOriginal(mediaODTO.getIdOriginal());
    }

    @Override
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
        filmModel.getTorrents()
                .removeIf(oldTorrent -> oldTorrent.getTorrentUrl().equals(filmModelTorrent.getTorrentUrl()));
        filmModel.getTorrents().add(filmModelTorrent);
        save(filmModel);
        return filmModelTorrent;
    }

    @Override
    public void deleteTorrentOfFilmModel(String torrentId) {
        FilmModel filmModel = filmModelRepository.findByTorrentsTorrentId(torrentId).orElseThrow(
                () -> new AutomatedDownloadFilmsException("Not exists film with this torrentId " + torrentId));
        if (Boolean.FALSE.equals(
                filmModel.getTorrents().removeIf(torrent -> torrent.getTorrentId().toString().equals(torrentId)))) {
            throw new AutomatedDownloadFilmsException("Not exists film with this torrentId " + torrentId);
        } else {
            filmModelRepository.save(filmModel);
        }
    }

    @Override
    public void cleanEmptys() {
        Iterable<FilmModel> withDuplicatesIterable = filmModelRepository.findAll();
        List<FilmModel> withDuplicates = new ArrayList<>();
        withDuplicatesIterable.forEach(withDuplicates::add);
        for (FilmModel filmModel2 : withDuplicates) {
            if (filmModel2.getTorrents().size() == 0) {
                deleteFilmById(filmModel2.getId());
            }
        }
    }

    private void joinSameIdOriginal(String idOriginal) {
        List<FilmModel> filmModelsWithSameOriginalId = filmModelRepository.findByIdOriginal(idOriginal);
        if (filmModelsWithSameOriginalId.size() > 1) {
            log.info("Join same id original {}", idOriginal);
            List<FilmModelTorrent> listToAdd = new ArrayList<>();
            for (FilmModel filmModelIterator : filmModelsWithSameOriginalId) {
                listToAdd.addAll(filmModelIterator.getTorrents());
            }
            filmModelRepository.deleteAll(filmModelsWithSameOriginalId);
            createNewFilmModel(listToAdd);
        }
    }

    private FilmModel createFilm(FilmModel showModel) {
        return filmModelRepository.save(showModel);
    }

    private void updateLibrary() {
        Iterable<FilmModel> withDuplicatesIterable = filmModelRepository.findAll();
        List<FilmModel> filmModels = new ArrayList<>();
        withDuplicatesIterable.forEach(filmModels::add);
        for (FilmModel filmModel : filmModels) {
            sendToAgent(filmModel);
            // filmModelRepository.save(filmModel);
            // updateFilm(filmModel);
        }
    }

    private void cleanDuplicates() {
        Iterable<FilmModel> withDuplicatesIterable = filmModelRepository.findAll();
        List<FilmModel> withDuplicates = new ArrayList<>();
        withDuplicatesIterable.forEach(withDuplicates::add);
        List<FilmModel> withoutDuplicates = new ArrayList<>();
        for (FilmModel filmModel : withDuplicates) {
            boolean toAggregate = true;
            // for (FilmModel filmModel2 : withoutDuplicates) {
            // if (Objects.nonNull(filmModel.get()) &&
            // Objects.nonNull(filmModel2.getTorrentUrl())
            // && filmModel2.getTorrentUrl().equals(filmModel.getTorrentUrl())) {
            // toAggregate = false;
            // }
            // }
            if (toAggregate) {
                withoutDuplicates.add(filmModel);
            }
        }
        log.info("Tamaño withDuplicates {}", withDuplicates.size());
        log.info("Tamaño withoutDuplicates {}", withoutDuplicates.size());
        filmModelRepository.deleteAll();
        filmModelRepository.saveAll(withoutDuplicates);
    }

    private void createNewFilmModel(List<FilmModelTorrent> filmModelTorrents) {
        if (Boolean.FALSE.equals(filmModelTorrents.isEmpty())) {
            log.info("Creating new filmModel with this data {} ", filmModelTorrents.get(0).getTorrentTitle());
            FilmModel filmModel = new FilmModel();
            filmModel.setTorrents(new HashSet<>(filmModelTorrents));
            sendToAgent(save(filmModel));
        }
    }

    private void sendToAgent(FilmModel filmModel) {
        log.info("Sending to agent the filmModel with ID {}", filmModel.getId());
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

    private void sendToDownloadTorrent(FilmModelTorrent filmModel) {
        log.info("Checking if needs to be downloaded:  {}", filmModel.getTorrentUrl());
        producerService.sendTorrentToDownload(new TorrentModelRequest(filmModel.getTorrentUrl()));
        filmModel.setDownloaded(true);
    }

    private FilmModel save(FilmModel filmModel) {
        return filmModelRepository.save(filmModel);
    }

}