package com.elasticsearch.playground;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/search/suggest")
    public List<Product> getSuggest (@RequestParam("keyword") String keyword) {
        return productService.getSuggest(keyword);
    }
}
