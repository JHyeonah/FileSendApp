package com.coinshot.filesendapp.model;

public class Response {
    public boolean success;
    public String fileName;

    public Response(boolean success, String fileName){
        this.success = success;
        this.fileName = fileName;
    }
}
