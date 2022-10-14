package com.example.thumbanalysis.models;

public class SearchResultModel {
    String fileName, message;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SearchResultModel(String fileName, String message) {
        this.fileName = fileName;
        this.message = message;
    }
}
