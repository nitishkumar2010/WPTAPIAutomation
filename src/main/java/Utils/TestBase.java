package Utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.asserts.SoftAssert;

import com.relevantcodes.extentreports.ExtentReports;

@Listeners(Utils.TestListener.class)
public class TestBase {
	protected final static long DEFAULT_TEST_TIMEOUT = 900000;
	protected static ThreadLocal<Config[]> threadLocalConfig = new ThreadLocal<Config[]>();
	static List<ITestNGMethod> passedtests = new ArrayList<ITestNGMethod>();
	static List<ITestNGMethod> failedtests = new ArrayList<ITestNGMethod>();
	static List<ITestNGMethod> skippedtests = new ArrayList<ITestNGMethod>();
	static List<ITestNGMethod> totaltests = new ArrayList<ITestNGMethod>();
	public static ExtentReports extent;

	@DataProvider(name = "GetTestConfig")
	public Object[][] GetTestConfig(Method method) {
		Config testConf = new Config(method);
		testConf.testName = method.getDeclaringClass().getName() + "." + method.getName();
		testConf.testStartTime = Helper.getCurrentDateTime("yyyy-MM-dd HH:mm:ss");
		threadLocalConfig.set(new Config[] { testConf });
		return new Object[][] { { testConf } };
	}
	
	@DataProvider(name = "GetTwoBrowserTestConfig")
	public Object[][] GetTwoBrowserTestConfig(Method method)
	{
		Config testConf = new Config(method);
		Config secondaryConfig = new Config(method);

		testConf.testName = secondaryConfig.testName = method.getDeclaringClass().getName() + "." + method.getName();
		testConf.testStartTime = secondaryConfig.testStartTime = Helper.getCurrentDateTime("yyyy-MM-dd HH:mm:ss");

		if (method.isAnnotationPresent(TestVariables.class))
		{
			// Create a object of annotation
			Annotation annotation = method.getAnnotation(TestVariables.class);
			TestVariables annotationObj = (TestVariables) annotation;
			testConf.putRunTimeProperty("newCommandTimeout", annotationObj.newCommandTimeout());
		}

		threadLocalConfig.set(new Config[] { testConf, secondaryConfig });

		return new Object[][] { { testConf, secondaryConfig } };
	}

	@BeforeClass(alwaysRun = true)
	@Parameters({ "browser", "environment", "testngOutputDir", "MobileUAFlag", "PlatformName", "RemoteAddress",
			"BrowserVersion", "RunType", "ProjectName", "BuildId" })
	public void InitializeParameters(@Optional String browser, @Optional String environment,
			@Optional String testngOutputDir, @Optional String MobileUAFlag, @Optional String PlatformName,
			@Optional String RemoteAddress, @Optional String BrowserVersion, @Optional String RunType,
			@Optional String ProjectName, @Optional String BuildId) {
		Config.BrowserName = browser;
		Config.Environment = environment;
		Config.ResultsDir = testngOutputDir;
		Config.MobileUAFlag = MobileUAFlag;
		Config.PlatformName = PlatformName;
		Config.RemoteAddress = RemoteAddress;
		Config.BrowserVersion = BrowserVersion;
		Config.RunType = RunType;
		Config.ProjectName = ProjectName;
		Config.BuildId = BuildId;
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown(ITestResult result) {
		tearDownHelper(result, true);
	}


	/**
	 * Method to initialize the ExtentReport object and setting the system info
	 */
	@BeforeSuite
	public void startSuite() {

		InetAddress IP = null;
		try {
			IP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		if (extent == null) {
			extent = new ExtentReports(
					System.getProperty("user.dir") + "//test-output//ExtentReport//ExtentReport.html", true);
			extent.addSystemInfo("Host Name", IP.getHostName() + "/" + IP.getHostAddress()).addSystemInfo("Environment",
					"Build/Stage");
			extent.addSystemInfo("User Name", "Nitish");

			extent.loadConfig(new File(System.getProperty("user.dir") + "//extent-config.xml"));
		}
	}

	/**
	 * Method to close/flush the extent report object once the complete suite
	 * executes
	 */
	@AfterSuite
	public void endSuite() {
		extent.flush();
		extent.close();
	}

	protected void tearDownHelper(ITestResult result, Boolean clearConfig) {
		String testcaseName = "NullConfig";
		Config[] testConfigs = threadLocalConfig.get();
		if (testConfigs != null)
			for (Config testConf : testConfigs) {
				if (testConf != null) {
					testcaseName = testConf.getTestName();
					testConf.logComment("<------ AfterMethod started for : " + testConf + " " + testConf.getTestName()
							+ " ------>");

					if (testConf.appiumDriver != null) {
					} else {
						testConf.closeBrowser(result);
					}

					testConf.logComment(
							"<------ AfterMethod ended for : " + testConf + " " + testConf.getTestName() + " ------>");

					/**
					 * flag to save testConfig variable for data driven test
					 * cases
					 */
					if (clearConfig) {
						testConf.runtimeProperties.clear();
						testConf = null;
					} else {
						// reset config data so that old failure data is not
						// passed to next test case in data driven test cases
						// reset more data if needed
						// like testResult etc.
						testConf.softAssert = new SoftAssert();
					}

				} else {
					System.out.println("testConfig object not found");
				}

			}

		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date startDate = new Date();
		System.out.println("<B>Test '" + testcaseName + "' Ended on '" + dateFormat.format(startDate) + "'</B>");

	}

	@AfterSuite(alwaysRun = true)
	public void testKillFirefox() throws IOException {
		// According to my local machine -- Need to be changed in case of remote
		// execution and acc to others machine
		/*if (Config.RunType.equalsIgnoreCase("official")) {
			System.out.println("****Closing all Firefox instances****");
			String path = "C:\\AutomationRepo21\\Common\\Prerequisite\\QuitAllFirefox.bat";
			// path = System.getProperty("user.dir") +
			// "\\..\\Common\\Prerequisite\\QuitAllFirefox.bat";

			Runtime.getRuntime().exec("cmd /c start " + path);

			System.out.println("****Closing all Chrome instances****");
			// path =
			// "C:\\AutomationRepo21\\Common\\Prerequisite\\QuitAllChrome.bat";
			// path = System.getProperty("user.dir") +
			// "\\..\\Common\\Prerequisite\\QuitAllChrome.bat";
			Runtime.getRuntime().exec("cmd /c start " + path);

			System.out.println("****Closing all IE instances****");
			// path =
			// "C:\\AutomationRepo21\\Common\\Prerequisite\\QuitAllIE.bat";
			// path = System.getProperty("user.dir") +
			// "\\..\\Common\\Prerequisite\\QuitAllIE.bat";
			Runtime.getRuntime().exec("cmd /c start " + path);
		} else {
			System.out.println("****No browser is closed, running test cases on local machine****");
		}*/

		for (ITestNGMethod iTestNGMethod : passedtests) {
			System.out.println("Passed : " + iTestNGMethod.getMethodName().toString());
		}

		for (ITestNGMethod iTestNGMethod : failedtests) {
			System.out.println("Failed : " + iTestNGMethod.getMethodName().toString());
		}

	}
}
