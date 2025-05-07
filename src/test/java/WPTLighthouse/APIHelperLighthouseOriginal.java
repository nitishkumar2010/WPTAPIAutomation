package WPTLighthouse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

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
public class APIHelperLighthouseOriginal {

    public Config testConfig;
    
    private static final String INFLUX_HOST = "http://localhost:8086";
    private static final String BUCKET = "wpt_metrics";
    private static final String TOKEN = "JBEzuAKGn1HWf-7h-1faSsIw4-CzP0X-GsMbiqenDOoSgvMSrVZhSvaq-JnbdhGAFQlVHtlvFPdjiGv7DuG5nw==";
    private static final String ORG = "EXSquared";


    public enum PageTypeLighthouse {
        HomePage, SearchPage
    }

    public APIHelperLighthouseOriginal(Config testConfig) {
        this.testConfig = testConfig;
    }

    public void submitAPIAndGetResponse(String url, PageTypeLighthouse pageType, String fileName, int mobileRun) {

        String apiKey = testConfig.getRunTimeProperty("APIKey");
        String apiUrl = testConfig.getRunTimeProperty("APIUrl");

		ValidatableResponse response = RestAssured.given().relaxedHTTPSValidation().header("X-WPT-API-KEY", apiKey)
				.queryParam("url", url).queryParam("location", "Dulles:Chrome.FIOS").queryParam("f", "json")
				.queryParam("mobile", mobileRun).queryParam("runs", 2).queryParam("fvonly", 1)
				.queryParam("lighthouse", 1).when().get(apiUrl).then().log().body();

        String userUrl = response.extract().path("data.userUrl").toString();
        String apiJsonUrl = response.extract().path("data.jsonUrl").toString();

        ValidatableResponse responseForJson = RestAssured.given().relaxedHTTPSValidation().queryParam("f", "json")
                .when().get(apiJsonUrl).then().log().body();

        while (!responseForJson.extract().path("statusCode").toString().equals("200")) {
            Browser.waitWithoutLogging(testConfig, 60);
            responseForJson = RestAssured.given().relaxedHTTPSValidation().queryParam("f", "json").when().get(apiJsonUrl).then().log().body();
        }
        
        testConfig.logComment("WebPageTest API Run URL: " + apiJsonUrl);
        testConfig.logComment("Status Code for the API: " + responseForJson.extract().path("statusCode").toString());
        testConfig.logComment("WebPageTest API User URL: " + userUrl);

        submitValuesInLighthouseCSV(apiJsonUrl, userUrl, responseForJson, pageType, fileName, mobileRun, url);
        submitValuesInCompleteDataCSV(apiJsonUrl, userUrl, responseForJson, "First Run", pageType, fileName, mobileRun);
        submitValuesInCompleteDataCSV(apiJsonUrl, userUrl, responseForJson, "Second Run", pageType, fileName, mobileRun);
    }

    private void submitValuesInCompleteDataCSV(String apiUrl, String userUrl, ValidatableResponse responseForJson, String run,
                                               PageTypeLighthouse pageType, String fileName, int mobileRun) {

        String loadTime = "", ttfb = "", startRender = "", speedIndexTime = "", documentRequestsCount = "",
                documentBytesIn = "", documentTime = "", fullyLoadedRequestsCount = "", fullBytesIn = "",
                fullyLoadedTime = "", largestContentfulPaint = "", totalBlockingTime = "", cumulativeLayoutShift = "";
        int sheetNum = 0;
        String runPrefix = (run.equals("First Run")) ? "1" : "2";
        String platform = (mobileRun == 0) ? "Desktop" : "Mobile";
        
        loadTime = responseForJson.extract().path("data.runs." + runPrefix + ".firstView.loadTime").toString();
        ttfb = responseForJson.extract().path("data.runs." + runPrefix + ".firstView.TTFB").toString();
        startRender = responseForJson.extract().path("data.runs." + runPrefix + ".firstView.render").toString();
        speedIndexTime = responseForJson.extract().path("data.runs." + runPrefix + ".firstView.SpeedIndex").toString();
        documentRequestsCount = responseForJson.extract().path("data.runs." + runPrefix + ".firstView.requestsDoc").toString();
        documentBytesIn = responseForJson.extract().path("data.runs." + runPrefix + ".firstView.bytesInDoc").toString();
        documentTime = responseForJson.extract().path("data.runs." + runPrefix + ".firstView.docTime").toString();
        fullyLoadedRequestsCount = responseForJson.extract().path("data.runs." + runPrefix + ".firstView.requestsFull").toString();
        fullBytesIn = responseForJson.extract().path("data.runs." + runPrefix + ".firstView.bytesIn").toString();
        fullyLoadedTime = responseForJson.extract().path("data.runs." + runPrefix + ".firstView.fullyLoaded").toString();
        largestContentfulPaint = responseForJson.extract().path("data.runs." + runPrefix + ".firstView[\"chromeUserTiming.LargestContentfulPaint\"]").toString();
        cumulativeLayoutShift = responseForJson.extract().path("data.runs." + runPrefix + ".firstView[\"chromeUserTiming.CumulativeLayoutShift\"]").toString();
        totalBlockingTime = responseForJson.extract().path("data.runs." + runPrefix + ".firstView.TotalBlockingTime").toString();

        String seventyFivePercFCP = responseForJson.extract().path("data.median.firstView.CrUX.metrics.first_contentful_paint.percentiles.p75").toString();
        String seventyFivePercLCP = responseForJson.extract().path("data.median.firstView.CrUX.metrics.largest_contentful_paint.percentiles.p75").toString();
        String seventyFivePercCLS = responseForJson.extract().path("data.median.firstView.CrUX.metrics.cumulative_layout_shift.percentiles.p75").toString();
        String seventyFivePercTTFB = responseForJson.extract().path("data.median.firstView.CrUX.metrics.largest_contentful_paint_image_time_to_first_byte.percentiles.p75").toString();
        String seventyFivePercINP = responseForJson.extract().path("data.median.firstView.CrUX.metrics.interaction_to_next_paint.percentiles.p75").toString();

        sheetNum = (pageType == PageTypeLighthouse.HomePage) ? 0 : 1;

        try (InputStream myxls = new FileInputStream(new File(System.getProperty("user.dir") + "\\Parameters\\" + fileName));
             Workbook workbook = new XSSFWorkbook(myxls);
             FileOutputStream outFile = new FileOutputStream(new File(System.getProperty("user.dir") + "\\Parameters\\" + fileName))) {

            Sheet sheet = workbook.getSheetAt(sheetNum);
            DecimalFormat formatter = new DecimalFormat("#,###");
            DecimalFormat df = new DecimalFormat("#.##");

            int rowCount = sheet.getLastRowNum();
            Row row = sheet.createRow(++rowCount);
            int columnCount = 0;

            row.createCell(columnCount++).setCellValue(Helper.getCurrentDate("MM.dd.YY"));
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(loadTime) / 1000));
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(ttfb) / 1000));
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(startRender) / 1000));
            row.createCell(columnCount++).setCellValue(speedIndexTime);
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(largestContentfulPaint) / 1000));
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(cumulativeLayoutShift)));
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(totalBlockingTime) / 1000));
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(documentTime) / 1000));
            row.createCell(columnCount++).setCellValue(documentRequestsCount);
            row.createCell(columnCount++).setCellValue(formatter.format(Integer.parseInt(documentBytesIn) / 1024) + " KB");
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(fullyLoadedTime) / 1000));
            row.createCell(columnCount++).setCellValue(fullyLoadedRequestsCount);
            row.createCell(columnCount++).setCellValue(formatter.format(Integer.parseInt(fullBytesIn) / 1024) + " KB");
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(seventyFivePercFCP) / 1000));
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(seventyFivePercLCP) / 1000));
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(seventyFivePercCLS)));
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(seventyFivePercTTFB) / 1000));
            row.createCell(columnCount++).setCellValue(df.format(Float.parseFloat(seventyFivePercINP) / 1000));
            row.createCell(columnCount++).setCellValue(run);
            row.createCell(columnCount++).setCellValue(platform);
            row.createCell(columnCount).setCellValue(userUrl);

            workbook.write(outFile);
            testConfig.logComment("File updated!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void submitValuesInLighthouseCSV(String apiUrl, String userUrl, ValidatableResponse responseForJson,
                                            PageTypeLighthouse pageType, String fileName, int mobileRun, String url) {

        String fcpTime = "", speedIndex = "", lcpTime = "", interactiveTime = "", totalBlockingTime = "",
                cumulativeLayoutShift = "", performanceScore = "", accessibilityScore = "", bestPracticeScore = "",
                pwaScore = "", seoScore = "";
        int sheetNum = (pageType == PageTypeLighthouse.HomePage) ? 2 : 3;
        String platform = (mobileRun == 0) ? "Desktop" : "Mobile";
        
        fcpTime = Optional.ofNullable(responseForJson.extract().path("data.lighthouse.audits.first-contentful-paint.displayValue"))
                .map(Object::toString).orElse("NULL");

		speedIndex = Optional.ofNullable(responseForJson.extract().path("data.lighthouse.audits.speed-index.displayValue"))
				.map(Object::toString).orElse("NULL");

		lcpTime = Optional.ofNullable(responseForJson.extract().path("data.lighthouse.audits.largest-contentful-paint.displayValue"))
				.map(Object::toString).orElse("NULL");

		interactiveTime = Optional.ofNullable(responseForJson.extract().path("data.lighthouse.audits.interactive.displayValue"))
				.map(Object::toString).orElse("NULL");

		totalBlockingTime = Optional.ofNullable(responseForJson.extract().path("data.lighthouse.audits.total-blocking-time.displayValue"))
				.map(Object::toString).orElse("NULL");

		cumulativeLayoutShift = Optional.ofNullable(responseForJson.extract().path("data.lighthouse.audits.cumulative-layout-shift.displayValue"))
				.map(Object::toString).orElse("NULL");

		performanceScore = Optional.ofNullable(responseForJson.extract().path("data.runs.1.firstView[\"lighthouse.Performance\"]"))
				.map(Object::toString).orElse("NULL");

		accessibilityScore = Optional.ofNullable(responseForJson.extract().path("data.runs.1.firstView[\"lighthouse.Accessibility\"]"))
				.map(Object::toString).orElse("NULL");

		bestPracticeScore = Optional.ofNullable(responseForJson.extract().path("data.runs.1.firstView[\"lighthouse.BestPractices\"]"))
				.map(Object::toString).orElse("NULL");

		seoScore = Optional.ofNullable(responseForJson.extract().path("data.runs.1.firstView[\"lighthouse.SEO\"]"))
				.map(Object::toString).orElse("NULL");

		pwaScore = Optional.ofNullable(responseForJson.extract().path("data.runs.1.firstView[\"lighthouse.PWA\"]"))
				.map(Object::toString).orElse("NULL");

        try (InputStream myxls = new FileInputStream(new File(System.getProperty("user.dir") + "\\Parameters\\" + fileName));
             Workbook workbook = new XSSFWorkbook(myxls);
             FileOutputStream outFile = new FileOutputStream(new File(System.getProperty("user.dir") + "\\Parameters\\" + fileName))) {

            Sheet sheet = workbook.getSheetAt(sheetNum);
            int rowCount = sheet.getLastRowNum();
            Row row = sheet.createRow(++rowCount);
            int columnCount = 0;

            row.createCell(columnCount++).setCellValue(Helper.getCurrentDate("MM.dd.YY"));
            row.createCell(columnCount++).setCellValue(fcpTime);
            row.createCell(columnCount++).setCellValue(speedIndex);
            row.createCell(columnCount++).setCellValue(lcpTime);
            row.createCell(columnCount++).setCellValue(interactiveTime);
            row.createCell(columnCount++).setCellValue(totalBlockingTime);
            row.createCell(columnCount++).setCellValue(cumulativeLayoutShift);
            
            setScore(row.createCell(columnCount++), performanceScore);
            setScore(row.createCell(columnCount++), accessibilityScore);
            setScore(row.createCell(columnCount++), bestPracticeScore);
            setScore(row.createCell(columnCount++), seoScore);
            setScore(row.createCell(columnCount++), pwaScore);
            
            //row.createCell(columnCount++).setCellValue((int) (Float.parseFloat(accessibilityScore) * 100));

            row.createCell(columnCount++).setCellValue(platform);
            row.createCell(columnCount).setCellValue(userUrl);

            workbook.write(outFile);
            testConfig.logComment("File updated!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
     // Write to InfluxDB
        try (InfluxDBClient influxDBClient = InfluxDBClientFactory.create(INFLUX_HOST, TOKEN.toCharArray(), ORG, BUCKET)) {

            Point point = Point.measurement("web_performance")
                    .addTag("url", url)
                    .addField("fcpTime", fcpTime)
                    .addField("speedIndex", Double.parseDouble(speedIndex.replaceAll("[^0-9.]", "")))
                    .addField("lcpTime", lcpTime)
                    .addField("interactiveTime", interactiveTime)
                    .addField("totalBlockingTime", totalBlockingTime)
                    .addField("cumulativeLayoutShift", cumulativeLayoutShift)
                    .addField("performanceScore", Double.parseDouble(performanceScore) * 100)
                    .addField("accessibilityScore", Double.parseDouble(accessibilityScore) * 100)
                    .addField("bestPracticeScore", Double.parseDouble(bestPracticeScore) * 100)
                    .addField("seoScore", Double.parseDouble(seoScore) * 100)
                    .addField("pwaScore", Double.parseDouble(pwaScore) * 100)
                    .time(Instant.now(), WritePrecision.MS); // <--- important: add timestamp

            try (WriteApi writeApi = influxDBClient.getWriteApi()) {
                writeApi.writePoint(point);
                System.out.printf("✅ Wrote to InfluxDB for URL: %s | lcpTime: %s | interactiveTime: %s | performanceScore: %s\n",
                		url, fcpTime, speedIndex, performanceScore);
            }
        }
    }

	private void setScore(Cell cell, String scoreStr) {
		if (scoreStr.equals("NULL")) {
			cell.setCellValue("NULL");
		} else {
			cell.setCellValue((int) (Float.parseFloat(scoreStr) * 100));
		}
	}
    
    public void readValuesAndExtractDifference(PageTypeLighthouse pageType) {
        String sheetname = (pageType == PageTypeLighthouse.HomePage) ? "HomePage" : "SearchPage";
        String headingLabel = (pageType == PageTypeLighthouse.HomePage) ? "Home Page" : "Search Page";

        TestDataReader categoryReader = testConfig.getCachedTestDataReaderObject(sheetname);
        int totalRecords = categoryReader.getRecordsNum();

        String filename = "LighthouseStats_" + Helper.getCurrentDate("dd-MM-yyyy") + ".txt";
        try (FileWriter fw = new FileWriter(System.getProperty("user.dir") + "\\Parameters\\" + filename, true)) {
            if (totalRecords >= 3) {
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
                findDifferenceAmongLastRuns(fw, "First Contentful Paint", lastRowFCP, secondLastRowFCP);
                findDifferenceAmongLastRuns(fw, "Largest Contentful Paint", lastRowLCP, secondLastRowLCP);
                findDifferenceAmongLastRuns(fw, "Time to Interactive", lastRowInteractiveTime, secondLastRowInteractiveTime);
                findDifferenceAmongLastRuns(fw, "Speed Index", lastRowSpeedIndex, secondLastRowSpeedIndex);
                findDifferenceAmongLastRuns(fw, "Total Blocking Time", lastRowTBT, secondLastRowTBT);
            } else {
                fw.write(headingLabel + "\nNot having enough records to compare... skipping comparison for " + headingLabel + "\n");
            }
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    private void findDifferenceAmongLastRuns(FileWriter fw, String label, String lastRowData, String secondLastRowData) throws IOException {
        DecimalFormat df = new DecimalFormat("#.##");
        fw.write(label + ": " + lastRowData + " \n");

        float lastRowValue = Float.parseFloat(lastRowData.replace(",", "").replaceAll("[^0-9.]", "").trim());
        float secondLastRowValue = Float.parseFloat(secondLastRowData.replace(" s", ""));
        float difference = lastRowValue - secondLastRowValue;

        fw.write(label + " - Performance has " + (difference > 0 ? "declined" : "improved") + " by " + df.format(Math.abs(difference)) + " seconds" + " \n");
    }
}