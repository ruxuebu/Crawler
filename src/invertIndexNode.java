import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;


public class invertIndexNode {
	private String previousWord;
	private String writeStr;
	private int perviousdocId;
	private int previousPos;
	private int frequency;
	private ArrayList<Integer> posOffsetArray;
	private String invertIndexFilePath;
	private File invertIndexFilename;
	private String collectionInfoFilePath;
	private File collectionInfoFilename;
	private RandomAccessFile mm;
	private BufferedWriter invertIndexOutput;
	private BufferedWriter collectionInfoOutput;
	private Iterator iter;
	private static long collectionFilePos;
	private int collectionFileLength;
//	private static int wordCount = 0;
		
	public invertIndexNode(){
		previousWord = "";
		writeStr = "";
		perviousdocId = -1;
		previousPos = -1;
		frequency = 0;
		posOffsetArray = new ArrayList<Integer>();
		invertIndexFilePath = "/Users/Jackie/Downloads/Search_Engine/InvertIndexFile/InvertIndex";	
		collectionInfoFilePath = "/Users/Jackie/Downloads/Search_Engine/InvertIndexFile/CollectionInfo";
		invertIndexFilename = new File(invertIndexFilePath);
		collectionInfoFilename = new File(collectionInfoFilePath);
		collectionFilePos = 0;
		collectionFileLength = 0;		
		
		if (invertIndexFilename.exists()) {
			invertIndexFilename.delete();
			try {
				invertIndexFilename.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (collectionInfoFilename.exists()) {
			collectionInfoFilename.delete();
			try {
				collectionInfoFilename.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			invertIndexOutput = new BufferedWriter(new FileWriter(invertIndexFilePath));
			collectionInfoOutput = new BufferedWriter(new FileWriter(collectionInfoFilePath));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void InvertIndexProcessingFunction(Tuples sourceTuples){
		if(!previousWord.equals("")){
			if(!previousWord.equals(sourceTuples.getWord())){
				try {
//					System.out.println("write = " + previousWord);
					writeStr = perviousdocId + " " + Integer.toString(frequency) + " ";
					invertIndexOutput.write(writeStr);
					collectionFileLength += writeStr.length();
					iter = posOffsetArray.iterator();
					while (iter.hasNext()) {	
						writeStr = iter.next().toString() + " ";
						invertIndexOutput.write(writeStr);
						collectionFileLength += writeStr.length();
					}
					writeToCollectionFile(previousWord);	
					collectionFileLength = 0;
					previousWord = sourceTuples.getWord();
					perviousdocId = sourceTuples.getDocID();
					previousPos = sourceTuples.getPos();
					posOffsetArray.clear();
					posOffsetArray.add(previousPos);
					frequency = 1;
					writeStr = previousWord + " ";
					invertIndexOutput.write(writeStr);
					collectionFileLength = writeStr.length();
//					wordCount++;
				} catch (IOException e1) {
					e1.printStackTrace();
				} 	 				
			}
			
			else if(perviousdocId != sourceTuples.getDocID()){
				try {
					writeStr = perviousdocId + " " + Integer.toString(frequency) + " ";
					invertIndexOutput.write(writeStr);
					collectionFileLength += writeStr.length();
					iter = posOffsetArray.iterator();
					while (iter.hasNext()) {	
						writeStr = iter.next().toString() + " ";
						invertIndexOutput.write(writeStr);
						collectionFileLength += writeStr.length();
					}
					perviousdocId = sourceTuples.getDocID();
					previousPos = sourceTuples.getPos();
					posOffsetArray.clear();
					posOffsetArray.add(previousPos);
					frequency = 1;	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}			
			else{				
				posOffsetArray.add(sourceTuples.getPos() - previousPos);   //offset
				previousPos = sourceTuples.getPos();
				frequency++;
			}
		}
		else{
			previousWord = sourceTuples.getWord();
			perviousdocId = sourceTuples.getDocID();
			previousPos = sourceTuples.getPos();
			posOffsetArray.add(previousPos);
			frequency++;
			try {
				writeStr = previousWord + " ";
				invertIndexOutput.write(writeStr);
				collectionFileLength = writeStr.length();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void checkAndCleanFunction(){			
		try {	
			writeStr = perviousdocId + " " + Integer.toString(frequency) + " ";
			invertIndexOutput.write(writeStr);
			collectionFileLength += writeStr.length();
			Iterator iter = posOffsetArray.iterator();
			while (iter.hasNext()) {	
				writeStr = iter.next().toString() + " ";
				invertIndexOutput.write(writeStr);
				collectionFileLength += writeStr.length();
			}	
			writeToCollectionFile(previousWord);
//			wordCount++;
//			collectionInfoOutput.write("word num is " + wordCount);
			invertIndexOutput.flush();
			collectionInfoOutput.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		} 	 	
	}
	
	public void writeToCollectionFile(String word){
		try {
			collectionInfoOutput.write(word + " " + collectionFilePos + " " + collectionFileLength + "\n");
			collectionFilePos += collectionFileLength;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
