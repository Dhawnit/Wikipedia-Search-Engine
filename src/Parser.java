import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Parser extends DefaultHandler {

	boolean idFlag = false;
	boolean titleFlag = false;
	boolean revisionFlag = false;
	boolean textFlag = false;
	boolean pageFlag = false;
	
	int id = 0;
	StringBuilder textContent = new StringBuilder();
	StringBuilder titleContent = new StringBuilder();
	
	static TreeMap<String,TreeMap<Integer, Data>> invertedIndex = new TreeMap<String,TreeMap<Integer, Data>>();
	static TreeMap<Integer, String> idTitleMap = new TreeMap<>();
	
	static final Pattern ref = Pattern.compile("(<ref.*?>.*?</ref>)|(<!--.*?-->)|(<gallery.*?>.*?</gallery>)|(<center.*?>.*?</center>)", Pattern.DOTALL);
	static final Pattern cite = Pattern.compile("\\{\\{ ?cite(.*?)\\}\\}", Pattern.DOTALL);
	static final Pattern citation = Pattern.compile("\\{\\{citation(.*?)\\}\\}",Pattern.DOTALL);
	static final Pattern UrlFtpFile = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",Pattern.DOTALL);
	static final Pattern doubleCurly = Pattern.compile("\\{\\{.*?\\}\\}", Pattern.DOTALL);
	static final Pattern tags = Pattern.compile("(&lt;|<)(.*?)(/?)(&gt;|>)",Pattern.DOTALL);
	static final Pattern FileImage = Pattern.compile("\\[\\[.*?:.*?\\]\\]");
	static final Pattern categoryPattern = Pattern.compile("\\[\\[category:(.*?)\\]\\]", Pattern.DOTALL);
	static final Pattern referencesPattern = Pattern.compile("== ?references ?==(.*?)==", Pattern.DOTALL);
	static final Pattern externalLinksPattern =Pattern.compile("== ?external links ?==(.*?)\n\n", Pattern.DOTALL);
	static final String infoBoxPattern = "{{infobox";
	static final String filePattern = "[[file:";
	static final String imagePattern = "[[image:";
	static final String citePattern = "{{cite";
	static final String citationPattern = "{{citation";
	static final Pattern[] citesPattern;
	static{
		citesPattern = new Pattern[13];
		citesPattern[0] = Pattern.compile("last ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[1] = Pattern.compile("first ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[2] = Pattern.compile("title ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[3] = Pattern.compile("publisher ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[4] = Pattern.compile("place ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[5] = Pattern.compile("location ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[6] = Pattern.compile("encyclopedia ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[7] = Pattern.compile("authorlink ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[8] = Pattern.compile("year ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[9] = Pattern.compile("quote ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[10] = Pattern.compile("journal ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[11] = Pattern.compile("author ?=(.*?)(\\||\\})", Pattern.DOTALL);
		citesPattern[12] = Pattern.compile("work ?=(.*?)(\\||\\})", Pattern.DOTALL);
	}
	static int totalDocuments = 0;
	static int documentsProcessed = 0;
	static int indexId = 1;
	static String path = "/media/dhawnit/New Volume/IRE/INDEXES/";
	static String idTitlePath = "/media/dhawnit/New Volume/IRE/ID_TITLE.txt";
	static ArrayList<String> fileNames = new ArrayList<String>();
	
	@Override
	public void startElement(String uri, String localName,String qName,
			Attributes attributes) throws SAXException {

		if (qName.equals("id") && !revisionFlag) {
			idFlag = true;
		}
		else if (qName.equals("title")) {
			titleFlag = true;
		}
		else if (qName.equals("revision")) {
			revisionFlag = true;
		}
		else if (qName.equals("text")) {
			textFlag = true;
		}
		else if (qName.equals("page")) {
			pageFlag = true;
		}
	}

	@Override
	public void endElement(String uri, String localName,
			String qName) throws SAXException {

		if (qName.equals("id") && !revisionFlag){
			idFlag = false;
		}
		else if (qName.equals("title")) {
			titleFlag = false;
		}
		else if (qName.equals("revision")) {
			revisionFlag = false;
		}
		else if (qName.equals("text")) {
			textFlag = false;
		}
		else if (qName.equals("page")) {
			
			// Title processing
//			System.out.println(titleContent.toString().replaceAll("[^a-z]", " ").trim().replaceAll("\\s+"," "));
			idTitleMap.put(id, titleContent.toString());
			tokenize(titleContent.toString().toLowerCase().replaceAll("[^a-z]", " ").trim().replaceAll("\\s+"," "), 'T');
			titleContent = new StringBuilder();
			
			// Text Content Processing
			String str = textContent.toString();
			
			str = processCategory(str);
			str = processReferences(str);
			str = processExternalLinks(str);
			str = extractInfoBox(str);
			
//			str = cite.matcher(str).replaceAll(" ");
//	        str = citation.matcher(str).replaceAll(" ");
			str = deleteCitation(str);
	        str = doubleCurly.matcher(str).replaceAll(" ");
	        str = ref.matcher(str).replaceAll(" ");
	        str = UrlFtpFile.matcher(str).replaceAll(" ");
	        str = tags.matcher(str).replaceAll(" ");
	        str = deleteFile(str);
	        str = deleteImage(str);
			str = FileImage.matcher(str).replaceAll(" ");
			
//	        System.out.println(str.replaceAll("[^a-z]", " ").trim().replaceAll("\\s+"," "));
	        tokenize(str.replaceAll("[^a-z]", " ").trim().replaceAll("\\s+"," "),'B');
	        
	        documentsProcessed++;
	        totalDocuments++;
	        if(documentsProcessed % 5000==0){
	        	dumpIndexTofile();
	        	dumpIdTitleMap();
	        	indexId++;
	        	documentsProcessed=0;
	        }
			textContent = new StringBuilder();
			pageFlag = false;
			System.out.print("Total Documents Processed : "+totalDocuments+"\r");
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {

		if (idFlag && !revisionFlag) {
			id = Integer.parseInt(new String(ch,start,length));
		}
		else if (titleFlag) {    		
			titleContent.append(new String(ch,start,length));
		}
		else if (textFlag) {
			textContent.append(new String(ch,start,length).toLowerCase());
		}
	}
	
	public String processCategory(String str){
		
		Matcher categoryMatcher = categoryPattern.matcher(str);
		while (categoryMatcher.find()) {
			//System.out.println(categoryMatcher.group(1).replaceAll("[^a-z]", " ").trim().replaceAll("\\s+"," "));
			tokenize(categoryMatcher.group(1).replaceAll("[^a-z]", " ").trim().replaceAll("\\s+"," "),'C');
		}
		str = categoryMatcher.replaceAll(" ");
		return str;
	}
	
	public String processReferences(String str){
		
		Pattern reflist = Pattern.compile("(;notes)|(\\{\\{reflist.*?\\}\\})",Pattern.DOTALL);
		Matcher referencesMatcher = referencesPattern.matcher(str);

		if(referencesMatcher.find()) {
			String references = referencesMatcher.group();
			//System.out.println(references);
			StringBuilder tmp = new StringBuilder();
			Matcher m = cite.matcher(references);
			while (m.find()) {
				tmp.append(parseCite(m.group(1)));
			}
			m = citation.matcher(references);
			while (m.find()) {
				tmp.append(parseCite(m.group(1)));
			}
			references = reflist.matcher(references).replaceAll(" ");
			//references = cite.matcher(references).replaceAll(" ");
			//references = citation.matcher(references).replaceAll(" ");
			references = deleteCitation(references);
			references = doubleCurly.matcher(references).replaceAll(" ");
			references = ref.matcher(references).replaceAll(" ");
			references = tags.matcher(references).replaceAll(" ");
			references = UrlFtpFile.matcher(references).replaceAll(" ");
			references = deleteFile(references);
			references = deleteImage(references);
			references = FileImage.matcher(references).replaceAll(" ");
			references = references.replaceAll("== ?references ?==", " ");
			references = references.replaceAll("\\[https?:.*? (.*?)\\]", " $1 ");
			tmp.append(references);
			tokenize(tmp.toString().replaceAll("[^a-z]", " ").trim().replaceAll("\\s+"," "), 'R');
		}
		str = referencesMatcher.replaceAll("==");
		return str;
	}
	
	public String processExternalLinks(String str){

		Matcher externalLinksMatcher = externalLinksPattern.matcher(str);
		if(externalLinksMatcher.find()) {
			//System.out.println(externalLinksMatcher.group());
			String externalLinks = externalLinksMatcher.group(1);
			Pattern http = Pattern.compile("(\\[https?:)(.*?) (.*?)(\\])",Pattern.DOTALL);
			Matcher match = http.matcher(externalLinks);
			String res="";
			while(match.find()){
				res = UrlFtpFile.matcher(match.group(3).toString()).replaceAll(" ");
				tokenize(res.replaceAll("[^a-z]", " ").trim().replaceAll("\\s+"," "), 'L');
			}
		}
		str = externalLinksMatcher.replaceAll(" ");
		return str;
	}
	
	public String deleteImage(String str){
		
		while(str.indexOf(imagePattern) != -1){
        	int startPos = str.indexOf(imagePattern);
    	    int bracketCount = 2;
    	    int endPos = startPos + imagePattern.length();
    	    for(; endPos < str.length(); endPos++) {
    	      switch(str.charAt(endPos)) {
    	        case ']':
    	          bracketCount--;
    	          break;
    	        case '[':
    	          bracketCount++;
    	          break;
    	        default:
    	      }
    	      if(bracketCount == 0) break;
    	    }
    	    if(startPos-1 < 0)
    			str = str.substring(endPos);
    		else
    			str = str.substring(0, startPos-1) + str.substring(endPos);
        }
		return str;
	}
	
	public String deleteFile(String str){
		
		while(str.indexOf(filePattern) != -1){
        	
    	    int startPos = str.indexOf(filePattern);
    	    int bracketCount = 2;
    	    int endPos = startPos + filePattern.length();
    	    for(; endPos < str.length(); endPos++) {
    	      switch(str.charAt(endPos)) {
    	        case ']':
    	          bracketCount--;
    	          break;
    	        case '[':
    	          bracketCount++;
    	          break;
    	        default:
    	      }
    	      if(bracketCount == 0) break;
    	    }
    	    if(startPos-1 < 0)
    			str = str.substring(endPos);
    		else
    			str = str.substring(0, startPos-1) + str.substring(endPos);
        }
		return str;
	}
	
	public String deleteCitation(String content) {
		
		//Find the start pos and end pos of citation.
		String str = "";
	    int startPos = content.indexOf(citePattern);
	    str = citePattern;
	    if(startPos < 0){
	    	startPos = content.indexOf(citationPattern);
	    	str = citationPattern;
	    }
	    if(startPos < 0) 
	    	return content;
	    int bracketCount = 2;
	    int endPos = startPos + str.length();
	    for(; endPos < content.length(); endPos++) {
	      switch(content.charAt(endPos)) {
	        case '}':
	          bracketCount--;
	          break;
	        case '{':
	          bracketCount++;
	          break;
	        default:
	      }
	      if(bracketCount == 0) break;
	    }
	    if(endPos+1 >= content.length()){
	    	return content;
	    }
	    //Discard the citation and search for remaining citations.
	    if(startPos-1 < 0)
			content = content.substring(endPos);
		else
			content = content.substring(0, startPos-1) + content.substring(endPos);
		return deleteCitation(content); 
	}
	
	public String parseCite(String c) {
		StringBuilder tmp = new StringBuilder();
		Matcher m = null;
		for (Pattern p : citesPattern) {
			m = p.matcher(c);
			while (m.find()) {
				tmp.append(m.group(1).trim() + " ");
			}
		}
		return tmp.toString().trim();
	}
	
	public String extractInfoBox(String content){
		
		//Find the start pos and end pos of info box.
	    int startPos = content.indexOf(infoBoxPattern);
	    if(startPos < 0) return content;
	    int bracketCount = 2;
	    int endPos = startPos + infoBoxPattern.length();
	    for(; endPos < content.length(); endPos++) {
	      switch(content.charAt(endPos)) {
	        case '}':
	          bracketCount--;
	          break;
	        case '{':
	          bracketCount++;
	          break;
	        default:
	      }
	      if(bracketCount == 0) break;
	    }
	    if(endPos+1 >= content.length()){
	    	return content;
	    }
	    
	    String infoBoxText = content.substring(startPos, endPos+1);
//	    System.out.p1rintln(infoBoxText);
	    
	    // Filter the infobox
	    infoBoxText = deleteCitation(infoBoxText);
//	    infoBoxText = cite.matcher(infoBoxText).replaceAll(" ");
//	    infoBoxText = citation.matcher(infoBoxText).replaceAll(" ");
	    infoBoxText = ref.matcher(infoBoxText).replaceAll(" ");
	    infoBoxText = tags.matcher(infoBoxText).replaceAll(" ");
	    infoBoxText = UrlFtpFile.matcher(infoBoxText).replaceAll(" ");
	    infoBoxText = deleteFile(infoBoxText);
	    infoBoxText = deleteImage(infoBoxText);
	    infoBoxText = FileImage.matcher(infoBoxText).replaceAll(" ");
	   
		processInfobox(new StringBuilder(infoBoxText));
		
		if(startPos-1 < 0)
			content = content.substring(endPos);
		else
			content = content.substring(0, startPos-1) + content.substring(endPos);
		return content;
	}
	
    public void processInfobox(StringBuilder infoboxString) {

    	StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        ArrayList<String> infovalues = new ArrayList<String>();
        
        int length = infoboxString.length();

        for ( int i = 9 ; i < length ; i++ ){

            char currentChar = infoboxString.charAt(i);
            if ( currentChar == '|' ) {
                for ( i++ ; i < length ; i++ ) {
                    currentChar = infoboxString.charAt(i);
                    if ( currentChar == '=' ) {
                        break;
                    }
                    keyBuilder.append(currentChar);
                }
                int count = 0;
                boolean isReplaced = false;
                for ( i++ ; i < length ; i++ ) {
                    currentChar = infoboxString.charAt(i);
                    if ( currentChar == '[' || currentChar == '{' || currentChar == '(' ) {
                        count ++;
                    }
                    else if ( currentChar == ']' || currentChar == '}' || currentChar == ')' ) {
                        count --;
                    }
                    else if ( currentChar == '<' ) {

                        if ( i+4 < length && infoboxString.substring(i+1,i+4).equals("!--") ) {
                        	
                            int locationClose = infoboxString.indexOf("-->" , i+1);
                            if ( locationClose == -1 || locationClose+2 > length ) {
                                i = length-1;
                            }
                            else {
                                i = locationClose+2;
                            }
                            isReplaced = true;
                        }
                    }
                    else if ( count == 0 && currentChar == '|' ) {
                        i--;
                        break;
                    }
                    if ( isReplaced == false )
                        valueBuilder.append(currentChar);
                }
                
                if ( keyBuilder.length() > 0 ) {
                    String value = new String(valueBuilder).trim();
                    if ( value.length() > 0 ){
                    	infovalues.add(value);
                    }
                }
                keyBuilder.setLength(0);
                valueBuilder.setLength(0);
            }
        }
        if ( keyBuilder.length() > 0 ) {
            String value = new String(valueBuilder).trim();
            if ( value.length() > 0 ){
            	infovalues.add(value);
            }
        }
//      System.out.println(infovalues);
        for(String x : infovalues)
        	tokenize(x.replaceAll("[^a-z]", " ").trim().replaceAll("\\s+"," "),'I');
    }
    
    public void tokenize(String str, char x){
    	
    	String[] arr = str.split("\\s+");
		for(String word: arr){
			
			if(!word.equals("") && !StopWords.stopWordSet.contains(word)){ // Do not process if stop word
				// Stemming word
				Stemmer s = new Stemmer();
				s.add(word.toCharArray(), word.length());
				s.stem();
				word = new String(s.getResultBuffer(), 0, s.getResultLength());
				if(!StopWords.stopWordSet.contains(word))
					addToInvertedIndex(word, x);
			}
		}
	}
    
	public void addToInvertedIndex(String word, char x){
		
		// if the word is not in the inverted index
		if (!invertedIndex.containsKey(word)) {
			TreeMap<Integer, Data> temp = new TreeMap<>();
			Data d = new Data();
			if(x=='T') d.countTitle++;
			if(x=='B') d.countBody++;
			if(x=='I') d.countInfobox++;
			if(x=='R') d.countReferences++;
			if(x=='L') d.countExternalLink++;
			if(x=='C') d.countCategory++;
			temp.put(id, d);
			invertedIndex.put(word,temp);
		}
		// if the word is in the inverted index
		else {

			TreeMap<Integer, Data> temp = invertedIndex.get(word);
			// if document id is not present
			if(!temp.containsKey(id)){
				Data d = new Data();
				if(x=='T') d.countTitle++;
				if(x=='B') d.countBody++;
				if(x=='I') d.countInfobox++;
				if(x=='R') d.countReferences++;
				if(x=='L') d.countExternalLink++;
				if(x=='C') d.countCategory++;
				temp.put(id, d);
			}
			// if document id is present
			else{
				if(x=='T') temp.get(id).countTitle++;
				if(x=='B') temp.get(id).countBody++;
				if(x=='I') temp.get(id).countInfobox++;
				if(x=='R') temp.get(id).countReferences++;
				if(x=='L') temp.get(id).countExternalLink++;
				if(x=='C') temp.get(id).countCategory++;
			}
		}
	}
	public static void dumpIdTitleMap(){
		try{
			FileWriter fw = new FileWriter(idTitlePath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);

			for (Map.Entry<Integer, String> entry : idTitleMap.entrySet())
				out.println(entry.getKey() +"=" + entry.getValue());
        	idTitleMap.clear();
			out.close();
			bw.close();
			fw.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void dumpIndexTofile(){

		fileNames.add("index-"+indexId+".txt");
		String indexFile = path+"index-"+indexId+".txt";
		try{

			FileWriter fw = new FileWriter(indexFile);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);

			for (Map.Entry<String,TreeMap<Integer, Data>> entry : invertedIndex.entrySet()) {

				String key = entry.getKey();
				out.print(key +"=");
				TreeMap<Integer, Data> temp = entry.getValue();
				for (Map.Entry<Integer, Data> entry1 : temp.entrySet()) {

					out.print(entry1.getKey()+":");

					if(entry1.getValue().countTitle!=0){
						out.print("t"+entry1.getValue().countTitle);
					}
					if(entry1.getValue().countBody!=0){
						out.print("b"+entry1.getValue().countBody);
					}
					if(entry1.getValue().countInfobox!=0){
						out.print("i"+entry1.getValue().countInfobox);
					}
					if(entry1.getValue().countReferences!=0){
						out.print("r"+entry1.getValue().countReferences);
					}
					if(entry1.getValue().countExternalLink!=0){
						out.print("l"+entry1.getValue().countExternalLink);
					}
					if(entry1.getValue().countCategory!=0){
						out.print("c"+entry1.getValue().countCategory);
					}
					out.print(",");
				}
				out.println();
			}
			invertedIndex.clear();
			out.close();
			bw.close();
			fw.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}