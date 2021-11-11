package es.lavanda.automated.download.film.model.dto;

import java.time.LocalDate;
import java.util.Date;

import es.lavanda.lib.common.model.FilmModelTorrent;
import es.lavanda.lib.common.model.MediaIDTO.Type;
import lombok.Data;

@Data
public class FilmModelDTO extends FilmModelTorrent {

    private String id;

    private String title;

    private String titleOriginal;

    private String image;

    private boolean downloaded;

    private boolean assignToDownload;

    private LocalDate releaseDate;

    private Type type;

    private String createdBy;

    private Date createdAt;

    private String lastModifiedBy;

    private Date lastModifiedAt;

}