import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class CraigsListing {
    public final String title;
    public final String link;
    public final String description;
    
    CraigsListing(String t, String l, String d) {
        title = t;
        link = l;
        description = d;
    }
    
    @Override
    public String toString() {
        return title + " " + link + " " + description;
    }
}

public class App {
    static final String SEARCH_PHRASE = "car";
    static final long SLEEP_TIME = 3000; // ms
    static final int ITERATIONS = 5;
    static final String CRAIGSLIST_BEST_URL = "https://www.craigslist.org/about/rss";
    static final String CRAIGSLIST_RSS_URL = "https://www.craigslist.org/about/best/all/index.rss";
    private Set<String> oldListings = new HashSet<>();
    
    void getCraigsListCookie() throws IOException {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        URLConnection connection = new URL(CRAIGSLIST_BEST_URL).openConnection();
        connection.connect(); // Get the session cookie
    }
    
    Document downloadCraigsListRss(String rssUrl) throws MalformedURLException, IOException, ParserConfigurationException, SAXException {
        getCraigsListCookie();
        URLConnection connection = new URL(rssUrl).openConnection();
        InputStream stream = connection.getInputStream();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(stream);
    }
    
    List<CraigsListing> searchBestListings(String searchPhrase) throws MalformedURLException, IOException, ParserConfigurationException, SAXException {
        Document doc = downloadCraigsListRss(CRAIGSLIST_RSS_URL);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("item");
        List<CraigsListing> results = new ArrayList<>();        
        for (int i = 0; i < nList.getLength(); i++) {
            Element listing = (Element) nList.item(i);
            String title = listing.getElementsByTagName("title").item(0).getTextContent();
            String link = listing.getElementsByTagName("link").item(0).getTextContent();
            String description = listing.getElementsByTagName("description").item(0).getTextContent();
            if(!oldListings.contains(link) && (title.contains(searchPhrase) || description.contains(searchPhrase))) {
                CraigsListing newListing = new CraigsListing(title,link,description);
                oldListings.add(link);
                results.add(newListing);                
            }
        }
        return results;
    }
    
    void start(String searchPhrase, long sleepTime, int loops) throws MalformedURLException, IOException, ParserConfigurationException, SAXException, InterruptedException {
        for(int i=0; i<loops; i++) {
            List<CraigsListing> results = searchBestListings(searchPhrase);
            if(!results.isEmpty()) {
                System.out.println("New results");
                for(CraigsListing result : results) {
                    System.out.println(result);
                }
            } else {
                System.out.println("No new results");
            }
            Thread.sleep(sleepTime);
        }                
    }
    
    public static void main(String args[]) throws IOException, ParserConfigurationException, SAXException, InterruptedException {
        App app = new App();
        app.start(SEARCH_PHRASE, SLEEP_TIME, ITERATIONS);
    }
}
