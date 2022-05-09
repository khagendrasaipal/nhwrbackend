package org.saipal.workforce.omed;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.result.InsertOneResult;

@Component
public class DocumentStorageService {
	
	@Autowired
	@Qualifier(value="oasMonogoTemplate")
	MongoTemplate oasMongoTemplate;
	
	public String storeDocument(OMED doc) {
		String jsonString = OmedParser.getJsonString(doc);
		Document d = Document.parse(jsonString);
		InsertOneResult result = oasMongoTemplate.getCollection("omed").insertOne(d);
		return result.getInsertedId().asObjectId().getValue().toString();
	}
	
	public OMED getDocumentById(String id) {
		Document d = new Document();
		d.append("_id", new ObjectId(id));
		String doc = oasMongoTemplate.getCollection("omed").find(d).first().toJson();
		return OmedParser.parseDocument(doc);
	}
	
	public OMED getDocumentByMessageId(String id) {
		Document d = new Document();
		d.append("messageid", id);
		String doc = oasMongoTemplate.getCollection("omed").find(d).first().toJson();
		return OmedParser.parseDocument(doc);
	}
	
	public String getJsonById(String id) {
		Document d = new Document();
		d.append("_id", new ObjectId(id));
		return oasMongoTemplate.getCollection("omed").find(d).first().toJson();
	}
	
	public String getJsonByMessageId(String id) {
		Document d = new Document();
		d.append("messageid", id);
		return oasMongoTemplate.getCollection("omed").find(d).first().toJson();
	}
}
