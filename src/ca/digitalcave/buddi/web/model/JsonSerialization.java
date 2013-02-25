package ca.digitalcave.buddi.web.model;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonSerialization {

	public JSONObject toJson() throws JSONException;
	
	public Object fromJson(JSONObject serialized) throws JSONException;
}
