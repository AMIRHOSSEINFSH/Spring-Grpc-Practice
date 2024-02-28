package org.example.grpcserver.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.val;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.grpcserver.db.TempDb;
import org.example.grpcserver.proto.BookAuthorServiceGrpc;
import org.example.grpcserver.proto.Models.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


@GrpcService
public class BookAuthorServerService extends BookAuthorServiceGrpc.BookAuthorServiceImplBase {

    private TempDb tmpDb;

    @Autowired
    public BookAuthorServerService(TempDb tmpDb) {
        this.tmpDb = tmpDb;
    }


    @Override
    public void getAuthorByAuthorId(AuthorIdRequest request, StreamObserver<Author> responseObserver) {
        val result = tmpDb.getAuthorByAuthorId(request.getAuthorId());
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    @Override
    public void getAuthorsByBookId(BookIdRequest request, StreamObserver<AuthorListResponse> responseObserver) {
        val result = tmpDb.getAuthorsByBookId(request.getBookId());
        responseObserver.onNext(AuthorListResponse.newBuilder().addAllAuthors(result).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getBookListOfAuthorByAuthorId(AuthorIdRequest request, StreamObserver<Book> responseObserver) {
        List<Book> books = tmpDb.getBookListOfAuthorByAuthorId(request.getAuthorId());
        for (Book book : books) {
            responseObserver.onNext(book);
        }
        responseObserver.onCompleted();
    }


    @Override
    public void getAuthorToBooksMapSnapshot(Empty request, StreamObserver<AuthorBookListMapResponse> responseObserver) {
        val result = tmpDb.getAuthorToBooksMapSnapshot().entrySet().stream().map(authorListEntry -> AuthorBookPair.newBuilder().setAuthor(authorListEntry.getKey()).addAllBook(authorListEntry.getValue()).build()).toList();
        responseObserver.onNext(AuthorBookListMapResponse.newBuilder().addAllPairList(result).build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<BookIdRequest> getMostAttendeeAuthorsForBook(StreamObserver<Book> responseObserver) {
        return new StreamObserver<>() {
            int mostAttendee = 0;
             Book resultBook;

            @Override
            public void onNext(BookIdRequest bookIdRequest) {
                List<Book> bookList = tmpDb.getBookList();
                for (final Book currentBook : bookList) {
                    if (currentBook.getAuthorListList().size() >= mostAttendee) {
                        mostAttendee = currentBook.getAuthorListList().size();
                        resultBook = currentBook;

                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(resultBook);
                responseObserver.onCompleted();
            }
        };


    }

    @Override
    public StreamObserver<BookIdRequest> getBooksWithSameAuthor(StreamObserver<AuthorBookPair> responseObserver) {
        return new StreamObserver<>() {
            final List<AuthorBookPair> responsePairList = new ArrayList<>();
            @Override
            public void onNext(BookIdRequest bookIdRequest) {
                val authorList= tmpDb.getBookList()
                        .stream()
                        .filter(book -> book.getBookId() == bookIdRequest.getBookId())
                        .findFirst()
                        .orElse(Book.getDefaultInstance())
                        .getAuthorListList();

                for (final Author currentAuthor : authorList) {
                    val bookList = tmpDb.getAuthorToBooksMapSnapshot().get(currentAuthor)
                            .stream()
                            .filter(book -> book.getBookId() != bookIdRequest.getBookId()).toList();
                    if (!bookList.isEmpty()) {
                        val responseModel = AuthorBookPair.newBuilder().setAuthor(currentAuthor).addAllBook(bookList).build();
                        responsePairList.add(responseModel);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responsePairList.forEach(responseObserver::onNext);
                responseObserver.onCompleted();
            }
        };
    }
}
