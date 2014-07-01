package me.timothy.sexsells;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;

import com.github.jreddit.submissions.Submission;
import com.github.jreddit.submissions.Submissions;
import com.github.jreddit.user.User;
import com.github.jreddit.user.UserInfo;
import com.github.jreddit.utils.ApiEndpointUtils;
import com.github.jreddit.utils.restclient.RestClient;

public class SSPoster {
	private SubredditPostDatabase majorDB;
	private SubredditPostDatabase[] minorDBs;
	private Submissions submissions;
	public SSPoster(SubredditPostDatabase major, SubredditPostDatabase[] minor) {
		majorDB = major;
		minorDBs = minor;
	}
	
	public int recheckDatabases(User user, RestClient client) {
		if(submissions == null)
			submissions = new Submissions(client);
		int responseCounter = 0;
		
		List<ParsedSubredditPost> majorPosts = majorDB.getPosts();
		for(ParsedSubredditPost post : majorPosts) {
			if(!post.wasRespondedTo()) {
				if(handlePost(user, client, post)) {
					post.setRespondedTo(true);
					responseCounter++;
				}
				try { Thread.sleep(2000); } catch(InterruptedException ie) { ie.printStackTrace(); }
			}
		}
		return responseCounter;
	};
	
	private boolean handlePost(User user, RestClient client, ParsedSubredditPost post) {
		List<ParsedSubredditPost> relevant = new ArrayList<>();
		
		for(int i = 0; i < minorDBs.length; i++) {
			SubredditPostDatabase mDB = minorDBs[i];
			
			List<ParsedSubredditPost> mDBPosts = mDB.getPosts();
			
			for(ParsedSubredditPost psp : mDBPosts) {
				if(psp.getAuthor().equalsIgnoreCase(post.getAuthor()) || psp.getTitle().toLowerCase().contains(post.getAuthor().toLowerCase())) {
					relevant.add(psp);
				}
			}
		}
		
		String responseText = getResponseText(client, user, post, relevant);
		Submission submission = new Submission(client);
		submission.setFullName(post.getId());
		
		try {
			submission.comment(user, responseText);
			Thread.sleep(5000);
		} catch (IOException | ParseException | InterruptedException e) {
			System.out.println("Failed to comment on " + post.getTitle() + " by " + post.getAuthor() + " (id " + post.getId() + ")");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private String getResponseText(RestClient client, User user, ParsedSubredditPost post, List<ParsedSubredditPost> relevant) {
		UserInfo authorInfo = User.about(client, post.getAuthor());
		
		String age = getPrettyTimeDiffUTC(authorInfo.getCreatedUTC());
		StringBuilder response = new StringBuilder();
		response.append("**Stats for /u/").append(post.getAuthor()).append("**\n");
		response.append("\n");
		response.append("---\n");
		response.append("\n");
		response.append("Status: **").append(post.trusted() ? "Trusted/Verified" : "Unknown").append("**\n");
		response.append("\n");
		response.append("* No. of Listings on similiar subreddits: ").append(relevant.size()).append("\n");
		response.append("\n");
		response.append("* Account Age: ").append(age).append(" --- Comment Karma: ").append(authorInfo.getCommentKarma())
			.append(" --- Link Karma: ").append(authorInfo.getLinkKarma()).append("\n\n");
		for(ParsedSubredditPost psp : relevant) {
			response.append("[").append(psp.getTitle()).append("](").append(ApiEndpointUtils.getFullnameFromLink(client, user, psp.getId())).append(")\n\n");
			try { Thread.sleep(2000); }catch(InterruptedException ie) { ie.printStackTrace(); }
		}
		response.append("\n");
		response.append("---\n");
		response.append("\n");
		response.append("New Buyer? Read our [Buying Basics Guide](/r/sexsells/w/buyingbasics)!\n");
		response.append("\n");
		response.append("---\n");
		response.append("\n");
		response.append("[[Report a Bug]][http://www.reddit.com/message/compose%3Fto%3Dneedfulthingss%26subject%3DBot%20Bug%20-%20")
		.append(post.getId()).append("] --- [[Message the Moderators]][http://www.reddit.com/message/compose%3Fto%3D%2Fr%2FSexsells]");
		return response.toString();
	}

	private String getPrettyTimeDiffUTC(double createdUTC) {
		long timeSeconds = (System.currentTimeMillis() / 1000) - ((long) createdUTC);
		long timeMinutes = timeSeconds / 60;
		long timeHours = timeMinutes / 60;
		long timeDays = timeHours / 24;
		long timeMonths = timeDays / 30;
		
		if(timeDays % 30 >= 15 && timeMonths >= 1)
			timeMonths++;
		
		if(timeMonths < 1) 
			return "< 1 Month";
		else
			return "~" + timeMonths + " Months";
	}
}
