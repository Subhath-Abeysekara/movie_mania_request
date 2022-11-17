package com.movieMania.backend.Service;

import com.intouncommon.backend.Repository.exception.ResourceNotFoundException;
import com.movieMania.backend.Entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class requestServiceImpl implements requestService{

    @Autowired
    private com.movieMania.backend.Repository.requestRepository requestRepository;

    @Autowired
    private otherService otherService;


    private String generateCode(String contact , Long id){
        String code = "";
        if (id<10){
            contact+="000";
        }
        if (id<100){
            contact+="00";
        }
        if (id<1000){
            contact+="0";
        }
        code+=contact;
        code+=id.toString();

        return code;
    }

    private request dtoToRequest(requestDto requestDto){
        request request = new request();

        request.setPayableStatus(requestDto.getPayableStatus());
        request.setContact(requestDto.getContact());
        request.setCustomerEmail(requestDto.getCustomerEmail());
        request.setCustomerName(requestDto.getCustomerName());
        request.setDriverLink(requestDto.getDriverLink());

        return request;
    }

    @Override
    public String addRequest(requestDto requestDto,String code) {

        if(code.equalsIgnoreCase("none")){

            request request = dtoToRequest(requestDto);
            Long movie = requestDto.getMovie();
            Long id = requestRepository.save(request).getRequestId();
            String multiCode = generateCode(request.getContact(),id);
            Optional<request> request1 = requestRepository.findById(id);
            if (request1.isPresent()){
                request1.get().setCode(multiCode);
                request1.get().setMultiCode(multiCode);
                request1.get().setMovieId(movie);
                String email = request1.get().getCustomerEmail();
                requestRepository.save(request1.get());
                otherService.sendMails(email,"Your Code For Request",multiCode);

            }


            return multiCode;
        }
        else {

            request request = dtoToRequest(requestDto);
            Long movie = requestDto.getMovie();
            Long id = requestRepository.save(request).getRequestId();
            String multiCode = generateCode(request.getContact(),id);
            Optional<request> request1 = requestRepository.findById(id);
            if (request1.isPresent()){
                request1.get().setCode(multiCode);
                request1.get().setMultiCode(code);
                request1.get().setMovieId(movie);
                requestRepository.save(request1.get());

            }


            return "success";
        }
    }
    @Override
    public List<request> getAllPayableRequest() {
        List<request> requests = requestRepository.findAll();
        List<request> requests1 = new ArrayList<>();
        for (request request : requests){
            if (request.getPayableStatus().equalsIgnoreCase("payable")){
                requests1.add(request);
            }
        }
        return requests1;
    }

//    private List<requestResponse> getAllRequestResponse(){
//        List<movie> movies = movieRepository.findAll();
//        List<requestResponse> requestResponses = new ArrayList<>();
//        for (movie movie : movies){
//            List<request> requests = movie.getRequests();
//            for (request request : requests){
//                requestResponse requestResponse = new requestResponse();
//                requestResponse.setRequest(request);
//                requestResponse.setMovie(returnMovieData(movie));
//                requestResponses.add(requestResponse);
//            }
//        }
//
//        return requestResponses;
//    }



    @Override
    public String cancelRequest(String code) {
        List<request> requests = requestRepository.findByMultiCode(code);
        for (request request : requests){
            if (request.getAdminStatus().equalsIgnoreCase("pending")){
                requestRepository.deleteByRequestId(request.getRequestId());
                return "cancel";
            }
            return "Cant cancel now";
        }
        return "error id";
    }

    @Override
    public String addScanCopy(String url,String code) {
        List<request> requests = requestRepository.findByMultiCode(code);
        for (com.movieMania.backend.Entity.request request : requests){
            request.setSlipUrl(url);
            request.setAdminStatus("payed");
            requestRepository.save(request);
        }
        if(requests.isEmpty()){
            return "error code";
        }

        return "added";
    }

    @Override
    public List<request> getPayedRequests() {
        List<request> requests = requestRepository.findAll();
        List<request> requests1 = new ArrayList<>();
        for (request request : requests){
            if (request.getAdminStatus().equalsIgnoreCase("payed")&&request.getPayableStatus().equalsIgnoreCase("payable")){
                requests1.add(request);
            }
            else{
            if (request.getAdminStatus().equalsIgnoreCase("confirm")&&request.getPayableStatus().equalsIgnoreCase("payable")){
                requests1.add(request);
            }
            }
        }
        return requests1;
    }

    @Override
    public String confirmRequest(Long id) {
        Optional<request> request = requestRepository.findById(id);
        if (request.isPresent()&&request.get().getAdminStatus().equalsIgnoreCase("payed")){
            request.get().setAdminStatus("confirm");
            String email = request.get().getCustomerEmail();
            String subject = "Your Request Has Confirmed Your movie will be uploaded soon";
            otherService.sendMails(email,"Request Confirmation",subject);
            requestRepository.save(request.get());
            return "confirmed";
        }
        return "error id";
    }

    @Override
    public String rejectRequest(Long id,String reason) {
        Optional<request> request = requestRepository.findById(id);
        if (request.isPresent()) {
            if (request.get().getAdminStatus().equalsIgnoreCase("pending")) {
                String email = request.get().getCustomerEmail();
                String subject = "Your Request Has Rejected Reason - "+reason;
                otherService.sendMails(email, "Request Rejection", subject);
                requestRepository.deleteByRequestId(id);
                return "rejected";
            }
            return "cant reject";
        }

        return "error id";
    }

    @Override
    public List<request> getByCode(String code) {
        List<request> requests = requestRepository.findAll();
        List<request> requests1 = new ArrayList<>();
        for (request request : requests){
            if(request.getMultiCode()!=null){
                if (request.getMultiCode().equals(code)){
                    requests1.add(request);
                }
            }
        }
        return requests1;
    }

    @Override
    public String sendUploadMail(String code) {
        Optional<request> request = requestRepository.findByCode(code);
        if (request.isPresent()){
            String subject = "Your Requested movie is uploaded to the driver please check your driver Link - "+request.get().getDriverLink();
            otherService.sendMails(request.get().getCustomerEmail(), "Movie Upload", subject);
            request.get().setAdminStatus("uploaded");
            requestRepository.save(request.get());
            return "sent";
        }
        return "error code";
    }

    @Override
    public List<request> getNotPayableRequests() {
        List<request> requests = requestRepository.findAll();
        List<request> requests1 = new ArrayList<>();
        for (request request : requests){
            if (request.getPayableStatus().equalsIgnoreCase("notPayable")&&request.getAdminShow().equalsIgnoreCase("notShow")){
                requests1.add(request);
            }
        }
        return requests1;
    }

    @Override
    public List<request> getUploadedRequests() {
        List<request> requests = requestRepository.findAll();
        List<request> requests1 = new ArrayList<>();
        for (request request : requests){
            if (request.getAdminStatus().equalsIgnoreCase("uploaded")&&request.getPayableStatus().equalsIgnoreCase("payable")){
                requests1.add(request);
            }
        }
        return requests1;
    }

    @Override
    public String setShowState(Long id) {
        Optional<request> request = requestRepository.findById(id);
        if (request.isPresent()&&request.get().getAdminShow().equalsIgnoreCase("notShow")){
            request.get().setAdminShow("show");
            request.get().setAdminStatus("confirm");
            requestRepository.save(request.get());
            return "saved";
        }
        return "error id";
    }
}
