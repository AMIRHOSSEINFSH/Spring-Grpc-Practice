package org.example.grpcclient.service;


import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.val;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.grpcclient.utils.GrpcException;
import org.example.grpcserver.proto.BookAuthorServiceGrpc;
import org.example.grpcserver.proto.Models.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import static org.example.grpcclient.utils.Const.GPRC_TIME_OUT;
import static org.example.grpcclient.utils.Const.GPRC_TIME_OUT_UTIL;


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

    public List<Book> getBookListOfAuthorByAuthorId(int authorId) throws InterruptedException, GrpcException {
        val latch = new CountDownLatch(1);
        val authorRequestModel = AuthorIdRequest.newBuilder().setAuthorId(authorId).build();
        val resultList = new ArrayList<Book>();

        asynchronousClient.getBookListOfAuthorByAuthorId(authorRequestModel, new StreamObserver<>() {
            @Override
            public void onNext(Book book) {
                resultList.add(book);
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });
        val isReach= latch.await(GPRC_TIME_OUT, GPRC_TIME_OUT_UTIL);
        //time out reached and server did not send any data
        if (!isReach) throw new GrpcException("Grpc TimeOut Reached",null);

        return resultList;
    }

    public Book getMostAttendeeBook() throws InterruptedException, GrpcException {
        final Book[] resultBook = new Book[1];
        val latch = new CountDownLatch(1);
        val responseObserver = asynchronousClient.getMostAttendeeAuthorsForBook(new StreamObserver<>() {
            @Override
            public void onNext(Book book) {
                resultBook[0] = book;
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
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

        val isReach= latch.await(GPRC_TIME_OUT, GPRC_TIME_OUT_UTIL);
        //time out reached and server did not send any data
        if (!isReach) throw new GrpcException("Grpc TimeOut Reached",null);

        return resultBook[0];
    }

    public List<AuthorBookPair> getAuthorBookPairListByIds(List<Integer> bookIds) throws InterruptedException, GrpcException {
        val latch = new CountDownLatch(1);
        List<AuthorBookPair> resultList = new ArrayList<>();

        val serverObserver = asynchronousClient.getBooksWithSameAuthor(
                new StreamObserver<>() {
                    @Override
                    public void onNext(AuthorBookPair authorBookPair) {
                        resultList.add(authorBookPair);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                }
        );

        val data =bookIds.stream()
                .map(itId -> BookIdRequest.newBuilder().setBookId(itId).build())
                .toList();
        data.forEach(serverObserver::onNext);
        serverObserver.onCompleted();

        val isReach= latch.await(GPRC_TIME_OUT, GPRC_TIME_OUT_UTIL);
        //time out reached and server did not send any data
        if (!isReach) throw new GrpcException("Grpc TimeOut Reached",null);

        return resultList;
    }


    public Map<Author, List<Book>> getLibrarySnapShot() {
        val data = synchronousClient.getAuthorToBooksMapSnapshot(Empty.newBuilder().build());
        return data.getPairListList().stream().collect(Collectors.toMap(AuthorBookPair::getAuthor, AuthorBookPair::getBookList));
    }


}
