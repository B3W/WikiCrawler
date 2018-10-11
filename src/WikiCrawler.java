import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class WikiCrawler {
		
	private class WebNode {
		
		private boolean explored;
		private int relevancy;
		
		public WebNode() {
			explored = false;
			relevancy = -1;
		}
	}
	
	private class LinkTuple {
		
		private final String parent;
		private final String link;
		
		public LinkTuple(String link, String parent) {
			this.parent = parent;
			this.link = link;
		}
	}
	
	//public static final String BASE_URL = "https://en.wikipedia.org";
	public static final String BASE_URL = "http://web.cs.iastate.edu/~pavan";

	
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
		int pageCnt = 1;
		
		try (Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "utf-8"))) {
			fileWriter.write(max + "\n");
			
			if (!focused) {
				ArrayList<LinkTuple> fifoQueue = new ArrayList<LinkTuple>();	// FIFO queue CHANGE THIS TO ACCEPT PAGE AND PAGE'S PARENT
				HashMap<String, WebNode> discovered = new HashMap<String, WebNode>();	// Keeps track of nodes which have been discovered
				
				fifoQueue.add(new LinkTuple(this.seed, null));	// Add root to queue
				discovered.put(this.seed, new WebNode());	// Add root to discovered
				LinkTuple page;
				
				while (!fifoQueue.isEmpty()) {
					page = fifoQueue.remove(0);	// Extract top of the queue
					
					if (page.parent != null) {
						fileWriter.write(String.format("%s %s\n", page.parent, page.link));
					}
					
					WebNode curNode = discovered.get(page.link);

					if (!curNode.explored) {
						URL wikiURL = new URL(BASE_URL + page.link);
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
					
						for (String extractedLink : extractLinks(htmlDoc.toString())) {	// Extract outgoing edges
							WebNode node = discovered.get(extractedLink);
							if (node == null) {
								if (pageCnt < max) {
									discovered.put(extractedLink, new WebNode());
									pageCnt++;
								} else {
									continue;
								}
							}
								
							if (topics == null || topics.length == 0) {	// If no topics then all pages get explored
								fifoQueue.add(new LinkTuple(extractedLink, page.link));
							} else {	// Only relevant pages get added to the queue and explored
								
							}
						}
						curNode.explored = true;
					}
				}
				
			} else {
				// TODO
			}
		}
	}
	
	
	private int computeRelevance(String document) {
		
		// TODO
		return 0;
	}

}
