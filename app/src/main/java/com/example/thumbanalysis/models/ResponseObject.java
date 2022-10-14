package com.example.thumbanalysis.models; ;

import com.example.thumbanalysis.models.Message;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseObject {

    @SerializedName("message")
    @Expose
    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

}