package com.bstech.voicechanger.model;



public class Record {
    private int id;
    private String title;
    private String filePath;
    private long duration;
    private String size;
    private long dateTime;
    private boolean isCheck;
    private String uriFile;


    public Record() {
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public boolean getCheck() {
        return isCheck;
    }




    public void setUriFile(String uriFile) {
        this.uriFile = uriFile;
    }

    public String getUriFile() {
        return uriFile;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDateTime() {
        return dateTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getLength() {
        return duration;
    }

    public void setLength(int length) {
        duration = length;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return dateTime;
    }

    public void setTime(long time) {
        dateTime = time;
    }


    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

}
