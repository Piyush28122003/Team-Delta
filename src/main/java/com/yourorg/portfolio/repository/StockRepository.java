package com.yourorg.portfolio.repository;

import com.yourorg.portfolio.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbol(String symbol);
    boolean existsBySymbol(String symbol);
    
    @Query("SELECT s FROM Stock s WHERE LOWER(s.symbol) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.companyName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Stock> searchBySymbolOrName(@Param("query") String query);
}

