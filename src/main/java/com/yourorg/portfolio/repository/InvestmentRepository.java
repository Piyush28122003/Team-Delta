package com.yourorg.portfolio.repository;

import com.yourorg.portfolio.model.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    List<Investment> findByPortfolioId(Long portfolioId);
    
    @Query("SELECT i FROM Investment i WHERE i.portfolio.user.id = :userId")
    List<Investment> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT i FROM Investment i WHERE i.portfolio.id = :portfolioId AND i.stock.symbol = :symbol")
    List<Investment> findByPortfolioIdAndStockSymbol(@Param("portfolioId") Long portfolioId, @Param("symbol") String symbol);
}

