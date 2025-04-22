package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionId;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.asserts.SoftAssert;

import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import io.appium.java_client.AppiumDriver;
import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidLauncher;
import ru.yandex.qatools.allure.annotations.Attachment;

/**
 * Config class to initialize the value before each test case run
 * 
 * @author nikumar
 *
 */

public class Config {
	// parameters that can be overridden through command line and are same for
	// all executing tests
	public static String BrowserName;
	public static String Environment;
	public static String EmailId;
	public static String AreaName;
	public static String MobileUAFlag;
	public static String ResultsDir;
	public static String PlatformName;
	public static String RemoteAddress;
	public static String ProjectName;
	public static String BrowserVersion;
	public static String BuildId;
	public String aggregatorId = null;
	public AppiumDriver appiumDriver;
	public Connection connection = null;
	public String customerId = null;
	public Connection DBConnection = null;
	public boolean debugMode = false;
	public static String RunType;
	public static String TestRailRun;
	public static String Run_id;
	public static String fileSeparator = File.separator;

	// parameters different for every test
	public WebDriver driver;
	public String downloadPath = null;
	public boolean enableScreenshot = true;
	public boolean endExecutionOnfailure = false;
	public boolean isMobile = false;

	public boolean logToStandardOut = true;
	public String merchantId = null;
	public String NODESERVERKILL = "taskkill /f /im node.exe";

	// stores the run time properties (different for every test)
	Properties runtimeProperties;

	public SelendroidCapabilities selCap;
	public SelendroidLauncher selLaunch;

	public SoftAssert softAssert;
	public static HashMap<String, TestDataReader> testDataReaderHashMap = new HashMap<String, TestDataReader>();
	public static HashMap<Integer, HashMap<String, String>> genericErrors = new HashMap<Integer, HashMap<String, String>>();
	TestDataReader testDataReaderObj;
	public boolean remoteExecution;
	String testEndTime;

	public String testLog;
	public ExtentTest logger;

	public Method testMethod;
	String testName;

	boolean testResult;

	// package fields
	String testStartTime;

	public String previousPage = "";
	public SessionId session = null;

	public Config(Method method) {
		try {
			endExecutionOnfailure = true;
			this.testMethod = method;
			this.testResult = true;
			this.connection = null;
			this.testLog = "";
			this.softAssert = new SoftAssert();

			logger = TestBase.extent.startTest(method.getName());

			// Read the Config file
			Properties property = new Properties();

			String path = System.getProperty("user.dir") + fileSeparator + "Parameters" + fileSeparator
					+ "Config.properties";

			if (debugMode)
				logComment("Read the configuration file:-" + path);
			FileInputStream fn = new FileInputStream(path);
			property.load(fn);
			fn.close();

			// override the environment value if passed through ant command line
			if (!(Environment == null || Environment.isEmpty()))
				property.put("Environment", Environment.toLowerCase());

			// override environment if declared in @TestVariables
			TestVariables testannotation = ObjectUtils.firstNonNull(method.getAnnotation(TestVariables.class),
					method.getDeclaringClass().getAnnotation(TestVariables.class));
			if (testannotation != null) {
				String environment = testannotation.environment();
				if (!environment.isEmpty()) {
					logComment("Running on " + environment.toLowerCase() + " environment");
					property.put("Environment", environment.toLowerCase());
				}

				String applicationName = testannotation.applicationName();
				if (!applicationName.isEmpty()) {
					logComment("Loading settings for application " + applicationName);

					path = System.getProperty("user.dir") + fileSeparator + "Parameters" + fileSeparator
							+ applicationName + ".properties";
					logComment("Read the environment file:- " + path);

					fn = new FileInputStream(path);
					property.load(fn);
					fn.close();
				}
			}

			path = System.getProperty("user.dir") + fileSeparator + "Parameters" + fileSeparator
					+ property.get("Environment") + ".properties";
			logComment("Read the environment file:- " + path);

			fn = new FileInputStream(path);
			property.load(fn);
			fn.close();

			this.runtimeProperties = new Properties();
			Enumeration<Object> em = property.keys();
			while (em.hasMoreElements()) {
				String str = (String) em.nextElement();
				putRunTimeProperty(str, (String) property.get(str));
			}

			this.debugMode = (getRunTimeProperty("DebugMode").toLowerCase().equals("true")) ? true : false;
			this.logToStandardOut = (getRunTimeProperty("LogToStandardOut").toLowerCase().equals("true")) ? true
					: false;

			// override run time properties if passed through command line
			if (!(BrowserName == null || BrowserName.isEmpty()))
				putRunTimeProperty("Browser", BrowserName);

			if (!(ProjectName == null || ProjectName.isEmpty()))
				putRunTimeProperty("ProjectName", ProjectName);

			if (!(PlatformName == null || PlatformName.isEmpty()) && !PlatformName.equalsIgnoreCase("Local")
					&& !getRunTimeProperty("PlatformName").equalsIgnoreCase("Android"))
				putRunTimeProperty("PlatformName", PlatformName);

			if (!(RemoteAddress == null || RemoteAddress.isEmpty()) && !RemoteAddress.equalsIgnoreCase("null")) {
				putRunTimeProperty("RemoteAddress", RemoteAddress);
				putRunTimeProperty("RemoteExecution", "true");
			}

			if (!(BuildId == null || BuildId.isEmpty()))
				putRunTimeProperty("BuildId", BuildId);

			if (!(BrowserVersion == null || BrowserVersion.isEmpty()))
				putRunTimeProperty("BrowserVersion", BrowserVersion);

			if (!(RunType == null || RunType.isEmpty()))
				putRunTimeProperty("RunType", RunType);
			else {
				RunType = getRunTimeProperty("RunType");
			}

			if (!(TestRailRun == null || TestRailRun.isEmpty()))
				putRunTimeProperty("TestRailRun", TestRailRun);
			else {
				TestRailRun = getRunTimeProperty("TestRailRun");
			}

			if (!(Run_id == null || Run_id.isEmpty()))
				putRunTimeProperty("Run_id", Run_id);
			else {
				Run_id = getRunTimeProperty("Run_id");
			}

			if (!(EmailId == null || EmailId.isEmpty()))
				putRunTimeProperty("EmailId", EmailId);
			else {
				EmailId = getRunTimeProperty("EmailId");
			}

			if (!(AreaName == null || AreaName.isEmpty()))
				putRunTimeProperty("AreaName", AreaName);
			else {
				AreaName = getRunTimeProperty("AreaName");
			}

			if (!(ResultsDir == null || ResultsDir.isEmpty())) {
				putRunTimeProperty("ResultsDir", ResultsDir);
			} else {
				// Set the full path of results dir
				String resultsDir = System.getProperty("user.dir") + getRunTimeProperty("ResultsDir");
				logComment("Results Directory is:- " + resultsDir);
				putRunTimeProperty("ResultsDir", resultsDir);

			}

			if (!(MobileUAFlag == null || MobileUAFlag.isEmpty()))
				putRunTimeProperty("MobileUAFlag", MobileUAFlag);
			// TODO Uncomment for android web execution
			// if (getRunTimeProperty("MobileUAFlag").equals("true"))
			// {
			// putRunTimeProperty("browser", "android_web");
			// }

			// Set the full path of test data sheet
			String testDataSheet = System.getProperty("user.dir") + getRunTimeProperty("TestDataSheet");
			if (debugMode)
				logComment("Test data sheet is:-" + testDataSheet);
			putRunTimeProperty("TestDataSheet", testDataSheet);

			// Set the full path of checkout page
			if (getRunTimeProperty("checkoutPage") != null) {
				String checkoutPage = System.getProperty("user.dir") + getRunTimeProperty("checkoutPage");
				if (debugMode)
					logComment("Checkout page is:-" + checkoutPage);
				putRunTimeProperty("checkoutPage", checkoutPage);
			}

			if (testannotation != null) {
				String remote = testannotation.remoteExecution();
				if (!remote.isEmpty()) {
					putRunTimeProperty("RemoteExecution", remote);
				}
			}

			endExecutionOnfailure = false;
			remoteExecution = (getRunTimeProperty("RemoteExecution").toLowerCase().equals("true")) ? true : false;
			isMobile = (((getRunTimeProperty("Browser").equals("android_web")
					|| getRunTimeProperty("Browser").equals("android_native"))
					&& getRunTimeProperty("RemoteExecution").equals("true"))
					|| getRunTimeProperty("MobileUAFlag").equals("true"));

			String folderName = testMethod.getName();
			if (remoteExecution) {
				RemoteAddress = getRunTimeProperty("RemoteAddress");
				downloadPath = fileSeparator + fileSeparator + RemoteAddress + fileSeparator + "Downloads"
						+ fileSeparator + folderName;
			} else {
				downloadPath = System.getProperty("user.home") + fileSeparator + "Downloads" + fileSeparator
						+ folderName;
			}

			boolean status = Helper.createFolder(downloadPath);

			if (status) {
				downloadPath = downloadPath + fileSeparator;
			} else {
				System.out.println("Something went Wrong.!! Error in Creating Folder -" + downloadPath
						+ " switching to predefined download Path - " + System.getProperty("user.home") + fileSeparator
						+ "Downloads" + fileSeparator);
				downloadPath = System.getProperty("user.home") + fileSeparator + "Downloads" + fileSeparator;
			}

		} catch (IOException e) {
			logException(e);
		}
	}

	@Attachment(value = "Logs For \"{0}\"", type = "text/html")
	public String attachLogs(String testName) {
		return this.testLog;
	}

	/**
	 * Create TestDataReader object for the given sheet and cache it can be fetched
	 * using - getCachedTestDataReaderObject()
	 * 
	 * @param sheetName
	 */
	private void cacheTestDataReaderObject(String sheetName, String path) {
		if (testDataReaderHashMap.get(path + sheetName) == null) {
			testDataReaderObj = new TestDataReader(this, sheetName, path);
			testDataReaderHashMap.put(path + sheetName, testDataReaderObj);
		}
	}

	public void closeBrowser() {
		logToStandardOut = true;

		Browser.quitBrowser(this);
		driver = null;
	}

	public void closeBrowser(ITestResult result) {
		try {
			Browser.closeBrowser(this);
		} catch (Exception e) {
		}

		try {
			Browser.quitBrowser(this);
		} catch (Exception ex) {
		}

		try {
			driver.switchTo().defaultContent();
			Browser.closeBrowser(this);
		} catch (Exception e) {
		}

		try {
			driver.switchTo().defaultContent();
			Browser.quitBrowser(this);
		} catch (Exception ex) {
		}

		driver = null;
	}

	/**
	 * End Test
	 * 
	 * @param result
	 *            - ITestResult
	 */
	public void endTest(ITestResult result) {
		testEndTime = Helper.getCurrentDateTime("yyyy-MM-dd HH:mm:ss");

		endExecutionOnfailure = false;
		enableScreenshot = false;

		List<String> reporterOutput = Reporter.getOutput(result);
		if (this.testStartTime != null) {
			long minutes = 0;
			long seconds = 0;
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String minuteOrMinutes = " ";
			String secondOrSeconds = "";
			try {
				long timeinMillis = (dateFormat.parse(testEndTime).getTime()
						- dateFormat.parse(this.testStartTime).getTime()) / 1000;
				minutes = timeinMillis / 60;
				seconds = timeinMillis % 60;
				if (minutes > 1)
					minuteOrMinutes = "s ";
				if (seconds > 1)
					secondOrSeconds = "s";
			} catch (Exception e) {
			}

			if (!Helper.listContainsString(reporterOutput,
					"<font color='Blue'><B>Total time taken by Test '" + getTestName() + "' : '"))
				logComment("<font color='Blue'><B>Total time taken by Test '" + getTestName() + "' : '" + minutes
						+ " minute" + minuteOrMinutes + seconds + " second" + secondOrSeconds + "' </B></font>");
		}

		if (!testResult) {
			if (!Helper.listContainsString(reporterOutput,
					"<B>Failure occured in test '" + getTestName() + "' Ended on '"))
				logFail("<B>Failure occured in test '" + getTestName() + "' Ended on '" + testEndTime + "'</B>");
		} else {
			if (!Helper.listContainsString(reporterOutput, "<B>Test Passed '" + getTestName() + "' Ended on '"))
				logPass("<B>Test Passed '" + getTestName() + "' Ended on '" + testEndTime + "'</B>");
		}
	}

	/**
	 * Get the cached TestDataReader Object for the given sheet. If it is not
	 * cached, it will be cached for future use
	 * 
	 * To read datasheet other than TestDataSheet, pass filename and sheet name
	 * separated by dot (i.e filename.sheetname)
	 * 
	 * @param sheetName
	 * @return TestDataReader object or null if object is not in cache
	 */
	public TestDataReader getCachedTestDataReaderObject(String sheetName) {
		String path = getRunTimeProperty("TestDataSheet");
		if (sheetName.contains(".")) {
			path = System.getProperty("user.dir") + getRunTimeProperty(sheetName.split("\\.")[0]);
			sheetName = sheetName.split("\\.")[1];

		}
		return getCachedTestDataReaderObject(sheetName, path);
	}

	/**
	 * Get the cached TestDataReader Object for the given sheet in the excel
	 * specified by path. If it is not cached, it will be cached for future use
	 * 
	 * @param sheetName
	 * @param path
	 *            Path of excel sheet to read
	 * @return TestDataReader object or null if object is not in cache
	 */
	public TestDataReader getCachedTestDataReaderObject(String sheetName, String path) {
		TestDataReader obj = testDataReaderHashMap.get(path + sheetName);
		// Object is not in the cache
		if (obj == null) {
			// cache for future use
			synchronized (Config.class) {
				cacheTestDataReaderObject(sheetName, path);
				obj = testDataReaderHashMap.get(path + sheetName);
			}
		}
		return obj;
	}

	/**
	 * Get the Run Time Property value
	 * 
	 * @param key
	 *            key name whose value is needed
	 * @return value of the specified key
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<JSONObject> getJSONArrayListFromRunTimeProperty(String key) {
		String keyName = key.toLowerCase();
		ArrayList<JSONObject> value;
		try {
			value = (ArrayList<JSONObject>) runtimeProperties.get(keyName);
			if (debugMode)
				logComment("Reading Run-Time key-" + keyName + " value:-'" + value + "'");
		} catch (Exception e) {
			if (debugMode) {
				logComment(e.toString());
				logComment("'" + key + "' not found in Run Time Properties");
			}
			return null;
		}
		return value;
	}

	/**
	 * Get the Run Time Property value
	 * 
	 * @param key
	 *            key name whose value is needed
	 * @return value of the specified key
	 */
	public Object getObjectRunTimeProperty(String key) {
		String keyName = key.toLowerCase();
		Object value = "";
		try {
			value = runtimeProperties.get(keyName);
			if (debugMode)
				logComment("Reading Run-Time key-" + keyName + " value:-'" + value + "'");
		} catch (Exception e) {
			if (debugMode) {
				logComment(e.toString());
				logComment("'" + key + "' not found in Run Time Properties");
			}
			return null;
		}
		return value;
	}

	/**
	 * Refreshes the cache for the given sheet in excel, and gets TestDataReader
	 * Object Also it will be cached for future use
	 * 
	 * @param sheetName
	 * @param path
	 *            Path of excel sheet to read
	 * @return TestDataReader object or null if object is not in cache
	 */
	public TestDataReader getRefreshedTestDataReaderObject(String sheetName, String path) {
		TestDataReader obj = new TestDataReader(this, sheetName, path);
		// cache for future use
		testDataReaderHashMap.put(path + sheetName, obj);
		obj = testDataReaderHashMap.get(path + sheetName);

		return obj;
	}

	/**
	 * Get the Run Time Property value
	 * 
	 * @param key
	 *            key name whose value is needed
	 * @return value of the specified key
	 */
	public String getRunTimeProperty(String key) {
		String keyName = key.toLowerCase();
		String value = "";
		try {
			value = runtimeProperties.get(keyName).toString();
			value = Helper.replaceArgumentsWithRunTimeProperties(this, value);
			if (debugMode)
				logComment("Reading Run-Time key-" + keyName + " value:-'" + value + "'");
		} catch (Exception e) {
			if (debugMode) {
				logComment(e.toString());
				logComment("'" + key + "' not found in Run Time Properties");
			}

			return null;
		}
		return value;
	}

	public String getTestName() {
		return testName;
	}

	public boolean getTestCaseResult() {
		return testResult;
	}

	public void logComment(String message) {
		Log.Comment(message, this);
		logger.log(LogStatus.INFO, message);
	}

	public void logException(Throwable e) {
		testResult = false;
		String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
		Log.Fail(fullStackTrace, this);
	}

	public void logFail(String message) {
		testResult = false;
		Log.Fail(message, this);
		logger.log(LogStatus.FAIL, message);
	}

	public void logFail(String what, float expected, float actual) {
		testResult = false;
		String message = "Expected '" + what + "' was :-'" + expected + "'. But actual is '" + actual + "'";
		Log.Fail(message, this);
		logger.log(LogStatus.FAIL, message);
	}

	public void logFail(String what, double expected, double actual) {
		testResult = false;
		String message = "Expected '" + what + "' was :-'" + expected + "'. But actual is '" + actual + "'";
		Log.Fail(message, this);
		logger.log(LogStatus.FAIL, message);
	}

	public void logFail(String what, int expected, int actual) {
		testResult = false;
		String message = "Expected '" + what + "' was :-'" + expected + "'. But actual is '" + actual + "'";
		Log.Fail(message, this);
		logger.log(LogStatus.FAIL, message);
	}

	public void logFail(String what, String expected, String actual) {
		testResult = false;
		String message = "Expected '" + what + "' was :-'" + expected + "'. But actual is '" + actual + "'";
		Log.Fail(message, this);
		logger.log(LogStatus.FAIL, message);
	}

	public void logFailureException(Throwable e) {
		testResult = false;
		Log.Failfinal(ExceptionUtils.getFullStackTrace(e), this);
		logger.log(LogStatus.FAIL, ExceptionUtils.getFullStackTrace(e));
	}

	public void logPass(String message) {
		Log.Pass(message, this);
		logger.log(LogStatus.PASS, message);
	}

	public void logPass(String what, float actual) {
		String message = "Verified '" + what + "' as :-'" + actual + "'";
		Log.Pass(message, this);
		logger.log(LogStatus.PASS, message);
	}

	public void logPass(String what, double actual) {
		String message = "Verified '" + what + "' as :-'" + actual + "'";
		Log.Pass(message, this);
		logger.log(LogStatus.PASS, message);
	}

	public void logPass(String what, int actual) {
		String message = "Verified '" + what + "' as :-'" + actual + "'";
		Log.Pass(message, this);
		logger.log(LogStatus.PASS, message);
	}

	public void logPass(String what, String actual) {
		String message = "Verified '" + what + "' as :-'" + actual + "'";
		Log.Pass(message, this);
		logger.log(LogStatus.PASS, message);
	}

	public void logWarning(String message) {
		Log.Warning(message, this);
		logger.log(LogStatus.INFO, message);
	}

	public void logWarning(String message, boolean logPageInfo) {
		Log.Warning(message, this, logPageInfo);
		logger.log(LogStatus.INFO, message);
	}

	public void openBrowser() {
		int retryCnt = 3;
		while (this.driver == null && retryCnt > 0) {
			try {
				this.driver = Browser.openBrowser(this);
				printNodeIpAddress(this.session, "browser");

			} catch (Exception e) {
				Log.Warning("Retrying the browser launch:-" + e.getLocalizedMessage(), this);
			}
			if (this.driver == null) {
				retryCnt--;
				if (retryCnt == 0) {
					logFail("Browser could not be opened");
					Assert.assertTrue(false);
				}
				Browser.wait(this, 2);
			}

		}
		endExecutionOnfailure = false;
	}

	/**
	 * Add the given key ArrayListJSONObject pair in the Run Time Properties
	 */
	public void putJSONArrayListInRunTimeProperty(String key, ArrayList<JSONObject> table) {
		String keyName = key.toLowerCase();
		runtimeProperties.put(keyName, table);
		if (debugMode)
			logComment("Putting Run-Time key-" + keyName + " value:-'" + table.toString() + "'");
	}

	/**
	 * Add the given key value pair in the Run Time Properties
	 * 
	 * @param key
	 * @param value
	 */
	public void putRunTimeProperty(String key, Object value) {
		String keyName = key.toLowerCase();
		runtimeProperties.put(keyName, value);
		if (debugMode)
			logComment("Putting Run-Time key-" + keyName + " value:-'" + value + "'");
	}

	/**
	 * Add the given key value pair in the Run Time Properties
	 * 
	 * @param key
	 * @param value
	 */
	public void putRunTimeProperty(String key, String value) {
		String keyName = key.toLowerCase();
		runtimeProperties.put(keyName, value);
		if (debugMode) {
			logComment("Putting Run-Time key-" + keyName + " value:-'" + value + "'");
		}
	}

	/**
	 * Removes the given key from the Run Time Properties
	 * 
	 * @param key
	 */
	public void removeRunTimeProperty(String key) {
		String keyName = key.toLowerCase();
		if (debugMode)
			logComment("Removing Run-Time key-" + keyName);
		runtimeProperties.remove(keyName);
	}

	/**
	 * This method will hit the hub and call the api of hub to get the ip address of
	 * machine where our test case is executing
	 * 
	 * @param session
	 *            {@link SessionId}
	 */
	public void printNodeIpAddress(SessionId session, String calledFor) {
		String[] hostAndPort = new String[2];
		String errorMsg = "Failed to acquire remote webdriver node and port info. Root cause: ";
		String hostName = this.getRunTimeProperty("RemoteAddress");
		int port = 4444;
		if (session == null) {
			// this.logComment("Session ID not found: It seems like this execution is Local
			// execution");
			return;
		} else {
			try {
				HttpHost host = new HttpHost(hostName, port);
				HttpClient client = HttpClientBuilder.create().build();
				// DefaultHttpClient client = new DefaultHttpClient();
				URL sessionURL = new URL(
						"http://" + hostName + ":" + port + "/grid/api/testsession?session=" + session);
				BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST",
						sessionURL.toExternalForm());
				org.apache.http.HttpResponse response = client.execute(host, r);
				JSONObject myjsonobject = extractObject(response);
				String url = Helper.getAttributeValueFromJson(this, myjsonobject, "proxyId");
				URL myURL = new URL(url);

				if ((myURL.getHost() != null) && (myURL.getPort() != -1)) {
					hostAndPort[0] = myURL.getHost();
					if (calledFor.equalsIgnoreCase("browser"))
						this.logComment(
								"<font color='Blue'><B>Test Case Executing at :</B> " + hostAndPort[0] + "</font>");
					else if (calledFor.equals("mobile")) {
						sessionURL = new URL("http://" + hostName + ":" + port + "/grid/api/proxy?id=" + url);
						r = new BasicHttpEntityEnclosingRequest("POST", sessionURL.toExternalForm());
						response = client.execute(host, r);
						myjsonobject = extractObject(response);

						String mobileName = Helper.getAttributeValueFromJson(this, myjsonobject, "mobileName");
						String deviceId = Helper.getAttributeValueFromJson(this, myjsonobject, "deviceId");
						this.logComment("<font color='Blue'><B>Test Case Executing at :</B> " + hostAndPort[0]
								+ " <B>on Mobile : </B> '" + mobileName + "' <B>with deviceId :</B> " + deviceId
								+ "</font>");
						putRunTimeProperty("deviceID", deviceId);
						putRunTimeProperty("mobileName", mobileName);
					}
					hostAndPort[1] = Integer.toString(myURL.getPort());
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(errorMsg, e);
			}
		}
	}

	/**
	 * This method will parse the json response and extract machine ip from json
	 * which is returned after calling api
	 * 
	 * @param resp
	 *            {@link HttpResponse}
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private static JSONObject extractObject(HttpResponse resp) throws IOException, JSONException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		StringBuffer s = new StringBuffer();
		String line;
		while ((line = rd.readLine()) != null) {
			s.append(line);
		}
		rd.close();
		JSONObject objToReturn = new JSONObject(s.toString());
		return objToReturn;
	}
}
