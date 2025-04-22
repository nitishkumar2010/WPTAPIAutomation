package WPTLighthouse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import Utils.Browser;
import Utils.Config;
import Utils.Helper;
import Utils.TestDataReader;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;

/**
 * Class for performing actions related to API's
 * 
 * @author nikumar
 *
 */
public class APIHelperLighthouse1 {

	public Config testConfig;

	public enum PageTypeLighthouse {
		HomePage, SearchPage
	}

	public APIHelperLighthouse1(Config testConfig) {
		this.testConfig = testConfig;
	}

	public void submitAPIAndGetResponse(String url, PageTypeLighthouse pageType, String fileName) {

		String apiKey = testConfig.getRunTimeProperty("APIKey");
		String apiUrl = testConfig.getRunTimeProperty("APIUrl");

		ValidatableResponse response = RestAssured.given().relaxedHTTPSValidation().header("X-WPT-API-KEY", apiKey)
				.queryParam("url", url).queryParam("location", "Dulles:Chrome.FIOS").queryParam("f", "json")
				.queryParam("runs", 2).queryParam("fvonly", 1).queryParam("lighthouse", 1).when().get(apiUrl).then()
				.log().body();

		String userUrl = response.extract().path("data.userUrl").toString();
		String apiJsonUrl = response.extract().path("data.jsonUrl").toString();

		ValidatableResponse responseForJson = RestAssured.given().relaxedHTTPSValidation().queryParam("f", "json")
				.when().get(apiJsonUrl).then().log().body();

		while (!responseForJson.extract().path("statusCode").toString().equals("200")) {
			Browser.waitWithoutLogging(testConfig, 30);
			responseForJson = RestAssured.given().relaxedHTTPSValidation().queryParam("f", "json").when()
					.get(apiJsonUrl).then(); //.log().body();
		}

		testConfig.logComment(responseForJson.extract().path("statusCode").toString());
		testConfig.logComment("WebPageTest API Run URL: " + apiJsonUrl);
		testConfig.logComment("WebPageTest API User URL: " + userUrl);

		submitValuesInLighthouseCSV(apiJsonUrl, userUrl, responseForJson, pageType, fileName);
		submitValuesInCompleteDataCSV(apiJsonUrl, userUrl, responseForJson, "First Run", pageType, fileName);
		submitValuesInCompleteDataCSV(apiJsonUrl, userUrl, responseForJson, "Second Run", pageType, fileName);

	}
	
	private void submitValuesInCompleteDataCSV(String apiUrl, String userUrl, ValidatableResponse responseForJson, String run,
			PageTypeLighthouse pageType, String fileName) {

		String loadTime = "", ttfb = "", startRender = "", speedIndexTime = "", documentRequestsCount = "",
				documentBytesIn = "", documentTime = "", fullyLoadedRequestsCount = "", fullBytesIn = "",
				fullyLoadedTime = "", largestContentfulPaint = "", totalBlockingTime = "", cumulativeLayoutShift = "";
		int sheetNum = 0;

		switch (run) {
		case "First Run":
			loadTime = responseForJson.extract().path("data.runs.1.firstView.loadTime").toString();
			ttfb = responseForJson.extract().path("data.runs.1.firstView.TTFB").toString();
			startRender = responseForJson.extract().path("data.runs.1.firstView.render").toString();
			speedIndexTime = responseForJson.extract().path("data.runs.1.firstView.SpeedIndex").toString();

			documentRequestsCount = responseForJson.extract().path("data.runs.1.firstView.requestsDoc").toString();
			documentBytesIn = responseForJson.extract().path("data.runs.1.firstView.bytesInDoc").toString();
			documentTime = responseForJson.extract().path("data.runs.1.firstView.docTime").toString();

			fullyLoadedRequestsCount = responseForJson.extract().path("data.runs.1.firstView.requestsFull").toString();
			fullBytesIn = responseForJson.extract().path("data.runs.1.firstView.bytesIn").toString();
			fullyLoadedTime = responseForJson.extract().path("data.runs.1.firstView.fullyLoaded").toString();
			
			largestContentfulPaint = responseForJson.extract().path("data.runs.1.firstView[\"chromeUserTiming.LargestContentfulPaint\"]").toString();
			cumulativeLayoutShift = responseForJson.extract().path("data.runs.1.firstView[\"chromeUserTiming.CumulativeLayoutShift\"]").toString();
			totalBlockingTime = responseForJson.extract().path("data.runs.1.firstView.TotalBlockingTime").toString();
			break;

		case "Second Run":
			loadTime = responseForJson.extract().path("data.runs.2.firstView.loadTime").toString();
			ttfb = responseForJson.extract().path("data.runs.2.firstView.TTFB").toString();
			startRender = responseForJson.extract().path("data.runs.2.firstView.render").toString();
			speedIndexTime = responseForJson.extract().path("data.runs.2.firstView.SpeedIndex").toString();

			documentRequestsCount = responseForJson.extract().path("data.runs.2.firstView.requestsDoc").toString();
			documentBytesIn = responseForJson.extract().path("data.runs.2.firstView.bytesInDoc").toString();
			documentTime = responseForJson.extract().path("data.runs.2.firstView.docTime").toString();

			fullyLoadedRequestsCount = responseForJson.extract().path("data.runs.2.firstView.requestsFull").toString();
			fullBytesIn = responseForJson.extract().path("data.runs.2.firstView.bytesIn").toString();
			fullyLoadedTime = responseForJson.extract().path("data.runs.2.firstView.fullyLoaded").toString();
			
			largestContentfulPaint = responseForJson.extract().path("data.runs.2.firstView[\"chromeUserTiming.LargestContentfulPaint\"]").toString();
			cumulativeLayoutShift = responseForJson.extract().path("data.runs.2.firstView[\"chromeUserTiming.CumulativeLayoutShift\"]").toString();
			totalBlockingTime = responseForJson.extract().path("data.runs.2.firstView.TotalBlockingTime").toString();
			break;
		}

		
		String seventyFivePercFCP = responseForJson.extract().path("data.median.firstView.CrUX.metrics.first_contentful_paint.percentiles.p75").toString();
		String seventyFivePercLCP = responseForJson.extract().path("data.median.firstView.CrUX.metrics.largest_contentful_paint.percentiles.p75").toString();
		String seventyFivePercCLS = responseForJson.extract().path("data.median.firstView.CrUX.metrics.cumulative_layout_shift.percentiles.p75").toString();
		String seventyFivePercTTFB = responseForJson.extract().path("data.median.firstView.CrUX.metrics.largest_contentful_paint_image_time_to_first_byte.percentiles.p75").toString();
		String seventyFivePercINP = responseForJson.extract().path("data.median.firstView.CrUX.metrics.interaction_to_next_paint.percentiles.p75").toString();
		
		
		switch (pageType) {
		case HomePage:
			sheetNum = 0;
			break;

		case SearchPage:
			sheetNum = 1;
			break;
		}

		try {
			InputStream myxls = new FileInputStream(
					new File(System.getProperty("user.dir") + "\\Parameters\\" + fileName));

			@SuppressWarnings("resource")
			Workbook workbook = new XSSFWorkbook(myxls);
			Sheet sheet = workbook.getSheetAt(sheetNum);

			DecimalFormat formatter = new DecimalFormat("#,###");
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);

			int rowCount = sheet.getLastRowNum();
			Row row = sheet.createRow(++rowCount);

			int columnCount = 0;
			Cell cell = row.createCell(columnCount);
			cell.setCellValue(Helper.getCurrentDate("MM.dd.YY"));

			columnCount = 1;
			float loadTimeVal = Float.parseFloat(loadTime) / 1000;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(loadTimeVal));

			columnCount = 2;
			float ttfbVal = Float.parseFloat(ttfb) / 1000;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(ttfbVal));

			columnCount = 3;
			float startRenderVal = Float.parseFloat(startRender) / 1000;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(startRenderVal));

			columnCount = 4;
			cell = row.createCell(columnCount);
			cell.setCellValue(speedIndexTime);
			
			columnCount = 5;
			float lcpVal = Float.parseFloat(largestContentfulPaint) / 1000;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(lcpVal));
			
			columnCount = 6;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(Float.parseFloat(cumulativeLayoutShift)));

			columnCount = 7;
			float tbtVal = Float.parseFloat(totalBlockingTime) / 1000;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(tbtVal));
			
			columnCount = 8;
			float documentTimeVal = Float.parseFloat(documentTime) / 1000;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(documentTimeVal));
			
			columnCount = 9;
			cell = row.createCell(columnCount);
			cell.setCellValue(documentRequestsCount);

			columnCount = 10;
			int documentBytesInVal = Integer.parseInt(documentBytesIn) / 1024;
			cell = row.createCell(columnCount);
			cell.setCellValue(formatter.format(documentBytesInVal) + " KB");

			columnCount = 11;
			float fullyLoadedTimeVal = Float.parseFloat(fullyLoadedTime) / 1000;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(fullyLoadedTimeVal));

			columnCount = 12;
			cell = row.createCell(columnCount);
			cell.setCellValue(fullyLoadedRequestsCount);

			columnCount = 13;
			int fullBytesInVal = Integer.parseInt(fullBytesIn) / 1024;
			cell = row.createCell(columnCount);
			cell.setCellValue(formatter.format(fullBytesInVal) + " KB");

			columnCount = 14;
			float seventyFiveFCP = Float.parseFloat(seventyFivePercFCP) / 1000;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(seventyFiveFCP));
			
			columnCount = 15;
			float seventyFiveLCP = Float.parseFloat(seventyFivePercLCP) / 1000;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(seventyFiveLCP));
			
			columnCount = 16;
			float seventyFiveCLS = Float.parseFloat(seventyFivePercCLS);
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(seventyFiveCLS));
			
			columnCount = 17;
			float seventyFiveTTFB = Float.parseFloat(seventyFivePercTTFB) / 1000;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(seventyFiveTTFB));
			
			columnCount = 18;
			float seventyFiveINP = Float.parseFloat(seventyFivePercINP) / 1000;
			cell = row.createCell(columnCount);
			cell.setCellValue(df.format(seventyFiveINP));
			
			columnCount = 19;
			cell = row.createCell(columnCount);
			cell.setCellValue(run);

			columnCount = 20;
			cell = row.createCell(columnCount);
			cell.setCellValue(userUrl);

			FileOutputStream outFile = new FileOutputStream(
					new File(System.getProperty("user.dir") + "\\Parameters\\" + fileName));
			workbook.write(outFile);
			outFile.close();
			testConfig.logComment("File updated!!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	private void submitValuesInLighthouseCSV(String apiUrl, String userUrl, ValidatableResponse responseForJson,
			PageTypeLighthouse pageType, String fileName) {

		String fcpTime = "", speedIndex = "", lcpTime = "", interactiveTime = "", totalBlockingTime = "",
				cumulativeLayoutShift = "", performanceScore = "", accessibilityScore = "", bestPracticeScore = "",
				pwaScore = "", seoScore = "";
		int sheetNum = 0;

		fcpTime = responseForJson.extract().path("data.lighthouse.audits.first-contentful-paint.displayValue")
				.toString();
		speedIndex = responseForJson.extract().path("data.lighthouse.audits.speed-index.displayValue").toString();
		lcpTime = responseForJson.extract().path("data.lighthouse.audits.largest-contentful-paint.displayValue")
				.toString();
		interactiveTime = responseForJson.extract().path("data.lighthouse.audits.interactive.displayValue").toString();
		totalBlockingTime = responseForJson.extract().path("data.lighthouse.audits.total-blocking-time.displayValue")
				.toString();
		cumulativeLayoutShift = responseForJson.extract()
				.path("data.lighthouse.audits.cumulative-layout-shift.displayValue").toString();

		performanceScore = responseForJson.extract().path("data.average.firstView.'lighthouse.Performance'").toString();
		accessibilityScore = responseForJson.extract().path("data.average.firstView.'lighthouse.Accessibility'")
				.toString();
		bestPracticeScore = responseForJson.extract().path("data.average.firstView.'lighthouse.BestPractices'")
				.toString();
		seoScore = responseForJson.extract().path("data.average.firstView.'lighthouse.SEO'").toString();
		pwaScore = responseForJson.extract().path("data.average.firstView.'lighthouse.PWA'").toString();

		switch (pageType) {
		case HomePage:
			sheetNum = 2;
			break;

		case SearchPage:
			sheetNum = 3;

		}

		try {
			InputStream myxls = new FileInputStream(
					new File(System.getProperty("user.dir") + "\\Parameters\\" + fileName));

			@SuppressWarnings("resource")
			Workbook workbook = new XSSFWorkbook(myxls);
			Sheet sheet = workbook.getSheetAt(sheetNum);

			int rowCount = sheet.getLastRowNum();
			Row row = sheet.createRow(++rowCount);

			int columnCount = 0;
			Cell cell = row.createCell(columnCount);
			cell.setCellValue(Helper.getCurrentDate("MM.dd.YY"));

			columnCount = 1;
			cell = row.createCell(columnCount);
			cell.setCellValue(fcpTime);

			columnCount = 2;
			cell = row.createCell(columnCount);
			cell.setCellValue(speedIndex);

			columnCount = 3;
			cell = row.createCell(columnCount);
			cell.setCellValue(lcpTime);

			columnCount = 4;
			cell = row.createCell(columnCount);
			cell.setCellValue(interactiveTime);

			columnCount = 5;
			cell = row.createCell(columnCount);
			cell.setCellValue(totalBlockingTime);

			columnCount = 6;
			cell = row.createCell(columnCount);
			cell.setCellValue(cumulativeLayoutShift);

			columnCount = 7;
			int performance_score = (int) (Float.parseFloat(performanceScore) * 100);
			cell = row.createCell(columnCount);
			cell.setCellValue(performance_score);

			columnCount = 8;
			int accessibility_score = (int) (Float.parseFloat(accessibilityScore) * 100);
			cell = row.createCell(columnCount);
			cell.setCellValue(accessibility_score);

			columnCount = 9;
			int bestPractice_score = (int) (Float.parseFloat(bestPracticeScore) * 100);
			cell = row.createCell(columnCount);
			cell.setCellValue(bestPractice_score);

			columnCount = 10;
			int seo_score = (int) (Float.parseFloat(seoScore) * 100);
			cell = row.createCell(columnCount);
			cell.setCellValue(seo_score);

			columnCount = 11;
			int pwa_score = (int) (Float.parseFloat(pwaScore) * 100);
			cell = row.createCell(columnCount);
			cell.setCellValue(pwa_score);

			columnCount = 12;
			cell = row.createCell(columnCount);
			cell.setCellValue(userUrl);

			FileOutputStream outFile = new FileOutputStream(
					new File(System.getProperty("user.dir") + "\\Parameters\\" + fileName));
			workbook.write(outFile);
			outFile.close();
			testConfig.logComment("File updated!!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void readValuesAndExtractDifference(PageTypeLighthouse pageType) {

		String sheetname = "", headingLabel = "";
		switch (pageType) {
		case HomePage:
			sheetname = "HomePage";
			headingLabel = "Home Page";
			break;

		case SearchPage:
			sheetname = "SearchPage";
			headingLabel = "Search Page";
			break;

		}

		TestDataReader categoryReader = testConfig.getCachedTestDataReaderObject(sheetname);
		int totalRecords = categoryReader.getRecordsNum();
		testConfig.logComment(String.valueOf(totalRecords));

		try {
			
			String filename = "LighthouseStats_" + Helper.getCurrentDate("dd-MM-yyyy") + ".txt";

			try {
				File myObj = new File(System.getProperty("user.dir") + "\\Parameters\\" + filename);
				if (myObj.createNewFile()) {
					testConfig.logComment("File created: " + myObj.getName());
				} else {
					testConfig.logComment("File already exists.");
				}
			} catch (IOException e) {
				testConfig.logComment("An error occurred.");
				e.printStackTrace();
			}

			FileWriter fw = new FileWriter(System.getProperty("user.dir") + "\\Parameters\\" + filename, true);

			if (totalRecords >= 3) {
				
				System.out.println(categoryReader.GetData(totalRecords - 1, "Date"));
				System.out.println(categoryReader.GetData(totalRecords - 2, "Date"));
				System.out.println(categoryReader.GetData(totalRecords - 3, "Date"));
				
				String lastRowFCP = categoryReader.GetData(totalRecords - 1, "First Contentful Paint");
				String lastRowLCP = categoryReader.GetData(totalRecords - 1, "Largest Contentful Paint");
				String lastRowInteractiveTime = categoryReader.GetData(totalRecords - 1, "Time to Interactive");
				String lastRowSpeedIndex = categoryReader.GetData(totalRecords - 1, "Speed Index");
				String lastRowTBT = categoryReader.GetData(totalRecords - 1, "Total Blocking Time");

				String secondLastRowFCP = categoryReader.GetData(totalRecords - 2, "First Contentful Paint");
				String secondLastRowLCP = categoryReader.GetData(totalRecords - 2, "Largest Contentful Paint");
				String secondLastRowInteractiveTime = categoryReader.GetData(totalRecords - 2, "Time to Interactive");
				String secondLastRowSpeedIndex = categoryReader.GetData(totalRecords - 2, "Speed Index");
				String secondLastRowTBT = categoryReader.GetData(totalRecords - 2, "Total Blocking Time");


				fw.write(headingLabel + "\n");
				
				System.out.println("First Contentful Paint" + " : " + lastRowFCP + " : " + secondLastRowFCP);
				findDifferenceAmongLastRuns(fw, "First Contentful Paint", lastRowFCP, secondLastRowFCP);
				
				System.out.println("Largest Contentful Paint" + " : " + lastRowFCP + " : " + secondLastRowLCP);
				findDifferenceAmongLastRuns(fw, "Largest Contentful Paint", lastRowLCP, secondLastRowLCP);
				
				System.out.println("Time to Interactive" + " : " + lastRowInteractiveTime + " : " + secondLastRowInteractiveTime);
				findDifferenceAmongLastRuns(fw, "Time to Interactive", lastRowInteractiveTime, secondLastRowInteractiveTime);
				
				System.out.println("Speed Index" + " : " + lastRowSpeedIndex + " : " + secondLastRowSpeedIndex);
				findDifferenceAmongLastRuns(fw, "Speed Index", lastRowSpeedIndex, secondLastRowSpeedIndex);
				
				System.out.println("Total Blocking Time" + " : " + lastRowTBT + " : " + secondLastRowTBT);
				findDifferenceAmongLastRuns(fw, "Total Blocking Time", lastRowTBT, secondLastRowTBT);
				
				fw.close();
			} else {
				fw.write(headingLabel + "\n");
				fw.write("Not having enough records to compare... skipping comparison for " + headingLabel + "\n");
				fw.close();
			}
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}

	}

	private void findDifferenceAmongLastRuns(FileWriter fw, String label, String lastRowData,
			String secondLastRowData) throws IOException {
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		fw.write(label + ": " + lastRowData + " \n");
		float lastRowSpeedIndexValue = Float.parseFloat(lastRowData.replace(",", "").replaceAll("[^0-9.]", "").trim());
		float secondLastRowSpeedIndexValue = Float.parseFloat(secondLastRowData.replace(" s", ""));

		float speedIndexValueDiff = lastRowSpeedIndexValue - secondLastRowSpeedIndexValue;
		if (speedIndexValueDiff > 0) {
			fw.write(label + " - Performance has declined by "
					+ String.valueOf(df.format(Math.abs(speedIndexValueDiff))) + " seconds" + " \n");
		} else {
			fw.write(label + " - Performance has improved by "
					+ String.valueOf(df.format(Math.abs(speedIndexValueDiff))) + " seconds" + " \n");
		}
		
	}

}
