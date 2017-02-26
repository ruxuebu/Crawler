
class myUrlNode{	
	private String url;
	private int depth;
	private boolean visitFlag;
	
	public myUrlNode(String sourceUrl, int d){
		url = sourceUrl;
		depth = d;
		visitFlag = true;
	}	
	public String getUrl() {return url;}
	public int getDepth() {return depth;}
	public boolean getVisitFlag() {return visitFlag;}
	public void setVisitFlag(boolean flag) {visitFlag = flag;}
}
