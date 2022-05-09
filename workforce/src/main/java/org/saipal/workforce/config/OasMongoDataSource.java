package org.saipal.workforce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class OasMongoDataSource {
	
	@Value("${oas.datasource.mongoUri}")
	private String connString;

	@Bean("oasmongo")
	public MongoClient mongoClient() {
		return MongoClients.create(connString);
	}
	
	@Bean("oasMonogoTemplate")
	public MongoTemplate oasMongoTemplate() {
		return new MongoTemplate(mongoClient(), "oas");
	}
}
