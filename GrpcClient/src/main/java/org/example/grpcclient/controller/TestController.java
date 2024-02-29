package org.example.grpcclient.controller;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.grpcclient.service.BookAuthorClientService;
import org.example.grpcclient.utils.GrpcException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/testDir")
@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class TestController {

    BookAuthorClientService clientService;

    @GetMapping("/1")
    public String sendRequest() {
        log.atInfo().log(Thread.currentThread().toString());
        try {
            return clientService.getMostAttendeeBook().toString();
        }catch (GrpcException e) {
            e.printStackTrace();
            return e.getEMessage();
        }
        catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
