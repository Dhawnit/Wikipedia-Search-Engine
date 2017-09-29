import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

class Tuple{
	String word;
	String postingList;
	int docId;
}
class Comp implements Comparator<Tuple> { 
    public int compare(Tuple x, Tuple y){
    	if(x.word.equals(y.word))
    		return x.docId-y.docId;
    	else
    		return x.word.compareTo(y.word);
    }
}

public class Merge {
	
	static String opFilePath = "/media/dhawnit/New Volume/IRE/MASTER_INDEX/";
	static String opFilePathSecondaryIndex = "/media/dhawnit/New Volume/IRE/SecondaryIndex.txt";
	static int wordsProcessed = 0;
	static int outputFileIndex = 1;
	
	public static void merge(){
		try{
	  
			/*ArrayList<String> fileNames = Parser.fileNames;
			int fileCount = fileNames.size();
			String path = Parser.path;*/
			
			//Directly Going to Indexes path for starting merge sort
			String path = "/media/dhawnit/New Volume/IRE/INDEXES/";
			ArrayList<String> fileNames = new ArrayList<String>();
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					fileNames.add(listOfFiles[i].getName());
				}
			}
			int fileCount = fileNames.size(); 
	
			
			ArrayList<BufferedReader> readers = new ArrayList<BufferedReader>();
			for (int i=0; i<fileCount; i++) {
				readers.add(new BufferedReader(new FileReader(path+fileNames.get(i))));
			}
			
			System.out.println("Merge Sort Started.....");
			
			// Merging Process
			Comparator<Tuple> comp = new Comp();
			PriorityQueue<Tuple> data = new PriorityQueue<Tuple>(comp);
			
			BufferedReader br = null;
			Tuple t = null;
			String word = "", newLine="", str="";
			int pos=0;
			String arr[] = null;
			StringBuilder postingList = new StringBuilder(); 
			
			for(int i=0; i<readers.size(); i++){
				br=readers.get(i);
				t=new Tuple();
				str = br.readLine();
				arr = str.split("=");
				t.word=arr[0];
				t.postingList=arr[1];
				t.docId=i;
				data.add(t);
			}
			
			FileWriter fw = new FileWriter(opFilePath+"op"+outputFileIndex+".txt");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			
			FileWriter fw1 = new FileWriter(opFilePathSecondaryIndex);
			BufferedWriter bw1 = new BufferedWriter(fw1);
			PrintWriter out1 = new PrintWriter(bw1);
			
			// EXTERNAL MERGE SORT LOGIC
			while(!data.isEmpty()){
				
				t = data.poll();
				word = t.word;
				postingList.append(t.postingList);
				pos = t.docId;
				newLine = readers.get(pos).readLine();
				
				if(newLine != null){
					arr = newLine.split("=");
					t = new Tuple();
					t.word=arr[0];
					t.postingList=arr[1];
					t.docId = pos;
					data.add(t);
				}else{
//					new File(path+fileNames.get(pos)).delete(); //To delete partial index
					readers.get(pos).close();
				}
				
				while(!data.isEmpty() && data.peek().word.equals(word)){
					t = data.poll();
					postingList.append(t.postingList);
					pos = t.docId;
					newLine = readers.get(pos).readLine();
					if(newLine != null){
						arr= newLine.split("=");
						t = new Tuple();
						t.word=arr[0];
						t.postingList=arr[1];
						t.docId = pos;
						data.add(t);
					}else{
//						new File(path+fileNames.get(pos)).delete();
						readers.get(pos).close();
					}
				}
				out.println(word+"="+postingList.toString());
				wordsProcessed++;
				postingList = new StringBuilder();
				
				if(wordsProcessed % 100000 == 1){
					out1.println(word+"="+outputFileIndex);
				}
				
				if(wordsProcessed % 100000 == 0){
					out.close(); bw.close(); fw.close();
					outputFileIndex++;
					fw = new FileWriter(opFilePath+"op"+outputFileIndex+".txt");
					bw = new BufferedWriter(fw);
					out = new PrintWriter(bw);
					wordsProcessed=0;
				}
			}
			out.close(); bw.close(); fw.close();
			out1.close(); bw1.close(); fw1.close();
		
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
