import java.util.Comparator;

public class Tuples implements Comparator{
	private String word;
	private int docID;
	private int pos;
	
	public Tuples(){
		word = "";
		docID = 0;
		pos = 0;
	}
	
	public Tuples(String w, int d, int p){
		word = w;
		docID = d;
		pos = p;
	}
	
	public String getWord() {return word;}
	public int getDocID() {return docID;}
	public int getPos() {return pos;}

	@Override
	public int compare(Object o1, Object o2) {
		Tuples myTuples1 = (Tuples) o1;
		Tuples myTuples2 = (Tuples) o2;
		int returnFlag = 0;
		
		if( ( returnFlag = myTuples1.word.compareTo(myTuples2.word) ) != 0 )
			return returnFlag;

		if( ( returnFlag = (myTuples1.docID - myTuples2.docID) ) != 0 )
			return (returnFlag > 0) ? 1 : -1;
		
		if( ( returnFlag = (myTuples1.pos - myTuples2.pos) ) != 0 )
			return (returnFlag > 0) ? 1 : -1;
		else return 0;
	}

	@Override
	public String toString() {
		return new String(word + " " + Integer.toString(docID) + " " + Integer.toString(pos) + "\n");
	}
}
