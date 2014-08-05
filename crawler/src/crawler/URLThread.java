package crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class URLThread implements Callable {
	String URL;
	String result;
	URL url;
	HttpURLConnection conn;
	List<String> list = new ArrayList<String>();
	public static boolean madeDir = new File("html/").mkdir(); //to hold all html
	
	public URLThread(String URL){
		this.URL = URL;
	}
	
	@Override
	@SuppressWarnings("CallToThreadDumpStack")
	public List<String> call() throws IOException{
		try {
			url = new URL(URL);
		} catch (MalformedURLException ex) {
			Logger.getLogger(URLThread.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		BufferedReader rd;
		String line;
		result = new String();
		try{
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while((line = rd.readLine()) != null){
				result += line;
				result += '\n';
			}
			rd.close();
		}catch(Exception e){
			//e.printStackTrace();
			return list;
		}
		
		if(!result.isEmpty()){
			int docID = Crawler.getdocID();
			//the "/" is added so that it writes to the correct directory
			FileWriter file = new FileWriter(Crawler.getDir() + "/"+ Integer.toString(docID) + ".txt");
			file.write(result);
			file.close();
            if(docID % 100 == 0){
            	Crawler.printmetrics();
            }
            Crawler.insertpid(url.toString(), docID);
			Document doc = Jsoup.parse(result);
			Elements resultLinks = doc.select("a[href]");
			for(Element link : resultLinks){
				String theurl = link.attr("abs:href");
				if(!theurl.contains(".gov") && !theurl.contains("#") && !theurl.contains(".jpg")
						&& !theurl.contains(".jpeg") && !theurl.contains(".png") && !theurl.contains(".gif")
						&& !theurl.contains(".flv") && !theurl.contains(".mp3") && !theurl.contains(".mkv")
						&& !theurl.contains(".mpeg") && !theurl.contains(".aac") && !theurl.contains(".wav")
						&& !theurl.contains(".avi") && !theurl.contains("mailto")){
					list.add(theurl);
				}
			}
		}
		return list;
	}
}
