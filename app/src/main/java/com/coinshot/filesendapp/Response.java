package com.coinshot.filesendapp;

public class Response {
    boolean success;
    String fileName;

    public Response(boolean success, String fileName){
        this.success = success;
        this.fileName = fileName;
    }
}
