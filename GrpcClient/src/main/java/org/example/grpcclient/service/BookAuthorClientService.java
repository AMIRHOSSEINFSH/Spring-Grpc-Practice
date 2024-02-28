package org.example.grpcclient.service;


import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.val;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.grpcserver.proto.BookAuthorServiceGrpc;
import org.example.grpcserver.proto.Models.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


@Service
public class BookAuthorClientService {

    @GrpcClient("grpcServer")
    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub synchronousClient;

    @GrpcClient("grpcServer")
    BookAuthorServiceGrpc.BookAuthorServiceStub asynchronousClient;

    public Author getAuthorByAuthorId(int authorId) {
        val authorRequestModel = AuthorIdRequest.newBuilder().setAuthorId(authorId).build();
        return synchronousClient.getAuthorByAuthorId(authorRequestModel);
    }

    public List<Author> getAuthorListByBookId(int bookId) {
        val bookRequestModel = BookIdRequest.newBuilder().setBookId(bookId).build();
        return synchronousClient.getAuthorsByBookId(bookRequestModel).getAuthorsList();
    }

    //can use CountDownLatch too
    volatile boolean isLock = true;

    public List<Book> getBookListOfAuthorByAuthorId(int authorId) {
        val authorRequestModel = AuthorIdRequest.newBuilder().setAuthorId(authorId).build();
        val resultList = new ArrayList<Book>();

        asynchronousClient.getBookListOfAuthorByAuthorId(authorRequestModel, new StreamObserver<>() {
            @Override
            public void onNext(Book book) {
                resultList.add(book);
            }

            @Override
            public void onError(Throwable throwable) {
                isLock = false;
            }

            @Override
            public void onCompleted() {
                isLock = false;
            }
        });
        while (isLock) {
            Thread.onSpinWait();
        }

        return resultList;
    }

    volatile boolean isLock2 = true;

    public Book getMostAttendeeBook() throws InterruptedException {
        final Book[] resultBook = new Book[1];
        val countDownLatch = new CountDownLatch(1);
        val responseObserver = asynchronousClient.getMostAttendeeAuthorsForBook(new StreamObserver<>() {
            @Override
            public void onNext(Book book) {
                resultBook[0] = book;
                isLock2 = false;
            }

            @Override
            public void onError(Throwable throwable) {
                isLock2 = false;
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                isLock2 = false;
                countDownLatch.countDown();
            }
        });

        val result = getLibrarySnapShot().values().stream()
                .flatMap(List::stream)
                .distinct()
                .map(book -> BookIdRequest.newBuilder().setBookId(book.getBookId()).build()).toList();
        for (BookIdRequest bookIdRequest : result) {
            responseObserver.onNext(bookIdRequest);
        }

        responseObserver.onCompleted();
        while (isLock2) {
            Thread.onSpinWait();
        }
//        boolean await= countDownLatch.await(1, TimeUnit.MINUTES);
//        return await ? resultBook[0] : null;
        return resultBook[0];
    }

    private volatile boolean isLock3= true ;
    public List<AuthorBookPair> getAuthorBookPairListByIds(List<Integer> bookIds) {
        List<AuthorBookPair> resultList = new ArrayList<>();

        val serverObserver = asynchronousClient.getBooksWithSameAuthor(
                new StreamObserver<>() {
                    @Override
                    public void onNext(AuthorBookPair authorBookPair) {
                        resultList.add(authorBookPair);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        isLock3 = false;
                    }

                    @Override
                    public void onCompleted() {
                        isLock3 = false;
                    }
                }
        );

        val data =bookIds.stream()
                .map(itId -> BookIdRequest.newBuilder().setBookId(itId).build())
                .toList();
        data.forEach(serverObserver::onNext);
        serverObserver.onCompleted();

        while (isLock3) {Thread.onSpinWait();}

        return resultList;
    }


    public Map<Author, List<Book>> getLibrarySnapShot() {
        val data = synchronousClient.getAuthorToBooksMapSnapshot(Empty.newBuilder().build());
        return data.getPairListList().stream().collect(Collectors.toMap(AuthorBookPair::getAuthor, AuthorBookPair::getBookList));
    }


}
