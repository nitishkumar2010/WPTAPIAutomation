package Utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class OpenHTMLFileInBrowser {

	public static void main(String[] args) throws IOException {
		
		File htmlFile = new File("D:\\BRP_VUE\\packages\\community-details-page\\dist\\index.html");
		Desktop.getDesktop().browse(htmlFile.toURI());
		
	}
	
}
