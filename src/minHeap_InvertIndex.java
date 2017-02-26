
public class minHeap_InvertIndex {
	private Tuples[] Heap;
	private int size;
	private int maxsize;
	private static final int FRONT = 1;
	
	private int parent(int pos) {return pos / 2;}
	private int leftChild(int pos) {return (2 * pos);}
	private int rightChild(int pos) {return (2 * pos) + 1;}

	public minHeap_InvertIndex(int maxsize){
	   this.maxsize = maxsize;
	   this.size = 0;
	   Heap = new Tuples[this.maxsize + 1];
	   Heap[0] = null;
	}

	private boolean isLeaf(int pos) {
		if (pos > (size / 2) && pos <= size) {
			return true;
		}
		return false;
	}

	private void swap(int fpos, int spos) {
		Tuples tmp;
		tmp = Heap[fpos];
		Heap[fpos] = Heap[spos];
		Heap[spos] = tmp;
	}

	private void minHeapify(int pos) {
		if (!isLeaf(pos)) {
			if ( compareTuple(Heap[pos], Heap[leftChild(pos)]) > 0 || compareTuple(Heap[pos], Heap[rightChild(pos)]) > 0 ) {
				if(compareTuple(Heap[rightChild(pos)], Heap[leftChild(pos)]) > 0){
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
	
	private int compareTuple(Tuples myTuples1, Tuples myTuples2){
		int returnFlag = 0;
		if( ( returnFlag = myTuples1.getWord().compareTo(myTuples2.getWord()) ) != 0 )
			return returnFlag;
		if( ( returnFlag = (myTuples1.getDocID() - myTuples2.getDocID()) ) != 0 )
			return (returnFlag > 0) ? 1 : -1;		
		if( ( returnFlag = (myTuples1.getPos() - myTuples2.getPos()) ) != 0 )
			return (returnFlag > 0) ? 1 : -1;
		else return 0;
	}
	
	public int getCurrentSize() {return size;}
	public int getCurrentMaxsize() {return maxsize;}

	public void insert(Tuples element) {
		Heap[++size] = element;
		int current = size;

		while (current > 1 && compareTuple(Heap[parent(current)], Heap[current]) > 0) {
			swap(current, parent(current));
			current = parent(current);
		}
	}
	
	public Tuples remove() {
		Tuples popped = Heap[FRONT];
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
//	public void print() {
//		for (int i = 1; i <= size / 2; i++) {
//			if((2*i+1) <= size){
//				System.out.print(
//						" PARENT : " + Heap[i].toString() + " LEFT CHILD : " + Heap[2 * i].toString() + " RIGHT CHILD :" + Heap[2 * i + 1].toString());
//				System.out.println();
//			}
//			else{
//				System.out.print(
//						" PARENT : " + Heap[i].toString() + " LEFT CHILD : " + Heap[2 * i].toString());
//				System.out.println();
//			}
//		}
//	}
}
