package es.lavanda.automated.download.film.model;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import es.lavanda.lib.common.model.FilmModelTorrent;
import es.lavanda.lib.common.model.MediaIDTO.Type;
import lombok.Data;
import lombok.ToString;

@Data
@Document("feed_films")
@ToString
public class FilmModel {

    @Id
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
    
    private Set<FilmModelTorrent> torrents = new HashSet<>();

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Date createdAt;

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    private Date lastModifiedAt;

}