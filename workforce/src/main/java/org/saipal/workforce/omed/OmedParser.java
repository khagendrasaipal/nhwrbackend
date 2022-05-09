package org.saipal.workforce.omed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OmedParser {

	public static OMED parseDocument(String jsonStringMessage) {
		ObjectMapper obMapper = new ObjectMapper();
		obMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			OMED dataObject = obMapper.readValue(jsonStringMessage, OMED.class);
			return dataObject;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
	}

	public static String getJsonString(OMED doc) {
		try {
			ObjectMapper obMapper = new ObjectMapper();
			String json = obMapper.writeValueAsString(doc);
			return json;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
	}
}
