import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SearchEngine {

	public static void main(String[] args) {
		
		String ipFile = "/media/dhawnit/New Volume/IRE/enwiki-latest-pages-articles-multistream.xml";

		try {
			StopWords.initializeStopWords();
			long start=System.currentTimeMillis();

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			Parser handler = new Parser();
			saxParser.parse(ipFile, handler);
			Parser.dumpIndexTofile();
			Parser.dumpIdTitleMap();
			System.out.println();
			System.out.println("Total time for Creating Partial Indexes : "+((System.currentTimeMillis()-start))+" ms");
			
			start=System.currentTimeMillis();
			Merge.merge();
			System.out.println("Total time for External Merge Sort: "+((System.currentTimeMillis()-start))+" ms");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
