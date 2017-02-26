import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class Runner implements Runnable{
	private String link;
	private LinkedList<myUrlNode> mainlist;
	private HashMap<String, Integer> myMap;
	private int currentDepth;
	private int crawlerSearchLevel;
	
	public Runner(String sourceLink, LinkedList<myUrlNode> llist, HashMap<String, Integer> myMap, int preDepth, int searchLevel){
		link = sourceLink;
		mainlist = llist;
		this.myMap = myMap;
		currentDepth = preDepth+1;
		crawlerSearchLevel = searchLevel;
	}
	
	public void run(){
		try{	
			Document doc = Jsoup.connect(link).timeout(1000).get();
			org.jsoup.select.Elements links = doc.select("a");
			String temp1 = "", temp2 = "";
			String[] res;
			
			for(Element e: links){
				temp1 = e.attr("abs:href").toString();
				res = temp1.split("/{1,2}");
				if (res.length > 1) {
					temp2 = "";
					temp2 = res[0] + "//" + res[1] + "/";
					if (!myMap.containsKey(temp2)) {
						myMap.put(temp2, currentDepth);
						if (currentDepth <= crawlerSearchLevel - 1) {
							mainlist.add(new myUrlNode(temp2, currentDepth));
						}
					}
				}
			}
		}catch(UnknownHostException e1){
			return;
		}catch(HttpStatusException e2){
			return;
		}catch(SocketTimeoutException e3){
			return;
		}catch(SSLHandshakeException e4){
			return;
		}catch(UnsupportedMimeTypeException e5){
			return;
		}catch(SocketException e6){
			return;
		}catch(SSLProtocolException e7){
			return;
		}catch(IOException ex){
			ex.printStackTrace();
		}		
		
		return;
	}
}
