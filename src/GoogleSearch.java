import java.io.File;
import java.io.IOException;  
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;  
import java.net.URL;  
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;  
import com.google.gson.Gson;  
   
public class GoogleSearch {  
	private String searchKey;
	//private LinkedList<String> resultUrlList;
	private String[] resultUrlList;
	
	public GoogleSearch(String sourceKey, String[] sourceUrlList){
		searchKey = sourceKey;
		resultUrlList = sourceUrlList;
		
		try {
			searchFunction();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Searching error in searchFunction()");
			e.printStackTrace();
		}
	}
   
    public void searchFunction() throws IOException {  
      	for (int i = 0; i < 20; i = i + 4) {  
            String address = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&start="+i+"&q=";  
            String query = searchKey;  
            String charset = "UTF-8";  
           
            URL url = new URL(address + URLEncoder.encode(query, charset));  
            Reader reader = new InputStreamReader(url.openStream(), charset);  
            GoogleResults results = new Gson().fromJson(reader, GoogleResults.class); 
            
            for (int m = 0; m <= 3; m++) {  
                //resultUrlList.add(results.getResponseData().getResults().get(m).getUrl() + "\n");
            	resultUrlList[m] = results.getResponseData().getResults().get(m).getUrl();
            } 
        }    
    }  
}  
   
   
class GoogleResults{     
    private ResponseData responseData;  
    public ResponseData getResponseData() { return responseData; }  
    public void setResponseData(ResponseData responseData) { this.responseData = responseData; }  
    public String toString() { return "ResponseData[" + responseData + "]"; }  
   
    static class ResponseData {  
        private List<Result> results;  
        public List<Result> getResults() { return results; }  
        public void setResults(List<Result> results) { this.results = results; }  
        public String toString() { return "Results[" + results + "]"; }  
    }  
   
    static class Result {  
        private String url;  
        private String title;  
        public String getUrl() { return url; }  
        public String getTitle() { return title; }  
        public void setUrl(String url) { this.url = url; }  
        public void setTitle(String title) { this.title = title; }  
        public String toString() { return "Result[url:" + url +",title:" + title + "]"; }  
    }  
} 