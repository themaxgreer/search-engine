package crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class Crawler {
	//
	private static ConcurrentHashMap<String, Integer> domains;
	private static ConcurrentHashMap<String, Integer> pages;
	
	//Static variables for threads
	private static int CAPACITY = 200000; //number of pages crawled
	private static int THREAD_NO = 256;
	private static int LOADFACTOR = 1000;
	private static int CONCURR = 1000;
	private static int THRESHOLD;		//in case a user provided threshold is provided
	private static ExecutorService pool;
	
	private static AtomicInteger docID; 	//each retrieved page gets its own docID
	private static AtomicLong duplicates;
	
	private static String[] pid_map = new String[200]; 	//page ID map
	private static final File dir = new File("html/");
	
	/* List provided by Professor */
	private static String[] DONTCRAWL = {"facebook.com", "twitter.com", "slashdot.org",
		"amazon.com", "google.com", "ebay.com" ,"www.facebook.com", "www.twitter.com", "www.slashdot.org",
		"www.amazon.com", "www.google.com", "www.ebay.com", "www.stussy.com", "www.torso.de"};

	public static void main(String[] args) throws URISyntaxException, IOException {
		if(args.length != 2){
			System.out.println("Usage: crawler.jar seedURL numOfSitesToCrawl");
			System.exit(1);
		}
		/* Set up variables */
		int toggle = 1;
		duplicates = new AtomicLong();
		pid_map = new String[250000];
		docID = new AtomicInteger();
		URI URL = new URI(args[0]);
		THRESHOLD = Integer.parseInt(args[1]);
		
		pool = Executors.newFixedThreadPool(THREAD_NO);
		pages = new ConcurrentHashMap<String, Integer>(CAPACITY, LOADFACTOR, CONCURR);
		
		domains = new ConcurrentHashMap<String, Integer>(CAPACITY, LOADFACTOR, CONCURR);
		domains.put(URL.getHost(), Integer.SIZE);
		
		/* Diagnosis for seed URL */
		System.out.println("added " + URL.toString());
		System.out.println(new Date());
	
		pool.submit(new DomainThread(URL.toString(), URL.getHost()));
		for(int i = 0; i < DONTCRAWL.length; i++){
			/* Add do no crawl to domain list so if found it 
			 * will be a "duplicate" and ignored
			 */
			domains.put(DONTCRAWL[i], i);
		}
		while(true){
            if(docID.get() % 1000 == 0 && toggle == 1){
                System.out.println(new Date() + " Threads: " + Thread.activeCount());
                toggle = 0;
            }else if(docID.get() % 1000 != 0){
                toggle = 1;
            }
            if(docID.get() > THRESHOLD){
            	pool.shutdown();
            	System.out.println("done and exiting");
            	FileWriter file;
            	file = new FileWriter("pid_map.dat");
            	for(int i = 1; i < docID.get(); i++){
            		file.write(i + "\t" +  pid_map[i] + "\n");
            	}
            	file.close();
            	System.exit(1);
            }
		}
	}
	public static boolean checkhash(String domain){
		return domains.containsKey(domain);
	}
	
	public static boolean checkpagehash(String url){
		return pages.containsKey(url);
	}
	
	public static void addpagehash(String url){
		pages.put(url, THRESHOLD);
	}
	
	public static void submitthread(String url, String domain){
        domains.put(domain, THRESHOLD);
		pool.submit(new DomainThread(url, domain));
	}

	public static int getdocID(){
		return docID.incrementAndGet();
	}
	
	public static int checkdocID(){
		return docID.get();
	}
	
	public static int getThreshold(){
		return THRESHOLD;
	}
	
	public static void insertpid(String url, int docID){
		pid_map[docID] = new String(url);
	}
	
	public static void printmetrics(int docID){
		System.out.println(new Date() + " DocIds: " + docID + "\tNumber of duplicates: " + duplicates.get());
	}
	
	public static void incrdups(){
		duplicates.incrementAndGet();
	}
	public static File getDir(){
		return dir;
	}
}
