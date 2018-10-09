import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class WikiCrawler {
		
	private class WebNode {
		
		private String page;
		private ArrayList<WebNode> outEdge;
		
		public WebNode(String page) {
			this.page = page;
			outEdge = new ArrayList<WebNode>();
		}
		
		public void addEdge(WebNode edge) {
			outEdge.add(edge);
		}
	}
	
	public static final String BASE_URL = "https://en.wikipedia.org";
	
	private String seed;
	private int max;
	private String[] topics;
	private String output;
	
	/**
	 * Constructs new WikiCrawler with given parameters
	 * 
	 * @param seed  Relative address in wiki domain
	 * @param max  Maximum number of pages to consider
	 * @param topics  Array of strings representing keywords in topic-list
	 * @param output  Filename as string where web graph discovered pages are written
	 */
	public WikiCrawler(String seed, int max, String[] topics, String output) {
		this.seed = seed;
		this.max = max;
		this.topics = topics;
		this.output = output;
	}
	
	
	/**
	 * Extracts all wiki links from an HTML page
	 *  
	 * @param document  String representing HTML page
	 * @return  ALL wiki links within the HTML page in order they are encountered
	 */
	public ArrayList<String> extractLinks(String document) {
		ArrayList<String> links = new ArrayList<String>();
		boolean pTagFlag = false;
		StringBuilder tempLink;
		char curChar = 0;
		char linkChar = 0;
		
		for (int i = 0; i < document.length(); i++) {
			curChar = document.charAt(i);
			
			if (!pTagFlag) {	// Search for the first paragraph tags before extracting links
				if (curChar == '<') {
					i++;
					if (String.valueOf(document.charAt(i)).toLowerCase().equals("p")) {
						i++;
						if (document.charAt(i) == '>') {
							pTagFlag = true;
						}
					}
				}
			} else {	// Paragraph tag encountered so begin link extraction
				if (curChar == '/' && document.charAt(i+1) == 'w' && 
						document.charAt(i+2) == 'i' && document.charAt(i+3) == 'k' &&
						document.charAt(i+4) == 'i' && document.charAt(i+5) == '/') {	// Check for '/wiki/'
					
					i += 6;	// Skip over '/wiki/'
					tempLink = new StringBuilder("/wiki/");
					for (int j = i; j < document.length(); j++) {	// End loop once closing quote is found
						linkChar = document.charAt(j);
						
						if (linkChar == '#' || linkChar == ':') {	// Check for invalid characters
							break;
						} else if (linkChar == '"') {	// Check for ending quote of link
							links.add(tempLink.toString());
							break;
						} else {	// Add character to the link
							tempLink.append(String.valueOf(linkChar));
						}
					}
				} else if (document.startsWith("id=\"mw-navigation\"", i)) { // Check for navigation bar to skip over those links
					break;
				}
			}
		}
		return links;
	} // extractLinks
	
	
	/**
	 * Crawls/explores the web pages start from seed URL until
	 * max number of pages has been crawled
	 * 
	 * @param focused If false explore via BFS, else 
	 * @throws IOException  Malformed URL or Input Stream error
	 */
	public void crawl(boolean focused) throws IOException {		
		int pageCnt = 0;
		if (!focused) {
			ArrayList<String> fifoQueue = new ArrayList<String>();	// FIFO queue
			ArrayList<String> discovered = new ArrayList<String>();	// Keeps track of nodes which have been discovered
			
			fifoQueue.add(this.seed);	// Add root to queue
			discovered.add(this.seed);	// Add root to discovered
			String page;
			
			while (!fifoQueue.isEmpty()) {
				page = fifoQueue.remove(0);	// Extract top of the queue
				
				URL wikiURL = new URL(BASE_URL + page);
				BufferedReader br = new BufferedReader(new InputStreamReader(wikiURL.openStream()));
				StringBuilder htmlDoc = new StringBuilder();
				String temp;
				while ((temp = br.readLine()) != null) {
					htmlDoc.append(temp);
				}
				br.close();
				
				try {	// Adhere to politeness policy
					Thread.sleep((3 * 1000) / 20);
				} catch (InterruptedException e) {}
				
				for (String link : extractLinks(htmlDoc.toString())) {	// Extract outgoing edges
					if (!discovered.contains(link)) {
						if (topics.length == 0) {	// If no topics then all pages get explored
							fifoQueue.add(link);
							discovered.add(link);
						} else {	// Only relevant pages get added to the queue and explored
							
						}
					}
				}
			}
			
		} else {
			
		}
		// TODO
	}
	
	
	private int computeRelevance(String document) {
		
		// TODO
		return 0;
	}

}
