package me.timothy.sexsells;

import org.json.simple.JSONObject;

public class ParsedSubredditPost {
	private final JSONObject object;
	private final String title;
	private final String author;
	private final String id;
	private final boolean trusted;
	
	private boolean respondedTo;
	
	@SuppressWarnings("unchecked")
	public ParsedSubredditPost(String title, String author, String id, boolean trusted, boolean respondedTo) {
		this.title = title;
		this.author = author;
		this.id = id;
		this.respondedTo = respondedTo;
		this.trusted = trusted;
		
		object = new JSONObject();
		object.put("title", title);
		object.put("author", author);
		object.put("id", id);
		object.put("trusted", trusted);
		object.put("respondedTo", respondedTo);
	}
	public String getTitle() {
		return title;
	}
	public String getAuthor() {
		return author;
	}
	public String getId() {
		return id;
	}
	public boolean trusted() {
		return trusted;
	}
	public boolean wasRespondedTo() {
		return respondedTo;
	}
	@SuppressWarnings("unchecked")
	public void setRespondedTo(boolean b) {
		respondedTo = b;
		object.put("respondedTo", b);
	}
	public JSONObject toObject() {
		return object;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParsedSubredditPost other = (ParsedSubredditPost) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public static ParsedSubredditPost fromObject(JSONObject jObject) {
		return new ParsedSubredditPost((String) jObject.get("title"), (String) jObject.get("author"), (String) jObject.get("id"), (boolean) jObject.get("trusted"), (boolean) jObject.get("respondedTo"));
	}
}
