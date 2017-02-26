import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


public class parseIndex {
	private String dataFilePath;
	private String regExIndex = "\\d{1,2}_index";
	private ArrayList<Tuples> array;
	private parse myParse;
	private minHeap_InvertIndex myMinHeap;
	private static int docID = 0;
	private static int splitCount = 0;
	private String tuplesNumFilePath;
	private BufferedWriter tuplesNumFileOutput;
	public static int totalDocNum = 0;
	public static final int maxNumInOneSplit = 10;
	
	public parseIndex(){
		dataFilePath = "/Users/Jackie/Downloads/Search_Engine/nz2_merged";
		tuplesNumFilePath = "/Users/Jackie/Downloads/Search_Engine/InvertIndexFile/TuplesNum";
		try {
			tuplesNumFileOutput = new BufferedWriter(new FileWriter(tuplesNumFilePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		array = new ArrayList<Tuples>();
		myParse = new parse();
		
		long a = System.currentTimeMillis();
		System.out.println("Start time " + a + " ms");
		readIndexFile(dataFilePath);
		long b = System.currentTimeMillis();
		parseToInvertIndex( (splitCount % 10) > 0 ? (splitCount / 10+1) : splitCount / 10 );
//		parseToInvertIndex( 9 );		
		long c = System.currentTimeMillis();
		System.out.println("End time " + c + " ms");
		System.out.println("Read Index File Last " + (b-a) + " ms");
		System.out.println("Parse To InvertIndex File Last " + (c-b) + " ms");
		System.out.println("Total time is " + (c-a) + " ms");
	}
	
	public static void main(String[] args){
		parseIndex myParseIndex = new parseIndex();
	}
		

/**********************************************************************
*	                   Functions for calling:                          *	
*	                                                                  *
***********************************************************************/
		

//readIndexFile: *****************************************************		
	public void readIndexFile(String filePath) {
		String readLine = "";
		String docNum = "";
		String[] tempRecord;
		int readSize = 0;
		String saveFilePath = "/Users/Jackie/Downloads/Search_Engine/pages";	
		File pageFileName;
		
		try {		
			File file = new File(filePath);
			if (file.isDirectory() && file.exists()) { 
				String[] filelist = file.list();
                for (int i = 0; i < filelist.length; i++) {                	                	 
                	Pattern p = Pattern.compile(regExIndex);
                	Matcher m = p.matcher(filelist[i]);
                	
                	if(m.find()){               		
                		docNum = "";
                		for(int k = 0; k < filelist[i].length(); k++){
                			if(filelist[i].charAt(k) != '_')
                				docNum += filelist[i].charAt(k);
                			else
                				break;
                		}
                		BufferedReader bufferforindex = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath + "/" + filelist[i]))));
                		BufferedInputStream bufferfordata = new BufferedInputStream(new GZIPInputStream(new FileInputStream(filePath + "/" + docNum + "_data")));                        		
                		 
                		long preSize = 0, curSize = 0;
                		while( (readLine = bufferforindex.readLine()) != null ){
                			tempRecord = readLine.split("\\s{1,}");
                			readSize = Integer.parseInt(tempRecord[3]);
                			byte[] readFromDataByte = new byte[readSize];
                			
                			if( (bufferfordata.read(readFromDataByte, 0, readFromDataByte.length)) != -1){
                				preSize = array.size();
                				myParse.parsePage(tempRecord[0], new String(readFromDataByte), array, docID);
                				docID++;
                				totalDocNum++;
                				curSize = array.size();
                				tuplesNumFileOutput.write((docID-1) + " " + (curSize-preSize) + "\n");
//                				System.out.println("["+(docID-1)+"]increase: " + (curSize-preSize));
                			} 
                			else
                				break;
                		}
                		splitCount++;
//                		System.out.println("splitCount = " + splitCount);

                		if(splitCount % maxNumInOneSplit == 0){
                			System.out.println("enter, splitcount = " + splitCount);
                			Collections.sort((List)array, new Tuples());               			              			
                			writeBackToSplitFile(splitCount);
                			array.clear();  
                		}               		
                	} 
                }
                
                if(splitCount % maxNumInOneSplit != 0){
        			tuplesNumFileOutput.flush();
                	System.out.println("enter, splitcount = " + splitCount);
        			Collections.sort((List)array, new Tuples());  			
        			writeBackToSplitFile((splitCount/10+1) * 10);
        			array.clear();  
                }
			} else {
				System.out.println("Cannot find the file");
			}
		} catch (Exception e) {
			System.out.println("Reading file error");
			e.printStackTrace();
		}
	}
	
//writeBackToSplitFile: *****************************************************	
	public void writeBackToSplitFile(int splitCount){
		int fileCount = splitCount / maxNumInOneSplit;
		String saveSplitFilePath = "/Users/Jackie/Downloads/Search_Engine/splitFile";
		File saveInvertIndexFileName = new File(saveSplitFilePath + "/File" + fileCount);
		
		if (saveInvertIndexFileName.exists()) {
			saveInvertIndexFileName.delete();
			try {
				saveInvertIndexFileName.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedWriter output = null;
		
		try {	
			output = new BufferedWriter(new FileWriter(saveInvertIndexFileName));			
			Iterator iter = array.iterator();
			while (iter.hasNext()) {				
				output.write(iter.next().toString());
			}		
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}	
	}

//parseToInvertIndex: *****************************************************
	public void parseToInvertIndex(int splitNum){
		int splitFileNum = splitNum;
		String splitFilePath = "/Users/Jackie/Downloads/Search_Engine/splitFile";
		String tempLineTxt = "";
		String tempString = "";
		String[] lineTxtArray = new String[splitFileNum];
		String[] tempSplitStr;
		InputStreamReader[] splitFileReaderArray = new InputStreamReader[splitFileNum];
		File[] fileArray = new File[splitFileNum];
		BufferedReader[] bufferedReaderArray = new BufferedReader[splitFileNum];
		HashMap myHeapHashMap = new HashMap();
		invertIndexNode myInvertIndexNode = new invertIndexNode();
		Tuples removeItem;
		int hashMapValue = 0;
		myMinHeap = new minHeap_InvertIndex(splitFileNum);
		
		try {
			for(int i = 0; i < splitFileNum; i++){
				fileArray[i] = new File(splitFilePath + "/File" + Integer.toString(i+1));
				splitFileReaderArray[i] = new InputStreamReader(new FileInputStream(fileArray[i]));
				if (fileArray[i].isFile() && fileArray[i].exists())
					bufferedReaderArray[i] = new BufferedReader(splitFileReaderArray[i]);
				else
					System.out.println("Cannot find the file" + i);
			}
			
			for(int Id = 0; Id < splitFileNum; Id++){
				if((tempLineTxt = bufferedReaderArray[Id].readLine()) != null){
					myHeapHashMap.put(tempLineTxt, Id);
					tempSplitStr = tempLineTxt.split("\\s{1,}");
					myMinHeap.insert(new Tuples(tempSplitStr[0], Integer.parseInt(tempSplitStr[1]), Integer.parseInt(tempSplitStr[2])));
				}
			}	
			
			while(myMinHeap.getCurrentSize() > 0){
				removeItem = myMinHeap.remove();
				myInvertIndexNode.InvertIndexProcessingFunction(removeItem);
				tempString = removeItem.toString();
				tempString = tempString.substring(0, tempString.length()-1);
				hashMapValue = (int) myHeapHashMap.get(tempString);	
				myHeapHashMap.remove(tempString);
			
				if((tempLineTxt = bufferedReaderArray[hashMapValue].readLine()) != null){
					myHeapHashMap.put(tempLineTxt, hashMapValue);
					tempSplitStr = tempLineTxt.split("\\s{1,}");
					myMinHeap.insert(new Tuples(tempSplitStr[0], Integer.parseInt(tempSplitStr[1]), Integer.parseInt(tempSplitStr[2])));
				}				
			}
			myInvertIndexNode.checkAndCleanFunction();
			
		}catch (Exception e) {
			System.out.println("Reading file error");
			e.printStackTrace();
		}		
	}
	
}
