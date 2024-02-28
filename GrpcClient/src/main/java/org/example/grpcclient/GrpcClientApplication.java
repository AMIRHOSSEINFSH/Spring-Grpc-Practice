package org.example.grpcclient;

import lombok.val;
import org.example.grpcclient.service.BookAuthorClientService;
import org.example.grpcserver.proto.Models;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcClientApplication {


    public static void main(String[] args) {
        var ctx = SpringApplication.run(GrpcClientApplication.class, args);
        BookAuthorClientService bookAuthorClientService = ctx.getBean(BookAuthorClientService.class);

        //TODO now you can call function on bookAuthorClientService instance
//        val result = bookAuthorClientService.getBookListOfAuthorByAuthorId(4);
        Models.Book result = null;
        try {
            result = bookAuthorClientService.getMostAttendeeBook();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(result);
    }

}
