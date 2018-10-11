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
		private ArrayList<String> edges;
		
		public WebNode() {
			explored = false;
			relevancy = -1;
			edges = new ArrayList<String>();
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
	 * Crawls/explores the web pages starting from seed URL until
	 * max number of pages has been crawled/discovered and prints
	 * graph edges to file denoted in this.output.
	 * 
	 * @param focused  If false explore via BFS, else use priority queue and explore most relevant pages 
	 * @throws IOException  Malformed URL or Input Stream error
	 */
	public void crawl(boolean focused) throws IOException {		
		int pageCnt = 1;
		int linkRelevance;
		int tempRelevance = 0;
		StringBuilder crawlOutput = new StringBuilder();
		URL wikiURL;
		BufferedReader br;
		StringBuilder htmlDoc;
			
		if (!focused) {
			ArrayList<LinkTuple> fifoQueue = new ArrayList<LinkTuple>();	// FIFO queue
			HashMap<String, WebNode> discovered = new HashMap<String, WebNode>();	// Keeps track of nodes which have been discovered
			
			fifoQueue.add(new LinkTuple(this.seed, null));	// Add root to queue
			discovered.put(this.seed, new WebNode());	// Add root to discovered
			LinkTuple page;
			
			while (!fifoQueue.isEmpty()) {
				page = fifoQueue.remove(0);	// Extract top of the queue
				
				if (page.parent != null) {
					crawlOutput.append(String.format("%s %s\n", page.parent, page.link));
				}
				
				WebNode curNode = discovered.get(page.link);

				if (!curNode.explored) {
					wikiURL = new URL(BASE_URL + page.link);
					br = new BufferedReader(new InputStreamReader(wikiURL.openStream()));
					htmlDoc = new StringBuilder();
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
								node = new WebNode();
								discovered.put(extractedLink, node);
								pageCnt++;
							} else {
								continue;
							}
						}
							
						if (topics == null || topics.length == 0) {	// If no topics then all pages get explored
							fifoQueue.add(new LinkTuple(extractedLink, page.link));
							
						} else {	// Only relevant pages get added to the queue and explored
							if (node.relevancy < 0) {  // Check if relevancy already computed
								linkRelevance = 0;
								
								wikiURL = new URL(BASE_URL + extractedLink);
								br = new BufferedReader(new InputStreamReader(wikiURL.openStream()));
								htmlDoc = new StringBuilder();
								String temp2;
								while ((temp2 = br.readLine()) != null) {
									htmlDoc.append(temp2);
								}
								br.close();
								
								try {	// Adhere to politeness policy
									Thread.sleep((3 * 1000) / 20);
								} catch (InterruptedException e) {}
								
								for (String topic : topics) {
									tempRelevance = computeRelevance(htmlDoc.toString(), topic);
									if (tempRelevance == 0) {  // If one of the topics not present then it isn't relevant
										break;
									}
									linkRelevance += tempRelevance;
								}
								node.relevancy = tempRelevance > 0 ? linkRelevance : 0;
							}
							if (node.relevancy > 0) {  // Relevance > 0 get added to the queue
								fifoQueue.add(new LinkTuple(extractedLink, page.link));
							}
						}
					}
					curNode.explored = true;
				}
			}
			
		} else {	// focused == true
			PriorityQ pQueue = new PriorityQ();	// Priority queue for storing nodes based on relevance
			HashMap<String, WebNode> discovered = new HashMap<String, WebNode>();	// Keeps track of nodes which have been discovered
			
			pQueue.add(this.seed, 0);
			discovered.put(this.seed, new WebNode());
			String page;
			
			while(!pQueue.isEmpty()) {
				page = pQueue.extractMax();	// Extract from top of heap
				
				WebNode curNode = discovered.get(page);
				if (!curNode.edges.isEmpty()) {
					crawlOutput.append(String.format("%s %s\n", curNode.edges.remove(0), page));
				}
				
				if (!curNode.explored) {
					wikiURL = new URL(BASE_URL + page);
					br = new BufferedReader(new InputStreamReader(wikiURL.openStream()));
					htmlDoc = new StringBuilder();
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
								node = new WebNode();
								discovered.put(extractedLink, node);
								pageCnt++;
							} else {
								continue;
							}
						}
						
						node.edges.add(page);
						
						if (topics == null || topics.length == 0) {	// If no topics then all pages get explored
							node.relevancy = 0;
							pQueue.add(extractedLink, 0);
							
						} else {	// Only relevant pages get added to the queue and explored
							if (node.relevancy < 0) {  // Check if relevance already computed
								linkRelevance = 0;
								
								wikiURL = new URL(BASE_URL + extractedLink);
								br = new BufferedReader(new InputStreamReader(wikiURL.openStream()));
								htmlDoc = new StringBuilder();
								String temp2;
								while ((temp2 = br.readLine()) != null) {
									htmlDoc.append(temp2);
								}
								br.close();
								
								try {	// Adhere to politeness policy
									Thread.sleep((3 * 1000) / 20);
								} catch (InterruptedException e) {}
								
								for (String topic : topics) {
									tempRelevance = computeRelevance(htmlDoc.toString(), topic);
									if (tempRelevance == 0) {  // If one of the topics not present then it isn't relevant
										break;
									}
									linkRelevance += tempRelevance;
								}
								node.relevancy = tempRelevance > 0 ? linkRelevance : 0;
							}
							if (node.relevancy > 0) {  // Relevance > 0 get added to the queue
								pQueue.add(extractedLink, node.relevancy);
							}
						}
					}
					curNode.explored = true;
				}
			}
		}
		try (Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "utf-8"))) {	// Write results
			fileWriter.write(pageCnt + "\n");
			fileWriter.write(crawlOutput.toString());
		}
	} // crawl
	
	
	/**
	 * Computes the relevance of a page based on the given topic
	 * 
	 * @param document  HTML page to search for keyword in
	 * @param topic  Keyword to match within the page
	 * @return  Number of times keyword is encountered in the page, 0 otherwise
	 */
	private int computeRelevance(String document, String topic) {
		boolean pTagFlag = false;
		boolean htmlTagEnd = false;
		int topicCnt = 0;
		char curChar = 0;
		String allowableChars = " /-\\;:\"@!()[]{}=+<>";
		
		for (int i = 0; i < document.length(); i++) {
			curChar = document.charAt(i);
			
			if (!pTagFlag) {	// Search for the first paragraph tags before extracting links
				if (curChar == '<') {
					i++;
					if (String.valueOf(document.charAt(i)).toLowerCase().equals("p")) {
						i++;
						if (document.charAt(i) == '>') {
							pTagFlag = true;
							htmlTagEnd = true;
						}
					}
				}
			} else {	// Paragraph tag encountered so begin link extraction
				if (curChar == '>') { 	// Check for end of html tag indicating text is visible
					htmlTagEnd = true;
					continue;
				} else if (curChar == '<') {
					htmlTagEnd = false;
				}
				if (htmlTagEnd) {
					if (document.startsWith(topic, i)) {
						if (allowableChars.indexOf(document.charAt(i-1)) == -1) {  // Check if it is its own word
							continue;
						} else if (allowableChars.indexOf(document.charAt(i+topic.length())) == -1) {
							continue;
						}
						topicCnt++;
						i += (topic.length() - 1);
					}
				}
			}
		}
		return topicCnt;
	} // computeRelevance

}
