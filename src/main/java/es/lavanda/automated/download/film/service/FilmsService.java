package es.lavanda.automated.download.film.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.lavanda.automated.download.film.model.FilmModel;
import es.lavanda.automated.download.film.model.TorrentCheckedResponse;
import es.lavanda.lib.common.model.FilmModelTorrent;
import es.lavanda.lib.common.model.MediaODTO;

public interface FilmsService {

    FilmModel updateFilm(String filmModelId, FilmModel filmModel);

    List<FilmModel> searchFilms(String search);

    void deleteFilmById(String id);

    List<FilmModel> getLastFilms();

    Page<FilmModel> getAllFilmsOrderedByCreated(Pageable pageable);

    List<FilmModel> getAllFilmsByTorrentTitle(String title);

    FilmModel getFilm(String id, boolean force);

    FilmModel editFilm(FilmModel showModel);

    void checkTorrents();

    void checkedTorrent(TorrentCheckedResponse torrentChecked);

    void executeFilm(FilmModelTorrent filmModelTorrent);

    void updateFilmWithMediaDTO(MediaODTO mediaODTO);

    FilmModelTorrent updateTorrent(String torrentId, FilmModelTorrent filmModelTorrent);

    void deleteTorrentOfFilmModel(String torrentId);
}
