import java.util.ArrayList;

public class WikiCrawler {
	
	private class FIFOQueue<T> {
		
		private ArrayList<T> queue;
		
		public FIFOQueue() {
			queue = new ArrayList<T>();
		}
		
		/**
		 * Add a new item into the queue
		 * 
		 * @param item  Item to be added into the queue
		 */
		public void add(T item) {
			queue.add(item);
		}
		
		/**
		 * Get the first item from the queue and remove it
		 * 
		 * @return  First item in the queue
		 */
		public T extractTop() {
			if (queue.size() == 0) {
				return null;
			}
			return queue.remove(0);
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
	 */
	public void crawl(boolean focused) {
		// TODO
	}

}
