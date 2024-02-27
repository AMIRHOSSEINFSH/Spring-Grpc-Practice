package org.example.grpcclient.service;


import com.google.protobuf.Empty;
import lombok.val;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.grpcserver.proto.BookAuthorServiceGrpc;
import org.example.grpcserver.proto.Models.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class BookAuthorClientService {

    @GrpcClient("grpcServer")
    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub synchronousClient;

    public Author getAuthorByAuthorId(int authorId) {
        val authorRequestModel = AuthorIdRequest.newBuilder().setAuthorId(authorId).build();
        return synchronousClient.getAuthorByAuthorId(authorRequestModel);
    }

    public List<Author> getAuthorListByBookId(int bookId) {
        val bookRequestModel = BookIdRequest.newBuilder().setBookId(bookId).build();
        return synchronousClient.getAuthorsByBookId(bookRequestModel).getAuthorsList();
    }

    public List<Book> getBookListOfAuthorByAuthorId(int authorId) {
        val authorRequestModel = AuthorIdRequest.newBuilder().setAuthorId(authorId).build();
        return synchronousClient.getBookListOfAuthorByAuthorId(authorRequestModel).getBooksList();

    }

    public Map<Author, List<Book>> getLibrarySnapShot() {
        val data = synchronousClient.getAuthorToBooksMapSnapshot(Empty.newBuilder().build());
        return data.getPairListList().stream().collect(Collectors.toMap(AuthorBookPair::getAuthor, AuthorBookPair::getBookList));
    }

}
