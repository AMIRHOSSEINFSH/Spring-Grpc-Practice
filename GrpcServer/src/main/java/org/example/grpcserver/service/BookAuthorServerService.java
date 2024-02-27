package org.example.grpcserver.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.val;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.grpcserver.db.TempDb;
import org.example.grpcserver.proto.BookAuthorServiceGrpc;
import org.example.grpcserver.proto.Models.*;
import org.springframework.beans.factory.annotation.Autowired;


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
    public void getBookListOfAuthorByAuthorId(AuthorIdRequest request, StreamObserver<BookListResponse> responseObserver) {
        val result = tmpDb.getBookListOfAuthorByAuthorId(request.getAuthorId());
        responseObserver.onNext(BookListResponse.newBuilder().addAllBooks(result).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAuthorToBooksMapSnapshot(Empty request, StreamObserver<AuthorBookListMapResponse> responseObserver) {
        val result=  tmpDb.getAuthorToBooksMapSnapshot()
                .entrySet()
                .stream()
                        .map(authorListEntry ->
                                AuthorBookPair.newBuilder()
                                        .setAuthor(authorListEntry.getKey())
                                        .addAllBook(authorListEntry.getValue())
                                        .build()
                        ).toList();
        responseObserver.onNext(AuthorBookListMapResponse.newBuilder().addAllPairList(result).build());
        responseObserver.onCompleted();
    }
}
