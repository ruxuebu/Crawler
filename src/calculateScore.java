import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.swing.plaf.synth.SynthSeparatorUI;


public class calculateScore {
	private BufferedInputStream invertIndexFileStream;         //InvertIndex输入流
	private File invertIndexFile;                              //InvertIndex文件
	private String invertIndexFileName;                        //InvertIndex文件名
	private InputStreamReader collectionInfoFileStream;        //collectionInfo输入流
	private BufferedReader collectionInfoReader;               //collectionInfo读入流
	private File collectionFile;                               //collectionInfo文件
	private String collectionInfoFileName;                     //collectionInfo文件名
	private String collectionInfoReadLine;
	private InputStreamReader tuplesNumFileStream;             //每个docID里有多少个tuples
	private BufferedReader tuplesNumFileReader;                
	private File tuplesNumFile;
	private String tuplesNumFilePath;
	private byte[] readByte;                                   //用于解析InvertIndex
	private String tuplesNumReadLine;
	private int invertIndexOffset;                             //tuple在InvertIndex里的偏移，用于从collectioninfo解析invertindex文件时用
	private int invertIndexReadLength;                         //tuple在InvertIndex里的长度，用于从collectioninfo解析invertindex文件时用
	private String[] keyWordArray;                             //输入多个keyword时存放的数组
	private int[] tuplesNumInEachDoc;
	private int totalDocNum;                                   //总的documentID的个数， 52140
	private LinkedList<Integer> getDocIdArray;                 //用于存放含有对应keyword的documentID
	private LinkedList<Integer> getWordFrequencyArray;         //用于存放对应keyword在其出现的每个document里出现的频率
	private double[] K_value;
	private double[] BM25_value;
	private minHeap_selectUrl mySelectUrlHeap;                 //用于排序的堆
	private String[] totalUrlArray;                            //用于存放所有document里的Url 
//	private String[] showUrlArray;                             
	private LinkedList<Integer> commonDoc;                     //多个keyword共同出现的所有documentID
	private LinkedList<LinkedList<Integer>> commonFrequencyArray;   //存放用于记录每个keyword在所有keyword共同出现的文件里出现的频率的linkedlist,
                                                                    //一个keyword对应一个LinkedList<Integer>
	private static int commonFrequencyArrayCount = 0;          //用于记录当前正在处理的commonFrequencyArray中的某一个LinkedList
	private boolean lastKeyWordFlag = false;  
	private int[] docNumContainKeyWord;                        //存放包含keyword的文件数的数组
	private static int keyWordCount = 0;                       //用于记录当前处理的keyword的count
	private static final int topNum = 10;                      //搜索前topNum的Url
	
		
	public calculateScore(int sourceDocNum, String[] sourceKeyWordArray){
		collectionInfoFileName = "/Users/Jackie/Downloads/Search_Engine/InvertIndexFile/CollectionInfo";
		collectionFile = new File(collectionInfoFileName);
		invertIndexFileName = "/Users/Jackie/Downloads/Search_Engine/InvertIndexFile/InvertIndex";
		invertIndexFile = new File(invertIndexFileName);
		tuplesNumFilePath = "/Users/Jackie/Downloads/Search_Engine/InvertIndexFile/TuplesNum";
		tuplesNumFile = new File(tuplesNumFilePath);		
		collectionInfoReadLine = "";
		tuplesNumReadLine = "";
		totalDocNum = sourceDocNum;
		tuplesNumInEachDoc = new int[sourceDocNum];
		totalUrlArray = new String[sourceDocNum];
//		showUrlArray = new String[topNum];
		keyWordArray = sourceKeyWordArray;
		getDocIdArray = new LinkedList<Integer>();
		getWordFrequencyArray = new LinkedList<Integer>();
		mySelectUrlHeap = new minHeap_selectUrl(topNum);
		commonDoc = new LinkedList<Integer>();
		commonFrequencyArray = new LinkedList<LinkedList<Integer>>();
		docNumContainKeyWord = new int[sourceKeyWordArray.length];
		K_value = new double[sourceDocNum];
	
		try {
			tuplesNumFileStream = new InputStreamReader(new FileInputStream(tuplesNumFile));
			tuplesNumFileReader = new BufferedReader(tuplesNumFileStream);
			int tempCount = 0;
			
			while((tuplesNumReadLine = tuplesNumFileReader.readLine()) != null){
				tuplesNumInEachDoc[tempCount] = Integer.parseInt(tuplesNumReadLine.split("\\s{1,}")[1]);
				tempCount++;
			}
			getDataFile();
			for(int i = 0; i < keyWordArray.length; i++){
				findWordInCollectionInfoFile(keyWordArray[i]);
				if(i >= 1){
					Iterator iter = getWordFrequencyArray.iterator();
					commonFrequencyArray.add(new LinkedList<Integer>());
					while (iter.hasNext()) {
						commonFrequencyArray.get(i).add((int) iter.next());
					}
					commonFrequencyArrayCount = i;
					if( !findCommonDoc() ){
						System.out.println("Nothing found");
						break;
					}
				}
				else{
					Iterator iter = getDocIdArray.iterator();
					while (iter.hasNext()) 
						commonDoc.add((int) iter.next());
					iter = getWordFrequencyArray.iterator();
					commonFrequencyArray.add(new LinkedList<Integer>());
					while (iter.hasNext()) 
						commonFrequencyArray.get(i).add((int) iter.next());
				}
				if(i == keyWordArray.length - 1) 
					lastKeyWordFlag = true;
//				System.out.println("size = " + commonDoc.size());
			}
			getDocScoreFunction();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
		
	public static void main(String[] args){
		String keyWords = "cat dog";
		String[] keyWordsArray = keyWords.split("\\s{1,}");
		System.out.println("**********");
		calculateScore a = new calculateScore(52140, keyWordsArray);
	}

	
	
/**********************************************************************
*	                   Functions for calling:                         *	
*	                                                                  *
***********************************************************************/	
	
	
//findWordInCollectionInfoFile: **************************************
//解析CollectionInfo文件
	public void findWordInCollectionInfoFile(String keyWord){
		String[] splitCollectionInfoArray;
		String[] splitInvertIndexArray;
		boolean findFlag = false;
		try {
			collectionInfoFileStream = new InputStreamReader(new FileInputStream(collectionFile));
			collectionInfoReader = new BufferedReader(collectionInfoFileStream);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			while((collectionInfoReadLine = collectionInfoReader.readLine()) != null){
				splitCollectionInfoArray = collectionInfoReadLine.split("\\s{1,}");
				if(keyWord.equals(splitCollectionInfoArray[0])){
					invertIndexOffset = Integer.parseInt(splitCollectionInfoArray[1]);
					invertIndexReadLength = Integer.parseInt(splitCollectionInfoArray[2]);
//					System.out.println("get: " + splitCollectionInfoArray[0] + ", " + invertIndexOffset + ", " + invertIndexReadLength);
					readByte = new byte[invertIndexReadLength];
					invertIndexFileStream = new BufferedInputStream(new FileInputStream(invertIndexFile));
					invertIndexFileStream.skip(invertIndexOffset);
					invertIndexFileStream.read(readByte, 0, invertIndexReadLength);
					invertIndexFileStream.close();
//					System.out.println(new String(readByte));
					splitInvertIndexArray = new String(readByte).split("\\s{1,}");
					parseInvertIndexFunction(splitInvertIndexArray);		
					findFlag = true;
					break;
				}
			}
			collectionInfoReader.close();
			collectionInfoFileStream.close();
			if(findFlag == false)
				System.out.println("Nothing found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//parseInvertIndexFunction: **************************************************
//用于将获取到的splitInvertIndexArray对InvertIndex文件进行解析
	public void parseInvertIndexFunction(String[] splitInvertIndexArray){
		int tempFrequency = 0;
		getDocIdArray.clear();
		getWordFrequencyArray.clear();	
		for(int i = 1; i < splitInvertIndexArray.length; ){
			getDocIdArray.add(Integer.parseInt(splitInvertIndexArray[i]));
			i++;
			tempFrequency = Integer.parseInt(splitInvertIndexArray[i++]);
			getWordFrequencyArray.add(tempFrequency);
			i += tempFrequency;
		}
		docNumContainKeyWord[keyWordCount++] = getDocIdArray.size();
	}

//getDataFile: ********************************************************************
//读取Data文件
	public void getDataFile() {
		String dataFilePath = "/Users/Jackie/Downloads/Search_Engine/nz2_merged";
		String regExIndex = "\\d{1,2}_index";
		String readLine = "";
		String docNum = "";
		String[] tempRecord;
		int readSize = 0;
		String saveFilePath = "/Users/Jackie/Downloads/Search_Engine/pages";	
		File pageFileName;
		int urlArrayCount = 0;
		
		try {		
			File file = new File(dataFilePath);
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
                		BufferedReader bufferforindex = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dataFilePath + "/" + filelist[i]))));
                		BufferedInputStream bufferfordata = new BufferedInputStream(new GZIPInputStream(new FileInputStream(dataFilePath + "/" + docNum + "_data")));                        		
                		
                		while( (readLine = bufferforindex.readLine()) != null ){
                			tempRecord = readLine.split("\\s{1,}");
                			totalUrlArray[urlArrayCount] = tempRecord[0];
                			urlArrayCount++;
                		}
                	}
                }
			}else {
				System.out.println("Cannot find the file");
			}
		}catch (Exception e) {
			System.out.println("Reading file error");
			e.printStackTrace();
		}			
	}

//findCommonDoc: ****************************************************
//寻找keyword都出现的文件
	public boolean findCommonDoc(){
		LinkedList<Integer> compareArrayDoc = getDocIdArray;
		int pos1 = 0, pos2 = 0; 
		while(pos1 < commonDoc.size() && pos2 < compareArrayDoc.size()){
			if(commonDoc.get(pos1) < compareArrayDoc.get(pos2)) {
				commonDoc.remove(pos1); 
				for(int x = 0; x < commonFrequencyArrayCount; x++)
					commonFrequencyArray.get(x).remove(pos1);
			}
			else if(commonDoc.get(pos1) > compareArrayDoc.get(pos2)) {
				compareArrayDoc.remove(pos2);
				commonFrequencyArray.get(commonFrequencyArrayCount).remove(pos2);
			}
			else {
				pos1++; 
				pos2++;
			}
		}
		while(pos2 == compareArrayDoc.size() && pos1 < commonDoc.size()){
			commonDoc.remove(pos1);
			for(int x = 0; x < commonFrequencyArrayCount; x++)
				commonFrequencyArray.get(x).remove(pos1);
		}
		if(commonDoc.size() > 0) return true;
		else return false;
	}
	
	public void getDocScoreFunction(){
		double d_Ave = 0;	
		BM25_value = new double[commonDoc.size()];
		for(int k = 0; k < totalDocNum; k++){
			d_Ave += tuplesNumInEachDoc[k];
		}
		d_Ave /= totalDocNum;
		for(int k = 0; k < totalDocNum; k++){
			K_value[k] = 1.2 * (0.25 + (0.75 * tuplesNumInEachDoc[k] / d_Ave));
		}
		int i = 0;
		double tempF = 0;
		int tempD = 0;
		Iterator iter;
		iter = commonDoc.iterator();
		while (iter.hasNext()) { 
			tempD = (int) iter.next();
			K_value[i] = 1.2 * (0.25 + (0.75 * tuplesNumInEachDoc[tempD] / d_Ave));
			BM25_value[i] = 0;
			for(int k = 0; k < docNumContainKeyWord.length; k++){
				tempF = commonFrequencyArray.get(k).get(i);
				BM25_value[i] += log( ((totalDocNum-docNumContainKeyWord[k]+0.5) / (docNumContainKeyWord[k]+0.5)), 2.0 ) * (2.2*tempF) / (K_value[i]+tempF);
//				BM25_value[i] += Math.log( ((totalDocNum-docNumContainKeyWord[k]+0.5) / (docNumContainKeyWord[k]+0.5)) ) * (2.2*tempF) / (K_value[i]+tempF);
			}
			mySelectUrlHeap.insert(new myDocId(tempD, BM25_value[i++]));
		}	
//		int showUrlArrayCount = topNum;
		while(mySelectUrlHeap.getCurrentSize() > 0){
			myDocId temp = mySelectUrlHeap.remove();
//			showUrlArray[--showUrlArrayCount] = totalUrlArray[mySelectUrlHeap.remove().getDocId()];			
			System.out.println("[" + temp.getDocId() + "] : " + totalUrlArray[temp.getDocId()] + ", score = " + temp.getDocScore());
		}		
//		for(int k = 0; k < showUrlArray.length; k++)
//			System.out.println("["+ (k+1) + "] " + showUrlArray[k]);
	}
	
	public double log(double value, double base) {
		return Math.log(value) / Math.log(base);
	}
}



/**********************************************************************
*	               Definition of class myDocId:                       *	
*	                                                                  *
***********************************************************************/

//class myDocId: *****************************************************
class myDocId{
	private int docId;
	private double docScore;
	public myDocId(int d, double s){
		docId = d;
		docScore = s;
	}
	public int getDocId() {return docId;}
	public double getDocScore() {return docScore;}
}


//class minHeap_selectUrl: ******************************************
class minHeap_selectUrl{
	private myDocId[] Heap;
	private int size;
	private int maxsize;
	private static final int FRONT = 1;
	
	private int parent(int pos) {return pos / 2;}
	private int leftChild(int pos) {return (2 * pos);}
	private int rightChild(int pos) {return (2 * pos) + 1;}

	public minHeap_selectUrl(int maxsize){
	   this.maxsize = maxsize;
	   this.size = 0;
	   Heap = new myDocId[this.maxsize + 1];
	   Heap[0] = null;
	}

	private boolean isLeaf(int pos) {
		if (pos > (size / 2) && pos <= size) {
			return true;
		}
		return false;
	}

	private void swap(int fpos, int spos) {
		myDocId tmp;
		tmp = Heap[fpos];
		Heap[fpos] = Heap[spos];
		Heap[spos] = tmp;
	}

	private void minHeapify(int pos) {
		if (!isLeaf(pos)) {
			if ( compareDocScore(Heap[pos], Heap[leftChild(pos)]) > 0 || compareDocScore(Heap[pos], Heap[rightChild(pos)]) > 0 ) {
				if(compareDocScore(Heap[rightChild(pos)], Heap[leftChild(pos)]) > 0){
					swap(pos, leftChild(pos));
					minHeapify(leftChild(pos));
				}
				else{
					swap(pos, rightChild(pos));
					minHeapify(rightChild(pos));
				}
			}
		}
	}
	
	private int compareDocScore(myDocId myTuples1, myDocId myTuples2){
		if(myTuples1.getDocScore() > myTuples2.getDocScore()) return 1;
		if(myTuples1.getDocScore() < myTuples2.getDocScore()) return -1;
		else return 0;
	}
	
	public int getCurrentSize() {return size;}
	public int getCurrentMaxsize() {return maxsize;}

	public void insert(myDocId element) {
		if(size == maxsize){
			if(element.getDocScore() > Heap[FRONT].getDocScore())
				remove();
			else return;
		}
		Heap[++size] = element;
		int current = size;

		while (current > 1 && compareDocScore(Heap[parent(current)], Heap[current]) > 0) {
			swap(current, parent(current));
			current = parent(current);
		}
	}
	
	public myDocId remove() {
		myDocId popped = Heap[FRONT];
		Heap[FRONT] = Heap[size--];
		if(size > 1)
			minHeapify(FRONT);
		return popped;
	}
	
	public void minHeap() {
		for (int pos = (size / 2); pos >= 1; pos--) {
			minHeapify(pos);
		}
	}
}
