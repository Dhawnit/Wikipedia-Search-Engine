import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class StopWords {
	
	public static HashSet<String> stopWordSet = new HashSet<String>();
	
	public static void initializeStopWords() throws Exception{

		FileInputStream fstream = new FileInputStream("/home/dhawnit/workspace/WikipediaSearchEngine/Files/StopWords.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		
		while ((strLine = br.readLine()) != null){
			stopWordSet.add(strLine);
		}
		br.close();
	}
}
