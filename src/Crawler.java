import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


public class Crawler {
	private String[] source_urlList;
	private LinkedList<String> urlList;	
	private LinkedList<myUrlNode> urlQuery;
	private HashMap<String, Integer> myMap;
	private GoogleSearch myGoogleSearch;
	private String searchKey = "alibaba";
	private int visitPos;
	public static final int maxThreadNum = 1000;
	public static final int searchLevel = 4;

	private boolean chooseFromGoogleFlag = false;  //true: using google API,  false: using results from file 
	
	public Crawler(){
		source_urlList = new String[4];
		urlList = new LinkedList<String>();
		urlQuery = new LinkedList<myUrlNode>();
		myMap = new HashMap<String, Integer>();
		visitPos = 0;
		
		configulationFunction();		
		threadManageFunction();
		writeToTxtFile();
	}
	
	public static void main(String[] args){
		Crawler myCraweler = new Crawler();		
	}

	
/**********************************************************************
*	                   Function for calling:                          *	
*	                                                                  *
***********************************************************************/
	

//splitUrl Function: *****************************************************
	public void splitUrl(String[] source_urlList){
		for(int i = 0; i < source_urlList.length; i++){
			if(source_urlList[i] != null){
				String[] result = source_urlList[i].split("/{1,2}");
				urlList.add(result[0] + "//" + result[1] + "/");
			}
		}
		deleteRepeatedUrl(urlList);
	}
	
	public void deleteRepeatedUrl(LinkedList uList){
		for(int i = 0; i < uList.size()-1; i++){
			for(int j = i+1; j < uList.size(); j++){
				if(uList.get(j).equals(uList.get(i)))
					uList.remove(j--);
			}	
		}
	}	

//configulationFunction: *****************************************************
	public void configulationFunction(){
		if(chooseFromGoogleFlag)
			myGoogleSearch = new GoogleSearch(searchKey, source_urlList);	
		else
			readTxtFile("/Users/Jackie/Downloads/googleSearchUrl.txt");
		
		for(int i = 0; i < source_urlList.length; i++)
			System.out.println("get [level-1] link: " + source_urlList[i]);
		
		splitUrl(source_urlList);
		
		for(int i = 0; i < urlList.size(); i++)
			myMap.put(urlList.get(i).toString(), 1);
	}

//threadManageFunction: *****************************************************	
	public void threadManageFunction(){
		String urlLink = "";
		int urlDepth = 0;
		
		long a = System.currentTimeMillis();
		System.out.println("Start time is: " + a + " ms");
		
		for(int i = 0; i < urlList.size(); i++)
			new Thread(new Runner(urlList.get(i).toString(), urlQuery, myMap, 1, searchLevel)).start();

		while(Thread.activeCount() > 1){
			if (Thread.activeCount() < maxThreadNum) {
				if (visitPos < urlQuery.size()) {
					urlLink = urlQuery.get(visitPos).getUrl();
					urlDepth = urlQuery.get(visitPos++).getDepth();
					
					if (urlDepth <= searchLevel-1)
						new Thread(new Runner(urlLink, urlQuery, myMap, urlDepth, searchLevel)).start();
				}
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Active thread = " + Thread.activeCount());
		}
		
		long b = System.currentTimeMillis();
		System.out.println("End time is: " + b + " ms");
		System.out.println("Last " + (b-a) + " ms");
	}

//writeToTxtFile Function: *****************************************************
	public void writeToTxtFile(){
		String filePath = "/Users/Jackie/Downloads/level-4-searchResult.txt";		
		File filename = new File(filePath);
		if (filename.exists()) {
			filename.delete();
			try {
				filename.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		RandomAccessFile mm = null;
		try {
			mm = new RandomAccessFile(filename, "rw");
			Iterator iter = myMap.entrySet().iterator();

			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				mm.writeBytes("[depth = " + val.toString() + "]  [" + key.toString() + "]\n");
			}	
			mm.writeBytes("url nums is " + myMap.size());
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (mm != null) {
				try {
					mm.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}
	}

//readTxtFile Function: *****************************************************	
	public void readTxtFile(String filePath) {
		try {
			String encoding = "UTF-8";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { 
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				int urlList_size = 0;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					source_urlList[urlList_size++] = lineTxt;
				}
				read.close();
			} else {
				System.out.println("Cannot find the file");
			}
		} catch (Exception e) {
			System.out.println("Reading file error");
			e.printStackTrace();
		}
	}
	
}