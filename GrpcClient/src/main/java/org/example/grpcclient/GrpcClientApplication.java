package org.example.grpcclient;

import lombok.val;
import org.example.grpcclient.service.BookAuthorClientService;
import org.example.grpcserver.proto.Models;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class GrpcClientApplication {


    public static void main(String[] args) {
        var ctx = SpringApplication.run(GrpcClientApplication.class, args);
        BookAuthorClientService bookAuthorClientService = ctx.getBean(BookAuthorClientService.class);

        /*//TODO now you can call function on bookAuthorClientService instance
//        val result = bookAuthorClientService.getBookListOfAuthorByAuthorId(4);
        List<Models.AuthorBookPair> result;
        val bookList = bookAuthorClientService.getLibrarySnapShot()
                .values()
                .stream().flatMap(List::stream)
                .collect(Collectors.toSet())
                .stream()
                .toList();

        val requestedIds = Set.of(
                bookList.get((new Random()).nextInt(0, bookList.size() - 1)).getBookId(),
                bookList.get((new Random()).nextInt(0, bookList.size() - 1)).getBookId()
        );

        result = bookAuthorClientService.getAuthorBookPairListByIds(requestedIds.stream().toList());
        System.out.println(result);*/
    }

}
