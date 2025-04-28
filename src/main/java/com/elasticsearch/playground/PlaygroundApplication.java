package com.elasticsearch.playground;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class PlaygroundApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlaygroundApplication.class, args);
	}


	@Autowired
	private ProductRepository repository;

//	@PostConstruct
//	public void initData () {
//		var garments = this.readResource("elastic-config/data.json", new TypeReference<List<Product>>() {
//		});
//		this.repository.saveAll(garments);
//	}
//


	private static final Logger log = LoggerFactory.getLogger(PlaygroundApplication.class);
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ResourceLoader resourceLoader;


	protected <T> T readResource(String path, TypeReference<T> typeReference){
		try{
			var classpath = "classpath:" + path;
			var file = this.resourceLoader.getResource(classpath).getFile();
			return this.mapper.readValue(file, typeReference);
		}catch (Exception e){
			throw new RuntimeException(e);
		}
	}

	protected <T> Consumer<T> print(){
		return t -> log.info("{}", t);
	}

}
