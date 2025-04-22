package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to read the resulted xml files and get the required html data
 * 
 * @author nikumar
 *
 */
public class ReadResultXMLAndExtractResultData {

	/**
	 * Method to read the resulted xml files and get the required html data
	 * 
	 * @param buildNum
	 * @param htmlFileName
	 * @param suiteName2
	 * @return
	 */
	public static String readFileAndPerformInsertion(String buildNum, String htmlFileName, String suiteName2) {
		String filePath = System.getProperty("user.dir") + File.separator + "test-output" + File.separator
				+ "jenkins-BRPExecution-" + buildNum.trim() + File.separator + "xml" + File.separator;

		String remoteAddress = "";
		try {
			Properties configProperty = new Properties();
			File configfile = new File(System.getProperty("user.dir") + File.separator + "Parameters" + File.separator
					+ "Config.properties");
			FileInputStream fileIn = new FileInputStream(configfile);
			configProperty.load(fileIn);
			remoteAddress = configProperty.getProperty("RemoteAddress");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String path = "http://" + remoteAddress + ":8084/test-output/" + "jenkins-BRPExecution-"
				+ buildNum.trim() + "/html/" + htmlFileName;
		
		String htmlText = "";
		System.out.println(filePath);
		try {
			File xmlFileFolderPath = new File(filePath);

			System.out.println("XML file's Folder is " + xmlFileFolderPath);
			File[] listOfFiles = xmlFileFolderPath.listFiles();

			for (File file : listOfFiles) {
				if (file.isFile()) {

					// Get xml file for which data is required
					File resultXmlFile = new File(xmlFileFolderPath + File.separator + file.getName());
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document document = dBuilder.parse(resultXmlFile);

					document.getDocumentElement().normalize();

					NodeList nodeList = document.getElementsByTagName("testsuite");

					for (int temp = 0; temp < nodeList.getLength(); temp++) {
						Node node = nodeList.item(temp);

						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element nodeElement = (Element) node;
							String suiteName = nodeElement.getAttribute("name");
							if (suiteName.equalsIgnoreCase(suiteName2)) {
								String totalTestCaseCount = nodeElement.getAttribute("tests");
								String failureTestCasesCount = nodeElement.getAttribute("failures");
								String skippedTestCasesCount = nodeElement.getAttribute("skipped");
								int passedTestCasesCount = Integer.parseInt(totalTestCaseCount)
										- Integer.parseInt(skippedTestCasesCount)
										- Integer.parseInt(failureTestCasesCount);

								DecimalFormat df = new DecimalFormat("###.##");
								double passPer = (passedTestCasesCount * 100.00)
										/ (Integer.parseInt(totalTestCaseCount));

								try {
									passPer = Double.parseDouble(df.format(passPer));
								} catch (NumberFormatException e) {
									// Set default value of 0.00 in case of
									// Number
									// format exception
									passPer = 0.00;
								}

								htmlText = "<tr><td> <a href ='" + path + "'>" + suiteName + "</td>"
										+ "<td align='center'>" + totalTestCaseCount + " </td>" + "<td align='center'>"
										+ passedTestCasesCount + " </td>" + "<td align='center'>"
										+ failureTestCasesCount + " </td>" + "<td align='center'>"
										+ skippedTestCasesCount + " </td>" + "<td align='center'>" + passPer + " %"
										+ " </td></tr>";

								System.out.println(htmlText);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return htmlText;
	}
}