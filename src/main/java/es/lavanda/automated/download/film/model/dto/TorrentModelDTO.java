package es.lavanda.automated.download.film.model.dto;

import lombok.Data;

@Data
public class TorrentModelDTO {

    private String torrentId;

    private String torrentTitle;

    private String torrentCroppedTitle;

    private String torrentImage;

    private String torrentQuality;

    private boolean downloaded;

    private String torrentSize;

    // private String torrentUrl;

    // private String torrentMagnet;

    private boolean torrentValidate;

    private Page torrentPage;

    public enum Page {
        DON_TORRENT, PCTMIX, PCTFENIX;

    }
}