package com.elasticsearch.playground;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {


    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public List<Product> getSuggest(String keyword) {
            return null;
    }
}
