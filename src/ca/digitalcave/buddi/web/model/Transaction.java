package ca.digitalcave.buddi.web.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.web.util.FormatUtil;

public class Transaction {
	private Long id;
	private String uuid;
	private int userId;
	private String description;
	private String number;
	private Date date;
	private boolean deleted;
	private Date created;
	private Date modified;
	private List<Split> splits;
	
	public Transaction() {
	}
	public Transaction(JSONObject json) throws JSONException {
		this.setId(json.has("id") ? json.getLong("id") : null);
		this.setUuid(json.has("uuid") ? json.getString("uuid") : UUID.randomUUID().toString());
		this.setDescription(json.getString("description"));
		this.setNumber(json.has("number") ? json.getString("number") : null);
		this.setDate(json.has("date") ? FormatUtil.parseDate(json.getString("date")) : null);
		this.setDeleted(json.has("deleted") ? json.getBoolean("deleted") : false);
		final List<Split> splits = new ArrayList<Split>();
		for (int i = 0; i < json.getJSONArray("splits").length(); i++){
			splits.add(new Split(json.getJSONArray("splits").getJSONObject(i)));
		}
		this.setSplits(splits);
	}
	
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("userId", this.getUserId());
		result.put("uuid", this.getUuid());
		result.put("description", this.getDescription());
		result.put("number", this.getNumber());
		result.put("date", FormatUtil.formatDate((Date) this.getDate()));
		result.put("deleted", this.isDeleted());
		JSONArray splits = new JSONArray();
		if (getSplits() != null){
			for (Split split : getSplits()) {
				splits.put(split.toJson());
			}
		}
		result.put("splits", splits);
		result.put("created", FormatUtil.formatDateTime((Date) this.getCreated()));
		result.put("modified", FormatUtil.formatDateTime((Date) this.getModified()));
		return result;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public List<Split> getSplits() {
		return splits;
	}
	public void setSplits(List<Split> splits) {
		this.splits = splits;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	
	@Override
	public String toString() {
		try {
			return toJson().toString();
		}
		catch (JSONException e){return "Error converting to JSON";}
	}

	
	//The following are calculated convenience methods, and are not stored in the DB.
	public long getAmount(){
		long total = 0;
		for (Split split : splits) {
			total += split.getAmount();
		}
		return total;
	}
	public String getFrom(Map<Integer, Source> s){
		if (splits.size() == 0){
			return null;
		}
		else if (splits.size() == 1){
			return s.get(splits.get(0).getFromSource()).getName();
		}
		else {
			Set<String> sources = new HashSet<String>();
			for (Split split : splits) {
				sources.add(s.get(split.getFromSource()).getName());
			}
			if (sources.size() == 1) return sources.toArray(new String[1])[0];
			else return "MULTIPLE";
		}
	}
	public String getTo(Map<Integer, Source> s){
		if (splits.size() == 0){
			return null;
		}
		else if (splits.size() == 1){
			return s.get(splits.get(0).getToSource()).getName();
		}
		else {
			Set<String> sources = new HashSet<String>();
			for (Split split : splits) {
				sources.add(s.get(split.getToSource()).getName());
			}
			if (sources.size() == 1) return sources.toArray(new String[1])[0];
			else return "MULTIPLE";
		}
	}
}
