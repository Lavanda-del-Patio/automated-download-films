package es.lavanda.automated.download.film.model.dto;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import es.lavanda.lib.common.model.MediaIDTO.Type;
import lombok.Data;

@Data
public class FilmModelDTO  {

    private String id;

    private String idOriginal;

    private String title;

    private String titleOriginal;

    private String image;

    private String backdropImage;

    private String overview;

    private float voteAverage;

    private LocalDate releaseDate;

    private Type type;
    
    private Set<FilmModelTorrentDTO> torrents = new HashSet<>();

    private String createdBy;

    private Date createdAt;

    private String lastModifiedBy;

    private Date lastModifiedAt;

}