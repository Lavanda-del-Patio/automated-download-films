package es.lavanda.automated.download.film.controller;

import java.util.List;
import java.util.stream.Collectors;

import es.lavanda.automated.download.film.exception.AutomatedDownloadFilmsException;
import es.lavanda.automated.download.film.model.FilmModel;
import es.lavanda.automated.download.film.model.dto.FilmModelDTO;
import es.lavanda.automated.download.film.service.FilmsServiceImpl;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

	private final ModelMapper modelMapper;

	@GetMapping
	public ResponseEntity<Page<FilmModelDTO>> getAllFilms(Pageable pageable) {
		List<FilmModelDTO> films = filmsServiceImpl.getAllFilmsOrderedByCreated(pageable).stream().map(film -> {
			FilmModelDTO filmModelDTO = modelMapper.map(film, FilmModelDTO.class);
			return filmModelDTO;
		}).collect(Collectors.toList());
		return ResponseEntity.ok(new PageImpl<>(films, pageable, films.size()));
	}

	@GetMapping("/search")
	public ResponseEntity<List<FilmModelDTO>> searchFilms(@RequestParam String title) {
		List<FilmModelDTO> films = filmsServiceImpl.searchFilms(title).stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
		return ResponseEntity.ok(films);
	}

	@GetMapping("/last")
	public ResponseEntity<List<FilmModelDTO>> getLastFilms() throws AutomatedDownloadFilmsException {
		List<FilmModelDTO> films = filmsServiceImpl.getLastFilms().stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
		return ResponseEntity.ok(films);
	}

	@PutMapping("/{filmModelId}")
	public ResponseEntity<FilmModelDTO> editFilm(@PathVariable String filmModelId, @RequestBody FilmModel filmModel) {
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(convertToDto(filmsServiceImpl.updateFilm(filmModelId, filmModel)));
	}

	@PutMapping("/torrents/{torrentId}")
	public ResponseEntity<FilmModelTorrent> editTorrent(@PathVariable String torrentId,
			@RequestBody FilmModelTorrent filmModelTorrent) {
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(filmsServiceImpl.updateTorrent(torrentId, filmModelTorrent));
	}

	@PostMapping("/torrents/{torrentId}")
	public ResponseEntity<FilmModelTorrent> downloadTorrent(@PathVariable String torrentId,
			@RequestParam(name = "forceDownload", required = false) boolean forceDownload) {
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(filmsServiceImpl.downloadTorrent(torrentId, forceDownload));
	}

	@GetMapping("/{id}")
	public ResponseEntity<FilmModelDTO> getFilm(@PathVariable String id, @RequestParam boolean force) {
		return ResponseEntity.ok(convertToDto(filmsServiceImpl.getFilm(id, force)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteFilm(@PathVariable String id) {
		filmsServiceImpl.deleteFilmById(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("/torrents/{id}")
	public ResponseEntity<Void> deleteTorrent(@PathVariable String id) {
		filmsServiceImpl.deleteTorrentOfFilmModel(id);
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

	@GetMapping("/check-torrents")
	public ResponseEntity<Void> updateLibrary() {
		filmsServiceImpl.checkTorrents();
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/clean-emptys")
	public ResponseEntity<Void> cleanEmptys() {
		filmsServiceImpl.cleanEmptys();
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private FilmModelDTO convertToDto(FilmModel post) {
		FilmModelDTO postDto = modelMapper.map(post, FilmModelDTO.class);
		return postDto;
	}

	private FilmModel convertToEntity(FilmModelDTO filmModelDTO) {
		FilmModel filmModel = modelMapper.map(filmModelDTO, FilmModel.class);
		return filmModel;
	}
}
