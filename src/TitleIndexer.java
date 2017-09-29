import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TitleIndexer {
	
	static String idTitlePath = "/media/dhawnit/New Volume/IRE/ID_TITLE.txt";
	static String path = "/media/dhawnit/New Volume/IRE/TITLE_INDEX/";
	public static void main(String[] args) {
		
		int counter = 0;
		String strLine = "";
		int opFile = 1;
		
		try{
			
			BufferedReader br = new BufferedReader(new FileReader(idTitlePath));
			
			FileWriter fw = new FileWriter(path+opFile+".txt");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			
			FileWriter fw1 = new FileWriter(path+"secondryTitle.txt");
			BufferedWriter bw1 = new BufferedWriter(fw1);
			PrintWriter out1 = new PrintWriter(bw1);
			
			int index = 0;
			while ((strLine = br.readLine()) != null){
				out.println(strLine);
				counter++;
				
				if(counter % 100000 == 1){
					index = strLine.indexOf('=');
					if(strLine.substring(index+1) != "")
						out1.println(strLine.substring(0, index) +"-"+opFile);
				}
				if(counter % 100000 == 0){
					counter = 0;
					out.close(); bw.close(); fw.close();
					opFile++;
					fw = new FileWriter(path+opFile+".txt");
					bw = new BufferedWriter(fw);
					out = new PrintWriter(bw);
				}
			}
			
			br.close();
			out.close();bw.close();fw.close();
			out1.close();bw1.close();fw1.close();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
