package org.example.grpcclient;

import lombok.val;
import org.example.grpcclient.service.BookAuthorClientService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcClientApplication {


    public static void main(String[] args) {
        var ctx = SpringApplication.run(GrpcClientApplication.class, args);
        BookAuthorClientService bookAuthorClientService = ctx.getBean(BookAuthorClientService.class);

        //TODO now you can call function on bookAuthorClientService instance
        val result = bookAuthorClientService.getLibrarySnapShot();
        System.out.println(result);
    }

}
