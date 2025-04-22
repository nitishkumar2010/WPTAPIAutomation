package Utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;

import ru.yandex.qatools.allure.annotations.Attachment;

public class Helper {

	@Attachment(value = "CSV File:\"{2}\"", type = "text/csv")
	public static byte[] attachCsvFile(Config testConfig, String filePath, String fileName) {
		return getByteArray(filePath);
	}

	@Attachment(value = "Excel File:\"{2}\"", type = "application/vnd.ms-excel")
	public static byte[] attachExcelFile(Config testConfig, String filePath, String excelFileName) {
		return getByteArray(filePath);
	}

	@Attachment(value = "HTML File:\"{2}\"", type = "text/html")
	public static byte[] attachHtmlFile(Config testConfig, String filePath, String fileName) {
		return getByteArray(filePath);
	}

	@Attachment(value = "Image:\"{2}\"", type = "img/png")
	public static byte[] attachImage(Config testConfig, String imgPath, String imageName) {
		return getByteArray(imgPath);
	}

	@Attachment(value = "JSON:\"{2}\"", type = "text/json")
	public static byte[] attachJsonFile(Config testConfig, String filePath, String fileName) {
		return getByteArray(filePath);
	}

	@Attachment(value = "Text File:\"{2}\"", type = "text/plain")
	public static byte[] attachTextFile(Config testConfig, String filePath, String fileName) {
		return getByteArray(filePath);
	}

	@Attachment(value = "XML File:\"{2}\"", type = "text/xml")
	public static byte[] attachXmlFile(Config testConfig, String filePath, String fileName) {
		return getByteArray(filePath);
	}

	public static void compareContains(Config testConfig, String what, String expected, String actual) {
		actual = actual.trim();
		if (actual != null)

		{
			if (!actual.contains(expected.trim())) {
				testConfig.logFail(what, expected, actual);
			} else {
				testConfig.logPass(what, actual);
			}
		} else {
			testConfig.logFail(what, expected, actual);
		}
	}

	public static void compareEquals(Config testConfig, String what, float expected, float actual) {
		if (actual != expected) {
			testConfig.logFail(what, expected, actual);
		} else {
			testConfig.logPass(what, actual);
		}
	}

	public static void compareEquals(Config testConfig, String what, double expected, double actual) {
		if (actual != expected) {
			testConfig.logFail(what, expected, actual);
		} else {
			testConfig.logPass(what, actual);
		}
	}

	public static void compareEquals(Config testConfig, String what, int expected, int actual) {
		if (actual != expected) {
			testConfig.logFail(what, expected, actual);
		} else {
			testConfig.logPass(what, actual);
		}
	}

	public static void compareEquals(Config testConfig, String what, String expected, String actual) {
		if (expected == null & actual == null) {
			testConfig.logPass(what, actual);
			return;
		}

		if (actual != null) {
			if (!actual.equals(expected)) {
				testConfig.logFail(what, expected, actual);
			} else {
				testConfig.logPass(what, actual);
			}
		} else {
			testConfig.logFail(what, expected, actual);
		}
	}

	public static void compareTrue(Config testConfig, String what, boolean actual) {
		if (!actual) {
			testConfig.logFail("Failed to verify " + what);
		} else {
			testConfig.logPass("Verified " + what);
		}
	}

	/**
	 * This method is used to compare a value to false. If the value is false, the
	 * test case passes else fails.
	 * 
	 * @param testConfig
	 * @param what
	 * @param actual
	 */

	public static void compareFalse(Config testConfig, String what, boolean actual) {
		if (!actual) {
			testConfig.logPass("Verified " + what);
		} else {
			testConfig.logFail("Failed to verify " + what);
		}
	}

	/**
	 * @param testConfig
	 * @param what
	 * @param expected
	 *            This value must be value having more than 2 digits after decimal
	 * @param actual
	 */
	public static void compareValues(Config testConfig, String what, String expected, String actual) {
		if (expected == null & actual == null) {
			testConfig.logPass(what, actual);
			return;
		}

		if (actual != null) {
			String[] expectedValue = expected.split(".");
			expected = expectedValue[1];
			expected = expected.substring(0, 2);
			String[] actualValue = actual.split(".");
			actual = actualValue[1];
			expected = String.valueOf(expectedValue);
			if (!actual.equals(expected)) {
				testConfig.logFail(what, expected, actual);
			} else {
				testConfig.logPass(what, actual);
			}
		} else {
			testConfig.logFail(what, expected, actual);
		}
	}

	/**
	 * Generate a random Alphabets string of given length
	 * 
	 * @param length
	 *            Length of string to be generated
	 */
	public static String generateRandomAlphabetsString(int length) {
		Random rd = new Random();
		String aphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			sb.append(aphaNumericString.charAt(rd.nextInt(aphaNumericString.length())));
		}

		return sb.toString();
	}

	/**
	 * Generate a random Alpha-Numeric string of given length
	 * 
	 * @param length
	 *            Length of string to be generated
	 */
	public static String generateRandomAlphaNumericString(int length) {
		Random rd = new Random();
		String aphaNumericString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			sb.append(aphaNumericString.charAt(rd.nextInt(aphaNumericString.length())));
		}

		return sb.toString();
	}

	/**
	 * Generate a random Special Character string of given length
	 * 
	 * @param length
	 *            Length of string to be generated
	 */

	public static String generateRandomSpecialCharacterString(int length) {
		Random rd = new Random();
		String specialCharString = "~!@#$%^*()_<>?/{}[]|\";";
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			sb.append(specialCharString.charAt(rd.nextInt(specialCharString.length())));
		}

		return sb.toString();
	}

	/**
	 * Generate a random decimal number
	 *
	 * @param integer
	 *            lower bound value
	 * @param integer
	 *            Upper bound value
	 * @param integer
	 *            decimal points
	 * 
	 * @return an decimal number between that bound upto given decimal points
	 */

	public static String generateRandomDecimalValue(int lowerBound, int upperBound, int decimalPlaces) {
		Random random = new Random();
		double dbl;
		dbl = random.nextDouble() * (upperBound - lowerBound) + lowerBound;
		return String.format("%." + decimalPlaces + "f", dbl);

	}

	/**
	 * Generate a random number of given length
	 * 
	 * @param length
	 *            Length of number to be generated
	 * @return
	 */
	public static long generateRandomNumber(int length) {
		long randomNumber = 1;
		int retryCount = 1;

		// retryCount added for generating specified length's number
		while (retryCount > 0) {
			String strNum = Double.toString(Math.random());
			strNum = strNum.replace(".", "");

			if (strNum.length() > length) {
				strNum = strNum.substring(0, length);
			} else {
				int remainingLength = length - strNum.length() + 1;
				randomNumber = generateRandomNumber(remainingLength);
				strNum = strNum.concat(Long.toString(randomNumber));
			}

			randomNumber = Long.parseLong(strNum);

			if (String.valueOf(randomNumber).length() < length) {
				retryCount++;
			} else {
				retryCount = 0;
			}

		}

		return randomNumber;
	}

	/**
	 * This function generate Random Alphabets String and put it into
	 * runTimeProperty
	 * 
	 * @param testConfig
	 * @param length
	 *            - Size of String
	 * @param variableName
	 *            - Name to be used in runTimeProperty
	 */
	public static void generateRandomStringAndPutRunTime(Config testConfig, int length, String variableName) {
		String var = Helper.generateRandomAlphabetsString(length);
		testConfig.putRunTimeProperty(variableName, var);
	}

	private static byte[] getByteArray(String pathToFile) {
		Path path = Paths.get(pathToFile);
		byte[] data = null;
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static String getCurrentDate(String format) {
		// get current date
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static String getCurrentDateTime(String format) {
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		String dateNow = formatter.format(currentDate.getTime());
		return dateNow;
	}

	/**
	 * Replaces the arguments like {$someArg} present in input string with its value
	 * from RuntimeProperties
	 * 
	 * @param input
	 *            string in which some Argument is present
	 * @return replaced string
	 */
	public static String replaceArgumentsWithRunTimeProperties(Config testConfig, String input) {
		if (input.contains("{$")) {
			int index = input.indexOf("{$");
			input.length();
			input.indexOf("}", index + 2);
			String key = input.substring(index + 2, input.indexOf("}", index + 2));
			String value = testConfig.getRunTimeProperty(key);

			input = input.replace("{$" + key + "}", value);
			return replaceArgumentsWithRunTimeProperties(testConfig, input);
		} else {
			return input;
		}

	}

	/**
	 * Get the roundOff value to desired minimum fraction of digits.
	 * 
	 * @param roundOffValue
	 * @param minimumFractionDigits
	 * @return
	 */
	public static String roundOff(double roundOffValue, int minimumFractionDigits) {

		DecimalFormat df = new DecimalFormat();
		df.setMinimumFractionDigits(minimumFractionDigits);
		df.setRoundingMode(RoundingMode.HALF_UP);
		String strRoundOffValue = df.format(roundOffValue);
		return strRoundOffValue;
	}

	/**
	 * Get the roundOff value to desired maximum fraction of digits.
	 * 
	 * @param roundOffValue
	 * @param maxFractionDigits
	 * @return
	 */
	public static String roundOffToMaxDigits(double roundOffValue, int maxFractionDigits) {

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(maxFractionDigits);
		df.setRoundingMode(RoundingMode.HALF_UP);
		String strRoundOffValue = df.format(roundOffValue).replaceAll(",", "");
		return strRoundOffValue;
	}

	/**
	 * This method truncates/sacles the given number to specified number of decimals
	 * given
	 * 
	 * @param dNumber
	 * @param numberofDecimals
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String truncateDecimal(double dNumber, int numberofDecimals) {

		String trucatedValue = "";
		if (dNumber > 0) {
			BigDecimal number = new BigDecimal(String.valueOf(dNumber)).setScale(numberofDecimals,
					BigDecimal.ROUND_FLOOR);
			trucatedValue = String.valueOf(number);
			return trucatedValue;
		} else {
			BigDecimal number = new BigDecimal(String.valueOf(dNumber)).setScale(numberofDecimals,
					BigDecimal.ROUND_CEILING);
			trucatedValue = String.valueOf(number);
			return trucatedValue;
		}
	}

	/**
	 * This Method is used to create folder at given path
	 * 
	 * @param path
	 * @return
	 */
	public static boolean createFolder(String path) {
		File newdir = new File(path);
		boolean result = false;
		if (!newdir.exists()) {
			System.out.println("Creating Folder: " + path);
			try {
				result = new File(path).mkdir();
			} catch (Exception se) {
				se.printStackTrace();
			}
			if (result)
				System.out.println("Folder " + path + " created");
			else
				System.out.println("Error in Creating Folder: " + path);
		} else {
			System.out.println("Folder: " + path + " already Exist");
			result = true;
		}
		return result;
	}

	/**
	 * compares values in first map with values in second map
	 * 
	 * @param testConfig
	 * @param expected
	 * @param actual
	 */
	public static void compareEquals(Config testConfig, Map<String, String> expected, Map<String, String> actual) {
		for (Map.Entry<String, String> entry : expected.entrySet()) {
			Helper.compareEquals(testConfig, entry.getKey(), entry.getValue(), actual.get(entry.getKey()));
		}
	}

	/**
	 * Get all attribute values from JSON object
	 * 
	 * @param json
	 * @param out
	 * @return Map<String, String>
	 * @throws JSONException
	 */
	public static Map<String, String> getValuesFromJson(JSONObject json, Map<String, String> out) throws JSONException {
		@SuppressWarnings("unchecked")
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			String val = null;
			try {
				JSONObject value = json.getJSONObject(key);
				getValuesFromJson(value, out);
			} catch (Exception e) {
				val = json.getString(key);
				if (val.indexOf("[") == 0 && val.indexOf("]") == val.length() - 1) {
					val = val.substring(1);
					val = val.substring(0, val.length() - 1);
					try {
						JSONObject value = new JSONObject(val);
						getValuesFromJson(value, out);
					} catch (Exception ex) {
					}
				}
			}

			if (val != null && !out.containsKey(key))
				out.put(key, val);
		}
		return out;
	}

	/**
	 * Get specific attribute value from JSON object
	 * 
	 * @param testConfig
	 * @param json
	 * @param attributeName
	 * @return attributeValue
	 */
	public static String getAttributeValueFromJson(Config testConfig, JSONObject json, String attributeName) {
		Map<String, String> out = new HashMap<String, String>();
		try {
			getValuesFromJson(json, out);
		} catch (Exception e) {
			testConfig.logFail(e.getMessage());
		}

		return out.get(attributeName);
	}

	/**
	 * Check List Contains Given String
	 * 
	 * @param list
	 * @param stringToMatch
	 * @return true/false
	 */
	public static boolean listContainsString(List<String> list, String stringToMatch) {
		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
			String tempString = iter.next();
			if (tempString.contains(stringToMatch))
				return true;
		}
		return false;
	}

	/**
	 * Scroll to the bottom of the page to load every card on the page and then
	 * again move to top of the page on the basis of scrollBackToTop value
	 * 
	 * @param testConfig
	 * @param scrollBackToTop
	 */
	public static void scrollOnLazyLoadingFYHWideSearchResultView(Config testConfig, Boolean scrollBackToTop) {
		testConfig.logComment("Scrolling to the end of lazy load page");
		JavascriptExecutor js = (JavascriptExecutor) testConfig.driver;
		Long windowHeight = (Long) js
				.executeScript("return document.getElementsByClassName('sidebar-scroll-container')[0].offsetHeight");
		int numberOfPixelsToDragTheScrollbarDown = 1000, i = 0;
		while (i < windowHeight) {
			js.executeScript("document.getElementsByClassName('sidebar-scroll-container')[0].scrollTo( " + i + ", "
					+ (i + numberOfPixelsToDragTheScrollbarDown) + ")");
			i += numberOfPixelsToDragTheScrollbarDown;
			Browser.waitWithoutLogging(testConfig, 1);
			try {
				windowHeight = (Long) js.executeScript(
						"return document.getElementsByClassName('w-full font-sofia px-4')[0].offsetHeight");
			} catch (Exception e) {
				windowHeight = (Long) js
						.executeScript("return document.getElementsByClassName('search-result')[0].offsetHeight");
			}
		}

		if (scrollBackToTop) {
			Browser.wait(testConfig, 2);
			js.executeScript("window.scrollTo(document.body.scrollTop,0)");
		}
	}

	/**
	 * Scroll to the bottom of the page to load every card on the page and then
	 * again move to top of the page on the basis of scrollBackToTop value
	 * 
	 * @param testConfig
	 * @param scrollBackToTop
	 */
	public static void scrollOnLazyLoadingFYHCommResultView(Config testConfig, Boolean scrollBackToTop) {
		testConfig.logComment("Scrolling to the end of lazy load page");
		JavascriptExecutor js = (JavascriptExecutor) testConfig.driver;
		Long windowHeight = (Long) js
				.executeScript("return document.getElementsByClassName('sidebar-scroll-container')[0].offsetHeight");
		int numberOfPixelsToDragTheScrollbarDown = 200, i = 0;
		while (i < (windowHeight/2)) {
			js.executeScript("document.getElementsByClassName('sidebar-scroll-container')[0].scrollTo( " + i + ", "
					+ (i + numberOfPixelsToDragTheScrollbarDown) + ")");
			i += numberOfPixelsToDragTheScrollbarDown;
			Browser.waitWithoutLogging(testConfig, 2);
			windowHeight = (Long) js
					.executeScript("return document.getElementsByClassName('2xl:min-h-150')[0].offsetHeight");
		}

		if (scrollBackToTop) {
			Browser.wait(testConfig, 2);
			js.executeScript("window.scrollTo(document.body.scrollTop,0)");
		}
	}

	public static void scrollOnContactUsModal(Config testConfig, Boolean scrollBackToTop) {
		testConfig.logComment("Scrolling to the end of lazy load page");
		JavascriptExecutor js = (JavascriptExecutor) testConfig.driver;
		int j = 0;
		Long windowHeight = (Long) js
				.executeScript("return document.getElementsByClassName('border-black h-full w-full px-[35px] "
						+ "md:pr-0 md:pl-[72px] xl:pl-[93px] xl:overflow-y-auto w-full')[0].offsetHeight");
		if (windowHeight < 1) {
			j = 1;
			windowHeight = (Long) js
					.executeScript("return document.getElementsByClassName('border-black h-full w-full px-[35px] "
							+ "md:pr-0 md:pl-[72px] xl:pl-[93px] xl:overflow-y-auto w-full')[1].offsetHeight");
		}

		int numberOfPixelsToDragTheScrollbarDown = 150, i = 0;
		while (i < windowHeight / 2) {
			js.executeScript(
					"document.getElementsByClassName('border-black h-full w-full px-[35px] md:pr-0 md:pl-[72px] xl:pl-[93px] "
							+ "xl:overflow-y-auto text-left')[" + j + "]." + "scrollTo(" + i + ", "
							+ (i + numberOfPixelsToDragTheScrollbarDown) + ")");
			i += numberOfPixelsToDragTheScrollbarDown;
			Browser.waitWithoutLogging(testConfig, 1);
		}

		if (scrollBackToTop) {
			Browser.wait(testConfig, 2);
			js.executeScript(
					"document.getElementsByClassName('border-black h-full w-full px-[35px] md:pr-0 md:pl-[72px]"
							+ " xl:pl-[93px] xl:overflow-y-auto text-left')[" + j + "]" + ".scrollTo("
							+ (i + numberOfPixelsToDragTheScrollbarDown) + ", " + 0 + ")");
		}
	}

	public static void scrollOnContactUsSendAMessageModal(Config testConfig, Boolean scrollBackToTop) {
		testConfig.logComment("Scrolling to the end of lazy load page");
		JavascriptExecutor js = (JavascriptExecutor) testConfig.driver;
		int j = 0;
		Long windowHeight = (Long) js.executeScript(
				"return document.getElementsByClassName('border-black h-full w-full px-[35px] md:pr-0 md:pl-[72px] xl:pl-[93px] xl:overflow-y-auto w-full')[0]"
						+ ".offsetHeight");
		if (windowHeight < 1) {
			j = 1;
			windowHeight = (Long) js.executeScript(
					"return document.getElementsByClassName('border-black h-full w-full px-[35px] md:pr-0 md:pl-[72px] xl:pl-[93px] xl:overflow-y-auto w-full')[1]"
							+ ".offsetHeight");
		}

		if (windowHeight < 1) {
			j = 2;
			windowHeight = (Long) js.executeScript(
					"return document.getElementsByClassName('border-black h-full w-full px-[35px] md:pr-0 md:pl-[72px] xl:pl-[93px] xl:overflow-y-auto w-full')[2]"
							+ ".offsetHeight");
		}

		int numberOfPixelsToDragTheScrollbarDown = 150, i = 0;
		while (i < windowHeight / 2) {
			js.executeScript(
					"document.getElementsByClassName('border-black h-full w-full px-[35px] md:pr-0 md:pl-[72px] xl:pl-[93px] xl:overflow-y-auto w-full')[" + j
							+ "]." + "scrollTo(" + i + ", " + (i + numberOfPixelsToDragTheScrollbarDown) + ")");
			i += numberOfPixelsToDragTheScrollbarDown;
			Browser.waitWithoutLogging(testConfig, 1);
		}

		if (scrollBackToTop) {
			Browser.wait(testConfig, 2);
			js.executeScript(
					"document.getElementsByClassName('border-black h-full w-full px-[35px] md:pr-0 md:pl-[72px] xl:pl-[93px] xl:overflow-y-auto w-full')[" + j
							+ "]" + ".scrollTo(" + (i + numberOfPixelsToDragTheScrollbarDown) + ", " + 0 + ")");
		}
	}

	/**
	 * Scroll to the bottom of the page to load every result on the page and then
	 * again move to top of the page on the basis of scrollBackToTop value
	 * 
	 * @param testConfig
	 * @param scrollBackToTop
	 */
	public static void scrollOnLazyLoadingPage(Config testConfig, Boolean scrollBackToTop) {
		Browser.waitWithoutLogging(testConfig, 20);
		JavascriptExecutor js = (JavascriptExecutor) testConfig.driver;
		Long windowHeight = (Long) js.executeScript("return document.body.scrollHeight");
		int numberOfPixelsToDragTheScrollbarDown = 400, i = 0;
		while (i < windowHeight) {
			js.executeScript("window.scrollTo( " + i + ", " + (i + numberOfPixelsToDragTheScrollbarDown) + ")");
			i += numberOfPixelsToDragTheScrollbarDown;
			Browser.waitWithoutLogging(testConfig, 1);

			if (i > windowHeight) {
				Browser.waitWithoutLogging(testConfig, 4);
				windowHeight = (Long) js.executeScript("return document.body.scrollHeight");
			}
		}

		if (scrollBackToTop) {
			Browser.wait(testConfig, 2);
			js.executeScript("window.scrollTo(document.body.scrollTop,0)");
		}
	}
	
	public static void removeCookies(Config testConfig) {
		testConfig.driver.manage().deleteAllCookies();
	}

}
