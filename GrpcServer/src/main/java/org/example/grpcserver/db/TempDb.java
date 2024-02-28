package org.example.grpcserver.db;

import lombok.Getter;
import lombok.val;
import org.example.grpcserver.proto.Models.*;

import java.util.*;

public class TempDb {

    @Getter
    private final List<Book> bookList;
    private final List<Author> authorList;
    private final Random rand = new Random();

    public TempDb() {
        ArrayList<Book> tmpBookList = new ArrayList<>();
        ArrayList<Author> tmpAuthorList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            tmpAuthorList.add(Author.newBuilder().setAuthorId(i).build());
            /*var author = Author.newBuilder().setAuthorId(i).build();
            tmpAuthorList.add(author);*/
        }
        for (int i = 1; i <= 10; i++) {
            List<Author> authorList = generateRandomAuthorListData(tmpAuthorList);
            if (authorList == null) authorList = Collections.emptyList();
            int bookId = i * 10;
            val bookBuilder = Book.newBuilder().setBookId(bookId);

            bookBuilder.addAllAuthorList(authorList);

            Book book = bookBuilder.build();

            /*for (Author value : authorList) {
                val authorId = value.getAuthorId();
                tmpAuthorList.stream().filter(author2 -> author2.getAuthorId() == authorId).findFirst()
                        .ifPresent(author -> tmpAuthorList.set(tmpAuthorList.indexOf(author), author.toBuilder().addBooks(book).build()));

            }*/


            tmpBookList.add(book);
        }
        bookList = tmpBookList;
        authorList = tmpAuthorList;
    }

    private List<Author> generateRandomAuthorListData(List<Author> authorList) {
        Set<Integer> authorIdList = new HashSet<>();
        if (authorList.isEmpty()) return null;

        for (int j = 0; j < rand.nextInt(1, 3); j++) {
            val authorId = authorList.get(rand.nextInt(0, authorList.size() - 1)).getAuthorId();
            authorIdList.add(authorId);
        }

        return authorList.stream().filter(author -> authorIdList.contains(author.getAuthorId())).toList();
    }

    public Author getAuthorByAuthorId(int authorId) {
        return authorList.stream().filter(author -> author.getAuthorId() == authorId).findFirst().orElseThrow();
    }

    public List<Author> getAuthorsByBookId(int bookId) {
        return bookList.stream().filter(book -> book.getBookId() == bookId).findFirst().orElseThrow().getAuthorListList();
    }

    public List<Book> getBookListOfAuthorByAuthorId(int authorId) {
        val author = authorList.stream().filter(author1 -> author1.getAuthorId() == authorId).findFirst().orElseThrow();
        return bookList.stream().filter(book -> book.getAuthorListList().contains(author)).toList();
    }

    public Map<Author, List<Book>> getAuthorToBooksMapSnapshot() {
        Map<Author, List<Book>> resultMap = new HashMap<>();
        for (Author value : authorList) {
            val authorId = value.getAuthorId();
            List<Book> bookList = new ArrayList<>();
            for (Book book : this.bookList) {
                val isMatch = book.getAuthorListList().stream().anyMatch(author -> author.getAuthorId() == authorId);
                if (isMatch) bookList.add(book);
            }
            resultMap.put(value, bookList);
        }
        return resultMap;
    }

}
