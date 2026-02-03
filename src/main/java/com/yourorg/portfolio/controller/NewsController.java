package com.yourorg.portfolio.controller;

import com.yourorg.portfolio.dto.NewsArticleDTO;
import com.yourorg.portfolio.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/stocks")
    public ResponseEntity<List<NewsArticleDTO>> getStockNews() {
        List<NewsArticleDTO> articles = newsService.getStockNews();
        return ResponseEntity.ok(articles);
    }
}
