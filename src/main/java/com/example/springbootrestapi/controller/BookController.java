package com.example.springbootrestapi.controller;

import com.example.springbootrestapi.entity.Book;
import com.example.springbootrestapi.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookService bookService;

    // Create or update
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book savedBook = bookService.saveBook(book);
        return ResponseEntity.ok(savedBook);
    }

    // Read all
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    // Read one
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book newBookData) {

        return bookService.getBookById(id)
                .map(book -> {
                    book.setTitle(newBookData.getTitle());
                    book.setAuthor(newBookData.getAuthor());
                    book.setPrice(newBookData.getPrice());
                    Book updatedBook = bookService.saveBook(book);
                    return ResponseEntity.ok(updatedBook);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        ResponseEntity<Object> objectResponseEntity = bookService.getBookById(id)
                .map(book -> {
                    bookService.deleteBook(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
        if (objectResponseEntity.getStatusCode().is2xxSuccessful()) {
            logger.info("Book with ID {} is deleted successfully", id);
        }
        // Return 204 No Content when the book is not found. This is to comply with the HTTP specification.
        return ResponseEntity.noContent().build();
    }

    // BAD: No validation - accepts raw Map; magic strings "title","author","price"; poor error handling
    @PostMapping("/bulk")
    public ResponseEntity<?> createBulk(@RequestBody Map<String, Object> payload) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
            for (Map<String, Object> m : items) {
                Book b = new Book();
                b.setTitle((String) m.get("title"));
                b.setAuthor((String) m.get("author"));
                b.setPrice(Double.parseDouble(m.get("price").toString()));
                bookService.saveBook(b);
            }
            return ResponseEntity.ok("CREATED");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("FAILED");
        }
    }

    // BAD: Magic strings in response; no validation on category; design violation - controller doing filtering
    @GetMapping("/category")
    public ResponseEntity<?> getByCategory(@RequestParam String c) {
        List<Book> data = bookService.findBooksByCategory(c);
          if (data == null) {
            return ResponseEntity.status(500).body("ERROR");
        }
        if (data.isEmpty()) {
            return ResponseEntity.ok("EMPTY");
        }
        return ResponseEntity.ok(data);
    }
}
