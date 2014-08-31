package crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DomainThread implements Runnable {
	String domain;
	ExecutorService pool;
	Queue queue = new PriorityQueue();
	CompletionService<List<String>> thepool;
	List<String> result;
	private static int numThreads = 2;
	
	public DomainThread(String url, String domain){
		this.domain = domain;
		queue.add(url);
		pool = Executors.newFixedThreadPool(numThreads);
		thepool = new ExecutorCompletionService<List<String>> (pool);
	}
	
	@Override
	public void run(){
		URL theUrl = null;
		while(!queue.isEmpty() && Crawler.checkdocID() < Crawler.getThreshold()){
			for(int i = 0; i < numThreads; i++){
				if(!queue.isEmpty()){
					thepool.submit(new URLThread((String)queue.poll()));
				}
			}
			int currentQueueSize = queue.size() + 1;
			for(int i = 0; i < Math.min(numThreads, currentQueueSize); i++){
				try {
					result = thepool.take().get();
					for(int j = 0; j < result.size(); j++){
						if(!result.get(j).isEmpty() && !result.get(j).contains(" ")){
							try {
								theUrl = new URL(result.get(j).replace("\"", " ").trim());
								
							} catch(MalformedURLException ex){
								Logger.getLogger(DomainThread.class.getName()).log(Level.SEVERE, null, ex);
							}
							String domain = theUrl.getHost();
							if(this.domain.contentEquals(domain)){
								if(!Crawler.checkpagehash(result.get(j))){
									queue.add(result.get(j));
									Crawler.addpagehash(result.get(j));
								} else {
									/* Diagnostic print to see the actual URL for the duplicate
									 * System.out.println("duplicate " + result.get(j));
									 */
									Crawler.incrdups();
								}
							} else if(!Crawler.checkhash(domain)){
								//Add a domain to crawler list and add a thread for this domain
								Crawler.submitthread(result.get(j), domain);
							} else {
							}
						}
					}
				} catch(InterruptedException ex){
					Logger.getLogger(DomainThread.class.getName()).log(Level.SEVERE, null, ex);
				} catch(ExecutionException ex){
					Logger.getLogger(DomainThread.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			try {
				Thread.sleep(500);	//to politely crawl
			} catch(InterruptedException ex){
				Logger.getLogger(DomainThread.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		pool.shutdown();
	}
}
