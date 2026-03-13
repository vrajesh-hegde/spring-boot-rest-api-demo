package com.example.springbootrestapi.repository;

import com.example.springbootrestapi.entity.Book;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // BOOKS-101: Case-insensitive partial match on title OR author
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author, Pageable pageable);

    // BOOKS-101: Price range filter
    Page<Book> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    // BOOKS-101: Combined keyword + price range filter
    @Query("SELECT b FROM Book b WHERE " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND b.price BETWEEN :minPrice AND :maxPrice")
    Page<Book> searchByKeywordAndPriceRange(
            @Param("keyword") String keyword,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    // BOOKS-101: Keyword-only search (no price filter)
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // BOOKS-101: Price range with pagination (no keyword)
    Page<Book> findByPriceBetween(Double minPrice, Double maxPrice, Pageable p);

    // BAD: Native SQL with magic numbers and hardcoded string
    @Query(value = "SELECT * FROM books WHERE price > 99.99 AND author IS NOT NULL ORDER BY id", nativeQuery = true)
    List<Book> findExpensiveBooksNative();

    // BAD: Native SQL with magic string for "available" (no such column - example of hardcoded status)
    @Query(value = "SELECT * FROM books WHERE title LIKE '%fiction%'", nativeQuery = true)
    List<Book> findFictionBooks();
}
