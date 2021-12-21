package es.lavanda.automated.download.film.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, exclude = "torrentYear")
public class FilmModelTorrentDTO extends TorrentModelDTO {

    private int torrentYear;

}
