package me.timothy.sexsells;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.json.simple.parser.JSONParser;

import com.github.jreddit.user.User;
import com.github.jreddit.utils.ApiEndpointUtils;
import com.github.jreddit.utils.restclient.HttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

public class SexSellsMain {
	/**
	 * The main subreddit to scan.
	 */
	private static final String MAJOR_SUBREDDIT = "sexsells";

	/**
	 * These subreddits are checked for posts and have appropriately
	 * updated databases. They are included in the comment to add to
	 * the "honorableness" of the postee.
	 */
	private static final String[] MINOR_SUBREDDITS =
		{
		"fetishitems", "pantyselling", "camshow", "skypeshows", "sexting", "usedpanties"
		};

	private String username;
	private String password;

	public static void main(String[] args) {
		new SexSellsMain().run();
	}

	private void run() {
		loadConfiguration();

		JSONParser parser = new JSONParser();
		SubredditPostDatabase majorDB = new SubredditPostDatabase(parser, MAJOR_SUBREDDIT, new File(MAJOR_SUBREDDIT + ".json"));
		SubredditScanner majorScanner = new SubredditScanner(MAJOR_SUBREDDIT, majorDB);

		SubredditPostDatabase[] minorDbs = new SubredditPostDatabase[MINOR_SUBREDDITS.length];
		SubredditScanner[] minorScanners = new SubredditScanner[MINOR_SUBREDDITS.length];
		
		SSPoster poster = new SSPoster(majorDB, minorDbs);

		for(int i = 0; i < MINOR_SUBREDDITS.length; i++) {
			minorDbs[i] = new SubredditPostDatabase(parser, MINOR_SUBREDDITS[i], new File(MINOR_SUBREDDITS[i] + ".json"));
			minorScanners[i] = new SubredditScanner(MINOR_SUBREDDITS[i], minorDbs[i]);
		}

		RestClient client = new HttpRestClient();
		client.setUserAgent("/r/SexSells bot by /u/Tjstretchalot");

		User user = new User(client, username, password);
		try {
			user.connect();
		}catch(OutOfMemoryError mex) {
			mex.printStackTrace();
			System.exit(0);
		}catch(Exception ex) {
			System.err.println("Failed to connect user!");
			ex.printStackTrace();
			return;
		}

		System.out.println("Successfully authd as " + username);
		sleepFor(2000);
		
		while(true) {
			System.out.println("Beggining scanning: ");
			for(int i = 0; i < MINOR_SUBREDDITS.length; i++) {
				System.out.print("  Scanning " + MINOR_SUBREDDITS[i] + "..");
				int ctr = minorScanners[i].scan(user, client);
				System.out.println(" got " + ctr + " new");
				minorDbs[i].save();
				sleepFor(2000);
			}
			System.out.print("  Scanning " + MAJOR_SUBREDDIT + "..");
			int ctr = majorScanner.scan(user, client);
			System.out.println(" got " + ctr + " new");
			majorDB.save();
			System.out.println("Done scanning");
			System.out.println("Considering responding");
			
			sleepFor(2000);
			int nResponses = poster.recheckDatabases(user, client);
			if(nResponses > 0)
				majorDB.save();
			sleepFor(2000);
			
			System.out.println("Responded to " + nResponses + " posts");
		}
	}
	private void loadConfiguration() {
		File file = new File("user.ini");
		if(!file.exists()) {
			System.out.println("Please create user.ini that resembles:");
			System.out.println("username=asdf");
			System.out.println("password=ghjk");
			System.exit(0);
		}

		Properties props = new Properties();
		try(FileReader in = new FileReader(file)) {
			props.load(in);
		}catch(IOException ex) {
			ex.printStackTrace();
			System.exit(0);
		}

		if(!(props.containsKey("username")) || !(props.containsKey("password"))) {
			System.out.println("user.ini needs to have both username and password!");
			System.exit(0);
		}

		username = props.getProperty("username");
		password = props.getProperty("password");
	}
	
	private void sleepFor(int ms) {
		try { Thread.sleep(ms); } catch(InterruptedException ie) { ie.printStackTrace(); System.exit(0); }
	}
}
