package me.timothy.sexsells;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SubredditPostDatabase {
	@SuppressWarnings("unused")
	private String subreddit;
	private File file;
	private List<ParsedSubredditPost> list;
	
	public SubredditPostDatabase(JSONParser parser, String subredditName, File file) {
		this.subreddit = subredditName;
		this.file = file;
		
		list = new ArrayList<ParsedSubredditPost>(500);
		
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("Failed to create " + file.getAbsolutePath());
				e.printStackTrace();
				System.exit(0);
			}
			
			JSONArray array = new JSONArray();
			try(FileWriter fw = new FileWriter(file)) {
				array.writeJSONString(fw);
			}catch(IOException ex) {
				System.out.println("Failed to write to " + file.getAbsolutePath());
				ex.printStackTrace();
				System.exit(0);
			}
			return;
		}
		JSONArray array = null;
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			array = (JSONArray) parser.parse(br);
		}catch(IOException | ParseException ex) {
			System.out.println("Failed to read+parse from " + file.getAbsolutePath());
			ex.printStackTrace();
			System.exit(0);
		}
		
		for(Object o : array) {
			list.add(ParsedSubredditPost.fromObject((JSONObject) o));
		}
	};
	
	public boolean addIfNotExists(ParsedSubredditPost post) {
		if(post == null)
			return false;
		
		if(!list.contains(post)) {
			list.add(post);
			return true;
		}
		return false;
	}
	
	public List<ParsedSubredditPost> getPosts() {
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public void save() {
		JSONArray array = new JSONArray();
		for(ParsedSubredditPost psp : list) {
			array.add(psp.toObject());
		}
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			array.writeJSONString(bw);
		}catch(IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
}
