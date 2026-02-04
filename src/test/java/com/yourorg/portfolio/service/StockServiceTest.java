package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.StockPriceDTO;
import com.yourorg.portfolio.exception.ResourceNotFoundException;
import com.yourorg.portfolio.model.Stock;
import com.yourorg.portfolio.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService Unit Tests")
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockApiClient stockApiClient;

    @InjectMocks
    private StockService stockService;

    private Stock stock;
    private StockPriceDTO stockPriceDTO;

    @BeforeEach
    void setUp() {
        stock = new Stock("AAPL", "Apple Inc.");
        stock.setId(1L);
        stock.setCurrency("USD");

        stockPriceDTO = new StockPriceDTO();
        stockPriceDTO.setSymbol("AAPL");
        stockPriceDTO.setCompanyName("Apple Inc.");
        stockPriceDTO.setCurrentPrice(new BigDecimal("175.50"));
        stockPriceDTO.setCurrency("USD");
    }

    @Nested
    @DisplayName("getStockBySymbol")
    class GetStockBySymbolTests {
        @Test
        void shouldReturnStockWhenSymbolExists() {
            when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(stock));

            Stock result = stockService.getStockBySymbol("AAPL");

            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo("AAPL");
            assertThat(result.getCompanyName()).isEqualTo("Apple Inc.");
        }

        @Test
        void shouldThrowWhenSymbolNotFound() {
            when(stockRepository.findBySymbol("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockService.getStockBySymbol("UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getOrCreateStock")
    class GetOrCreateStockTests {
        @Test
        void shouldReturnExistingStockWhenFound() {
            when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(stock));

            Stock result = stockService.getOrCreateStock("AAPL");

            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo("AAPL");
            verify(stockApiClient, never()).fetchStockPrice(any());
            verify(stockRepository, never()).save(any());
        }

        @Test
        void shouldCreateAndSaveNewStockWhenNotFound() {
            when(stockRepository.findBySymbol("GOOGL")).thenReturn(Optional.empty());
            StockPriceDTO priceDTO = new StockPriceDTO();
            priceDTO.setSymbol("GOOGL");
            priceDTO.setCompanyName("Alphabet Inc.");
            priceDTO.setCurrentPrice(new BigDecimal("142.30"));
            when(stockApiClient.fetchStockPrice("GOOGL")).thenReturn(priceDTO);

            Stock newStock = new Stock("GOOGL", "Alphabet Inc.");
            newStock.setId(2L);
            when(stockRepository.save(any(Stock.class))).thenReturn(newStock);

            Stock result = stockService.getOrCreateStock("GOOGL");

            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo("GOOGL");
            verify(stockApiClient).fetchStockPrice("GOOGL");
            verify(stockRepository).save(any(Stock.class));
        }

        @Test
        void shouldUseSymbolAsCompanyNameWhenApiReturnsNullCompanyName() {
            when(stockRepository.findBySymbol("XYZ")).thenReturn(Optional.empty());
            StockPriceDTO priceDTO = new StockPriceDTO();
            priceDTO.setSymbol("XYZ");
            priceDTO.setCompanyName(null);
            when(stockApiClient.fetchStockPrice("XYZ")).thenReturn(priceDTO);

            Stock newStock = new Stock("XYZ", "XYZ Inc.");
            newStock.setId(3L);
            when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

            Stock result = stockService.getOrCreateStock("XYZ");

            assertThat(result).isNotNull();
            assertThat(result.getCompanyName()).isEqualTo("XYZ Inc.");
        }
    }

    @Nested
    @DisplayName("searchStocks")
    class SearchStocksTests {
        @Test
        void shouldReturnMatchingStocks() {
            when(stockRepository.searchBySymbolOrName("Apple")).thenReturn(List.of(stock));

            List<Stock> result = stockService.searchStocks("Apple");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSymbol()).isEqualTo("AAPL");
            verify(stockRepository).searchBySymbolOrName("Apple");
        }

        @Test
        void shouldReturnEmptyListWhenNoMatches() {
            when(stockRepository.searchBySymbolOrName("Unknown")).thenReturn(List.of());

            List<Stock> result = stockService.searchStocks("Unknown");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCurrentPrice")
    class GetCurrentPriceTests {
        @Test
        void shouldReturnPriceFromApiClient() {
            when(stockApiClient.fetchStockPrice("AAPL")).thenReturn(stockPriceDTO);

            StockPriceDTO result = stockService.getCurrentPrice("AAPL");

            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo("AAPL");
            assertThat(result.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("175.50"));
            verify(stockApiClient).fetchStockPrice("AAPL");
        }
    }

    @Nested
    @DisplayName("getTrendingStocks")
    class GetTrendingStocksTests {
        @Test
        void shouldReturnTrendingStocksFromApiClient() {
            when(stockApiClient.fetchTrendingStocks()).thenReturn(List.of(stockPriceDTO));

            List<StockPriceDTO> result = stockService.getTrendingStocks();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSymbol()).isEqualTo("AAPL");
            verify(stockApiClient).fetchTrendingStocks();
        }
    }

    @Nested
    @DisplayName("getAllStocks")
    class GetAllStocksTests {
        @Test
        void shouldReturnAllStocksFromRepository() {
            when(stockRepository.findAll()).thenReturn(List.of(stock));

            List<Stock> result = stockService.getAllStocks();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSymbol()).isEqualTo("AAPL");
        }
    }
}
