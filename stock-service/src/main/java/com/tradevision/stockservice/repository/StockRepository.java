package com.tradevision.stockservice.repository;

import com.tradevision.stockservice.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);

    List<Stock> findAllByOrderBySymbolAsc();

    @Query("SELECT s FROM Stock s WHERE LOWER(s.symbol) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(s.companyName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Stock> searchBySymbolOrCompanyName(String query);

    boolean existsBySymbol(String symbol);
}
