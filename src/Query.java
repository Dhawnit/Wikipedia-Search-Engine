import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

// Run this after TitleIndexer.java
public class Query {
	
	static String opFilePathSecondaryIndex = "/media/dhawnit/New Volume/IRE/SecondaryIndex.txt";
	static String opFilePath = "/media/dhawnit/New Volume/IRE/MASTER_INDEX/";
	static String path = "/media/dhawnit/New Volume/IRE/TITLE_INDEX/";
	static TreeMap<Integer,Integer> secondaryIndexIdTitleMap = new TreeMap<>();
	
	public static void main(String[] args){
		
		try{
			
			TreeMap<String, Integer> secondaryIndex = new TreeMap<>();
			String strLine="";
			String[] arr = null;
			
			//Load Secondary Index in Memory
			BufferedReader br = new BufferedReader(new FileReader(opFilePathSecondaryIndex));
			while ((strLine = br.readLine()) != null){
				arr = strLine.split("=");
				secondaryIndex.put(arr[0], Integer.parseInt(arr[1]));
			}
			br.close();
			
			//Load Secondary Index for Title in Memory
			br = new BufferedReader(new FileReader(path+"secondryTitle.txt"));
			int index=0;
			while ((strLine = br.readLine()) != null){
				index = strLine.indexOf('-');
				secondaryIndexIdTitleMap.put(Integer.parseInt(strLine.substring(0, index)),Integer.parseInt(strLine.substring(index+1)));
			}
			br.close();
			
			StopWords.initializeStopWords();
			
			//Query Input
			System.out.println("Wikipedia Search Engine Started...");
			br = new BufferedReader(new InputStreamReader(System.in));
			String query = null;
			double totalDocuments = 17640866;
			double noOfDocuments = 0;
			double idf = 0;
			TreeMap<String, String> feild = new TreeMap<>();
			
			while(true){
				
				System.out.print("Query > ");
				query = br.readLine();
				if(query.length() == 0) continue;
				if ("quit".equals(query))
					break;
				query = query.toLowerCase().replaceAll("[^a-z: ]", "").trim();
				
				long start=System.currentTimeMillis();
				
				if(query.contains(":")){
					
					feild = new TreeMap<>();
					arr = query.split("\\s");
					String fieldType="";
					for(String word : arr){
						if(word.contains(":")){
							String arr1[] = word.split(":");
							fieldType = arr1[0];
							Stemmer s = new Stemmer();
							String temp = arr1[1];
							s.add(temp.toCharArray(), temp.length());
							s.stem();
							temp = new String(s.getResultBuffer(), 0, s.getResultLength());

							if(!feild.containsKey(temp)){
								feild.put(temp, fieldType);
							}else{
								String str = feild.get(temp);
								feild.put(temp,str+fieldType);
							}
						}else{
							Stemmer s = new Stemmer();
							s.add(word.toCharArray(), word.length());
							s.stem();
							word = new String(s.getResultBuffer(), 0, s.getResultLength());

							if(!feild.containsKey(word)){
								feild.put(word, fieldType);
							}else{
								String str = feild.get(word);
								feild.put(word,str+fieldType);
							}
						}
					}
					query = query.replaceAll("[^a-z ]", " ").trim();
				}

				ArrayList<String> wordsToSearch = new ArrayList<>();
				arr = query.split("\\s+");
				for(String word: arr){
												
					if(!word.equals("") && !StopWords.stopWordSet.contains(word)){ // Do not process if stop word
						// Stemming word
						Stemmer s = new Stemmer();
						s.add(word.toCharArray(), word.length());
						s.stem();
						word = new String(s.getResultBuffer(), 0, s.getResultLength());
						if(!StopWords.stopWordSet.contains(word))
							wordsToSearch.add(word);
					}
				}

				HashMap<Integer, ArrayList<String>> docIdToPostingsMap = new HashMap<>();
				
				for(String word : wordsToSearch){
					
					int opIndex=0;
					if(secondaryIndex.containsKey(word)){
						opIndex = secondaryIndex.get(word);
					}else{
						opIndex = secondaryIndex.lowerEntry(word).getValue();
					}
					
					BufferedReader br1 = new BufferedReader(new FileReader(opFilePath+"op"+opIndex+".txt"));
					String[] postingList = null;
					
					while ((strLine = br1.readLine()) != null){
						arr = strLine.split("=");
						if(word.equals(arr[0])){
							postingList = arr[1].split(",");
							noOfDocuments = postingList.length;
							idf = Math.log10(totalDocuments/noOfDocuments); 
							break;
						}
					}
					br1.close();
					
					if(postingList != null){
						int cnt = 0;
						String str=""; //will be empty for non-field query
						if(feild.containsKey(word))
							str = feild.get(word);
						for(String word1 : postingList){
							arr = word1.split(":");
							int docId = Integer.parseInt(arr[0]);
							boolean check = doCheck(str,arr[1]);
							if(check){
								if(docIdToPostingsMap.containsKey(docId))
									docIdToPostingsMap.get(docId).add(arr[1]+"_"+idf);
								else{
									ArrayList<String> tmp = new ArrayList<String>();
									tmp.add(arr[1]+"_"+idf);
									docIdToPostingsMap.put(docId, tmp);
								}
								cnt++;
							}
							//Comment below line For fast results, May not be accurate
							if(cnt == 120000) break;
						}
					}
				}
				feild.clear();
				if(docIdToPostingsMap.isEmpty()){
					System.out.println("==========Results===========");
					System.out.println("No Results found !!!");
					System.out.println("Time for Searching : "+((System.currentTimeMillis()-start))+" ms\n");
					continue;
				}
				
				List<Map.Entry<Integer, ArrayList<String>>> list = 
				        new ArrayList<>(docIdToPostingsMap.entrySet());
				    Collections.sort(list, new EntryComparator());
				
				// Sorted in Reverse order based on Posting length
				TreeMap<Integer, TreeMap<Integer,Double>> finalMap = new TreeMap<>(Collections.reverseOrder());
				
				for (Map.Entry<Integer, ArrayList<String>> entry : list) {
					
					int docId = entry.getKey();
					ArrayList<String> postings = entry.getValue();
					int postingsLength = postings.size();
					double score = 0;
					if(!finalMap.containsKey(postingsLength)){
						finalMap.put(postingsLength, new TreeMap<Integer,Double>());
					}
					for(String x : postings){
						score += getScore(x);
					}
					finalMap.get(postingsLength).put(docId, score);
				}
				
				int k = 10; // Top 10 results
				int count = 0;
				
				System.out.println("==========Results===========");
				for (Map.Entry<Integer, TreeMap<Integer, Double>> entry : finalMap.entrySet()) {
					List<Map.Entry<Integer,Double>> tmp = entriesSortedByValues(entry.getValue());
					for (Map.Entry<Integer, Double> entry1 : tmp) {
						String title = getTitle(entry1.getKey());
						System.out.println(++count +". "+ title);
						if(count >= k)
							break;
					}
					if(count >= k)
						break;
				}
				System.out.println("Time for Searching : "+((System.currentTimeMillis()-start))+" ms\n");
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean doCheck(String a, String b){
		if(a=="")
			return true;
		boolean flag = false;
		for(int i=0; i<a.length(); i++){
			if(b.contains(a.charAt(i)+""));
				flag=true;
		}
		return flag;
	}
	
	public static double getScore(String str){
		
		double score=0;
		String[] arr = str.split("_");
		String freq = arr[0];
		double idf = Double.parseDouble(arr[1]);
		int t=0,b=0,i=0,r=0,l=0,c=0;
		String tmp = "";
		for(int j=0; j<freq.length(); j++){
			tmp="";
			if(freq.charAt(j)=='t' || freq.charAt(j)=='c' || freq.charAt(j)=='b' || freq.charAt(j)=='l' || freq.charAt(j)=='r' || freq.charAt(j)=='i'){
				char x = freq.charAt(j);
				j++;
				while(j<freq.length() && (freq.charAt(j) >='0' && freq.charAt(j)<='9')){
					tmp+=freq.charAt(j);
					j++;
				}
				if(x=='t') t = Integer.parseInt(tmp);
				else if(x=='c') c = Integer.parseInt(tmp);
				else if(x=='b') b = Integer.parseInt(tmp);
				else if(x=='l') l = Integer.parseInt(tmp);
				else if(x=='r') r = Integer.parseInt(tmp);
				else if(x=='i') i = Integer.parseInt(tmp);
				j--;
			}		
		}
		double tf = Math.log10((double)(t*100+(i+c+r+l)*50+b+1));
		score = tf*idf;
		return score;
	}
	
	public static String getTitle(int Id){
		
		String title = "";
		int op = 0;
		if(secondaryIndexIdTitleMap.containsKey(Id)){
			op = secondaryIndexIdTitleMap.get(Id);
		}else{
			op = secondaryIndexIdTitleMap.lowerEntry(Id).getValue();
		}
		try{
			BufferedReader br = new BufferedReader(new FileReader(path+op+".txt"));
			int index=0;
			String strLine = "";
			while ((strLine = br.readLine()) != null){
				index = strLine.indexOf('=');
				if(strLine.substring(index+1) != ""){
					if(Integer.parseInt(strLine.substring(0, index))==Id){
						title = strLine.substring(index+1);
						break;
					}
				}
			}
			br.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return title;
	}
	
	public static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
		Collections.sort(sortedEntries, 
				new Comparator<Entry<K,V>>() {
			@Override
			public int compare(Entry<K,V> e1, Entry<K,V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		return sortedEntries;
	}
	public static class EntryComparator implements Comparator<Map.Entry<Integer, ArrayList<String>>> {
		public int compare(Map.Entry<Integer, ArrayList<String>> left,
				Map.Entry<Integer, ArrayList<String>> right) {     
			// Right then left to get a descending order
			return Integer.compare(right.getValue().size(), left.getValue().size());
		}
	}
}
