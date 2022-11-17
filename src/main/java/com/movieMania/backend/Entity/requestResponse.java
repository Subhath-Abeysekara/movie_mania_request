package com.movieMania.backend.Entity;

import lombok.Data;

@Data
public class requestResponse {

    request request;

    public requestResponse() {
    }

    public com.movieMania.backend.Entity.request getRequest() {
        return request;
    }

    public void setRequest(com.movieMania.backend.Entity.request request) {
        this.request = request;
    }

}
