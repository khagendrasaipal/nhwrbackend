package org.saipal.workforce.files;

import java.io.IOException;
import java.util.Map;

import org.bson.types.ObjectId;
import org.saipal.fmisutil.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;

@Component
public class FileRepo {
	@Autowired
	private GridFsTemplate gridFsTemplate;

	@Autowired
	private GridFsOperations operations;

//	public String addFile(MultipartFile file) throws IOException {
//		DBObject metaData = new BasicDBObject();
//		ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(),
//				metaData);
////		System.out.println(id.toString());
////		return id.toString();
//	}
	
	public ResponseEntity<Map<String, Object>> addFile(MultipartFile file) throws IOException {
		DBObject metaData = new BasicDBObject();
		ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(),
				metaData);
//		System.out.println(id.toString());
//		return id.toString();
		return Messenger.getMessenger().setData(id.toString()).success();
	}

	public MediaFile getFile(String id) throws IllegalStateException, IOException {
		GridFSFile gfile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
		MediaFile file = new MediaFile();
		file.name = operations.getResource(gfile).getFilename();
		file.type = gfile.getMetadata().getString("_contentType");
		file.fileStream = operations.getResource(gfile).getInputStream();
		return file;
	}

	public boolean deleteFile(String id) {
		gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
		GridFSFile gfile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
		if (gfile == null) {
			return true;
		}
		return false;
	}

	public String replaceFile(String id, MultipartFile file) {
		try {
			DBObject metaData = new BasicDBObject();
			ObjectId retId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(),
					file.getContentType(), metaData);
			gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
			return retId.toString();
		} catch (IOException e) {
			return "";
		}
	}
}
