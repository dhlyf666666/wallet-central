package com.dhlyf.walletmodel.common;

public enum HttpApplicationType {

JSON("application/json"),
    FORM("application/x-www-form-urlencoded"),
    XML("application/xml"),
    TEXT("text/plain"),
    HTML("text/html"),
    JAVASCRIPT("application/javascript"),
    CSS("text/css"),
    PNG("image/png"),
    JPG("image/jpeg"),
    GIF("image/gif"),
    BMP("image/bmp"),
    MP4("video/mp4"),
    MP3("audio/mp3"),
    ZIP("application/zip"),
    PDF("application/pdf"),
    DOC("application/msword"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    XLS("application/vnd.ms-excel"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PPT("application/vnd.ms-powerpoint"),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    RAR("application/x-rar-compressed"),
    TAR("application/x-tar"),
    GZ("application/gzip"),
    BZ2("application/x-bzip2"),
    TXT("text/plain"),
    CSV("text/csv"),
    SQL("application/sql"),
    JS("application/javascript"),
    JAVA("text/x-java-source"),
    C("text/x-c"),
    CPP("text/x-c++"),
    H("text/x-c-header"),
    HTM("text/html"),
    WAV("audio/wav"),
    OGG("audio/ogg"),
    WEBM("video/webm"),
    ICO("image/x-icon"),
    JPEG("image/jpeg"),
    TIFF("image/tiff"),
    TIF("image/tiff"),
    SVG("image/svg+xml"),
    WEBP("image/webp"),
    ;
    private String type;

    private HttpApplicationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
