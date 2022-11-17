package com.movieMania.backend.Service;

import com.movieMania.backend.Entity.request;
import com.movieMania.backend.Entity.requestDto;
import com.movieMania.backend.Entity.requestDto;
import com.movieMania.backend.Entity.requestResponse;

import java.util.List;

public interface requestService {

    String addRequest(requestDto requestDto , String code);
    List<request> getAllPayableRequest();
    String cancelRequest(String code);
    String addScanCopy(String url,String code);
    List<request> getPayedRequests();
    String confirmRequest(Long id);
    String rejectRequest(Long id,String reason);
    List<request> getByCode(String code);
    String sendUploadMail(String code);
    List<request> getNotPayableRequests();
    List<request> getUploadedRequests();
    String setShowState(Long id);
}
