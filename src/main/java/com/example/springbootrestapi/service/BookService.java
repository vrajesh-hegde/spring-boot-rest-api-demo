package com.example.springbootrestapi.service;

import com.example.springbootrestapi.dto.BookSearchResponse;
import com.example.springbootrestapi.entity.Book;
import com.example.springbootrestapi.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Book management. This class handles the logic for CRUD operations
 * and advanced search/filtering as per BOOKS-101.
 */
@Service
public class BookService {

    @Autowired
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Create or Update a Book
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    // Read: Get all books
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Read: Get a specific book
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    // Delete a book
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    /**
     * BOOKS-101: Advanced search with keyword, price range, pagination, and sorting.
     * Satisfies AC#1, AC#2, AC#3, AC#4, AC#5, AC#6, AC#7.
     *
     * @param keyword   partial, case-insensitive match against title and author (nullable)
     * @param minPrice  lower bound of price range, inclusive (nullable — defaults to 0.0)
     * @param maxPrice  upper bound of price range, inclusive (nullable — defaults to Double.MAX_VALUE)
     * @param page      0-based page index
     * @param size      number of results per page
     * @param sortBy    field to sort by: "title", "author", or "price"
     * @param direction "asc" or "desc"
     * @return BookSearchResponse envelope with pagination metadata
     * @throws IllegalArgumentException if minPrice > maxPrice (AC#7)
     */
    public BookSearchResponse searchBooks(String keyword, Double minPrice, Double maxPrice,
                                          int page, int size, String sortBy, String direction) {

        // AC#7: Validate price range
        double effectiveMin = (minPrice != null) ? minPrice : 0.0;
        double effectiveMax = (maxPrice != null) ? maxPrice : Double.MAX_VALUE;
        if (effectiveMin > effectiveMax) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }

        // AC#5: Build sort from sortBy + direction params; default to title asc
        String resolvedSortBy = resolveSortField(sortBy);
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(resolvedSortBy).descending()
                : Sort.by(resolvedSortBy).ascending();

        // AC#4: Build Pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Book> result;
        boolean hasKeyword  = keyword  != null && !keyword.isBlank();
        boolean hasPriceMin = minPrice != null;
        boolean hasPriceMax = maxPrice != null;

        // AC#3: Branch on which combination of filters is active
        if (hasKeyword && (hasPriceMin || hasPriceMax)) {
            // AC#1 + AC#2 combined
            result = bookRepository.searchByKeywordAndPriceRange(keyword, effectiveMin, effectiveMax, pageable);
        } else if (hasKeyword) {
            // AC#1: keyword only
            result = bookRepository.searchByKeyword(keyword, pageable);
        } else if (hasPriceMin || hasPriceMax) {
            // AC#2: price range only
            result = bookRepository.findByPriceBetween(effectiveMin, effectiveMax, pageable);
        } else {
            // No filters — return all books paged
            result = bookRepository.findAll(pageable);
        }

        // AC#6: Wrap in response envelope
        return BookSearchResponse.from(result);
    }

    /**
     * BOOKS-101 AC#5: Maps user-supplied sortBy string to actual entity field name,
     * defaulting to "title" for unrecognised values.
     */
    private String resolveSortField(String sortBy) {
        if (sortBy == null) return "title";
        return switch (sortBy.toLowerCase()) {
            case "author" -> "author";
            case "price"  -> "price";
            default       -> "title";
        };
    }

    // BAD: High cyclomatic complexity, magic numbers, vague variable names, no validation
    public double computeBookTierPrice(Book b, String t, int n) {
        double a = b.getPrice();
        if (t.equals("PREMIUM")) {
            if (n > 10) { a = a * 0.85; }
            else if (n > 5) { a = a * 0.90; }
            else if (n > 2) { a = a * 0.95; }
        } else if (t.equals("BUDGET")) {
            if (n > 50) { a = a * 0.70; }
            else if (n > 20) { a = a * 0.80; }
            else if (n > 5) { a = a * 0.90; }
        } else if (t.equals("VIP")) {
            a = a * 0.50;
        }
        if (a > 1000) { a = a - 100; }
        else if (a > 500) { a = a - 50; }
        return Math.round(a * 100.0) / 100.0;
    }

    // BAD: Code duplication - same logic as computeBookTierPrice but inlined differently
    public double getDiscountedPrice(Book b, String tier, int qty) {
        double p = b.getPrice();
        if (tier.equals("PREMIUM")) {
            if (qty > 10) p = p * 0.85;
            else if (qty > 5) p = p * 0.90;
            else if (qty > 2) p = p * 0.95;
        } else if (tier.equals("BUDGET")) {
            if (qty > 50) p = p * 0.70;
            else if (qty > 20) p = p * 0.80;
            else if (qty > 5) p = p * 0.90;
        } else if (tier.equals("VIP")) {
            p = p * 0.50;
        }
        if (p > 1000) p = p - 100;
        else if (p > 500) p = p - 50;
        return Math.round(p * 100.0) / 100.0;
    }

    // BAD: Poor error handling - swallows exception, returns null; performance: loads all then filters
    public List<Book> findBooksByCategory(String cat) {
        try {
            List<Book> all = bookRepository.findAll();
            List<Book> out = new java.util.ArrayList<>();
            for (Book x : all) {
                if (x.getTitle() != null && x.getTitle().toLowerCase().contains(cat.toLowerCase())) {
                    out.add(x);
                }
            }
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    // BAD: Dead code - never called
    @SuppressWarnings("unused")
    private String formatForDisplay(double v) {
        return "PRICE: " + v;
    }
}
