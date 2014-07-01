package me.timothy.sexsells;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import com.github.jreddit.submissions.Submission;

public class SubredditSubmissionParser {
	public static ParsedSubredditPost parse(Submission submission, String subreddit) {
		try {
			return new ParsedSubredditPost(submission.getTitle(), submission.getAuthor(), submission.getFullName(),
					submission.getAuthorFlairCSSClass() != null && 
					(
							submission.getAuthorFlairCSSClass().toLowerCase().contains("trusted") ||
							submission.getAuthorFlairCSSClass().toLowerCase().contains("verified")
					), false);
		}catch(IOException | ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
