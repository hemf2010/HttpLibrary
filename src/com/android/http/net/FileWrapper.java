package com.android.http.net;

import java.io.InputStream;

public class FileWrapper {
	public InputStream inputStream;
    public String fileName;
    public String contentType;

    public FileWrapper(InputStream inputStream, String fileName, String contentType) {
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public String getFileName() {
        if(fileName != null) {
            return fileName;
        } else {
            return "nofilename";
        }
    }
}
