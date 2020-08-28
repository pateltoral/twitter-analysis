package TwitterSentiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class SearchSentiment extends HttpServlet {

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, TwitterException {
		response.setContentType("text/html;charset=UTF-8");
		try (PrintWriter out = response.getWriter()) {
			/* TODO output your page here. You may use following sample code. */
			String search = request.getParameter("txtSearch");

			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setOAuthConsumerKey("**************************");
			cb.setOAuthConsumerSecret("*******************************************");
			cb.setOAuthAccessToken("**************************************");
			cb.setOAuthAccessTokenSecret("*******************************************");

			Twitter twitter = new TwitterFactory(cb.build()).getInstance();
			Query query = new Query("#" + search);
			int numberOfTweets = 100;
			long lastID = Long.MAX_VALUE;
			ArrayList<Status> tweets = new ArrayList<Status>();

			String fileName = "/var/lib/tomcat9/webapps/twitterData.txt";
			FileWriter fileWriter = new FileWriter(fileName);
			fileWriter.flush();
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			String text = "";

			if (numberOfTweets - tweets.size() > 100) {
				query.setCount(100);
			} else {
				query.setCount(numberOfTweets - tweets.size());
			}
			try {
				QueryResult result = twitter.search(query);
				for (Status tweet : result.getTweets()) {
					Status t = (Status) tweet;
					GeoLocation loc = t.getGeoLocation();
					String user = t.getUser().getScreenName();
					String msg = t.getText();

					if (loc != null) {
						Double lat = t.getGeoLocation().getLatitude();
						Double lon = t.getGeoLocation().getLongitude();
						text = user + ":" + msg + " located at " + lat + ", " + lon + "|";
					} else {
						text = user + ":" + msg + "|";
					}
					text = text + "\n";

					bufferedWriter.write(text);
				}

				if (result.getCount() > 0) {
					// Hadoop Portion
					HadoopProcessingClass obj = new HadoopProcessingClass();
					String startProcess = "nohup sh -x /var/lib/tomcat9/webapps/TwitterAnalysis.sh  > temp.log  2>&1 &";
					String start = obj.startprocess(startProcess);

					// End
					// Reading Data
					BufferedReader in = new BufferedReader(
							new FileReader("/var/lib/tomcat9/webapps/TwitterResult.txt"));
					String str;
					while ((str = in.readLine()) != null) {
						out.print(str);
					}
					in.close();
				}
				// End
			} catch (TwitterException te) {
				if (te.exceededRateLimitation()) {
					out.print("Sucess " + te);
				}
			} catch (IOException e) {
				out.print(e);
			} finally {
				bufferedWriter.close();
				fileWriter.close();
			}
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the
	// + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request  servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch (TwitterException ex) {
			Logger.getLogger(Twitter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request  servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch (TwitterException ex) {
			Logger.getLogger(Twitter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>

}
