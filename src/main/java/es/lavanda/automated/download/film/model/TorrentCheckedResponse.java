package es.lavanda.automated.download.film.model;

import lombok.Data;

@Data
public class TorrentCheckedResponse {

    private String torrent;

    private boolean validate;

    private String magnet;
}
