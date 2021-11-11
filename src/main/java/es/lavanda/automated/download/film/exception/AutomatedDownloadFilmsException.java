package es.lavanda.automated.download.film.exception;

public class AutomatedDownloadFilmsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AutomatedDownloadFilmsException(String message) {
        super(message);
    }

    public AutomatedDownloadFilmsException(String message, Exception e) {
        super(message, e);
    }
}