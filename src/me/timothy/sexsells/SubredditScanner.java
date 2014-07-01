package me.timothy.sexsells;

import java.io.IOException;
import java.util.List;

import org.json.simple.parser.ParseException;

import com.github.jreddit.submissions.Submission;
import com.github.jreddit.submissions.Submissions;
import com.github.jreddit.submissions.Submissions.Popularity;
import com.github.jreddit.user.User;
import com.github.jreddit.utils.restclient.RestClient;

/**
 * Scans subreddits and adds to a database. Subreddit scanners only
 * scan with a call to {@link me.timothy.sexsells.SubredditScanner#scan(User, RestClient)}.
 * 
 * @author Timothy
 */
public class SubredditScanner {
	private Submissions submissions;
	private String subreddit;
	private SubredditPostDatabase database;
	
	public SubredditScanner(String subreddit, SubredditPostDatabase db) {
		this.subreddit = subreddit;
		this.database = db;
	};

	/**
	 * Scans the subreddit for any new posts and adds them to the
	 * database
	 * @param user the user
	 * @param client the client
	 * @return the number of new posts
	 */
	public int scan(User user, RestClient client) {
		if(submissions == null)
			submissions = new Submissions(client);
		
		List<Submission> submissionsInSubreddit = null;
		try {
			submissionsInSubreddit = submissions.getSubmissions(subreddit, Popularity.NEW, null, user);
		} catch (IOException | ParseException e) {
			System.out.println("Failed to get submissions in " + subreddit);
			e.printStackTrace();
			return 0;
		}
		int cnter = 0;
		for(Submission submission : submissionsInSubreddit) {
			if(database.addIfNotExists(SubredditSubmissionParser.parse(submission, subreddit)))
				cnter++;
		}
		return cnter;
	};
}
