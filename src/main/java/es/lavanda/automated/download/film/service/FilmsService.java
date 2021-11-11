package es.lavanda.automated.download.film.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.lavanda.automated.download.film.model.FilmModel;

public interface FilmsService {

    FilmModel getFilmByTitle(String title);

    FilmModel save(FilmModel filmModel);

    FilmModel updateFilm(String filmModelId, FilmModel filmModel);

    List<FilmModel> searchFilms(String search);

    void deleteFilmById(String id);

    List<FilmModel> getLastFilms();

    Page<FilmModel> getAllFilmsOrderedByCreated(Pageable pageable);

    List<FilmModel> getAllFilmsByTorrentTitle(String title);

    FilmModel getFilm(String id, boolean force);

    FilmModel createFilm(FilmModel showModel);

    FilmModel editFilm(FilmModel showModel);

    // void cleanDuplicates();

    void checkTorrents();

    void checkedTorrent(TorrentCheckedResponse torrentChecked);

    void executeFilm(FilmModelTorrent filmModelTorrent);

    void updateLibrary();

    void updateFilmWithMediaDTO(MediaODTO mediaODTO);

    FilmModelTorrent updateTorrent(String torrentId, FilmModelTorrent filmModelTorrent);

    void deleteFilmByTorrentId(String torrentId);
}
