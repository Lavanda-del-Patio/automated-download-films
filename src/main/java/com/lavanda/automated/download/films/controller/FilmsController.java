package com.lavanda.automated.download.films.controller;

import java.util.List;

import com.lavanda.automated.download.films.exception.AutomatedDownloadFilmsException;
import com.lavanda.automated.download.films.model.FilmModel;
import com.lavanda.automated.download.films.service.FilmsServiceImpl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.lavanda.lib.common.model.FilmModelTorrent;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feed-films")
@CrossOrigin(allowedHeaders = "*", origins = { "http://localhost:4200", "https://lavandadelpatio.es",
		"https://pre.lavandadelpatio.es" }, allowCredentials = "true", exposedHeaders = "*", methods = {
				RequestMethod.OPTIONS, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.PATCH, RequestMethod.POST,
				RequestMethod.PUT }, originPatterns = {})
public class FilmsController {

	private final FilmsServiceImpl filmsServiceImpl;

	@GetMapping
	public ResponseEntity<Page<FilmModel>> getAllFilms(Pageable pageable) {
		return ResponseEntity.ok(filmsServiceImpl.getAllFilmsOrderedByCreated(pageable));
	}

	@GetMapping("/search")
	public ResponseEntity<List<FilmModel>> searchFilms(@RequestParam String title) {
		return ResponseEntity.ok(filmsServiceImpl.searchFilms(title));
	}

	@GetMapping("/last")
	public ResponseEntity<List<FilmModel>> getLastFilms() throws AutomatedDownloadFilmsException {
		return ResponseEntity.ok(filmsServiceImpl.getLastFilms());
	}

	@PutMapping("/{filmModelId}")
	public ResponseEntity<FilmModel> editFilm(@PathVariable String filmModelId, @RequestBody FilmModel filmModel) {
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(filmsServiceImpl.updateFilm(filmModelId, filmModel));

	}

	@PutMapping("/torrents/{torrentId}")
	public ResponseEntity<FilmModelTorrent> editTorrent(@PathVariable String torrentId,
			@RequestBody FilmModelTorrent filmModelTorrent) {
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(filmsServiceImpl.updateTorrent(torrentId, filmModelTorrent));
	}

	@GetMapping("/{id}")
	public ResponseEntity<FilmModel> getFilm(@PathVariable String id) {
		return ResponseEntity.ok(filmsServiceImpl.getFilm(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteFilm(@PathVariable String id) {
		filmsServiceImpl.deleteFilmById(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("/torrents/{id}")
	public ResponseEntity<Void> deleteTorrent(@PathVariable String id) {
		filmsServiceImpl.deleteFilmByTorrentId(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// @GetMapping("/duplicates")
	// public ResponseEntity<Void> deleteDuplicates() {
	// filmsServiceImpl.cleanDuplicates();
	// return new ResponseEntity<>(HttpStatus.OK);
	// }

	// @GetMapping("/update-library")
	// public ResponseEntity<Void> updateLibrary() {
	// filmsServiceImpl.updateLibrary();
	// return new ResponseEntity<>(HttpStatus.OK);
	// }

}
