package com.lavanda.automated.download.films.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import es.lavanda.lib.common.model.FilmModelTorrent;
import lombok.Data;

@Data
public class LambdaDTO {

    @JsonIgnore
    private String version;

    private Date timestamp;

    @JsonIgnore
    private Object requestContext;

    @JsonIgnore
    private Object requestPayload;

    @JsonIgnore
    private Object responseContext;

    @JsonProperty("responsePayload")
    private List<FilmModelTorrent> filmModelTorrents;
}
