package Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.TestResult;

import com.relevantcodes.extentreports.LogStatus;

public class TestListener implements ITestListener, IInvokedMethodListener {
	@Override
	public void onTestFailure(ITestResult result) {
		// This variable will control the duplicate logging of result & accuracy (in
		// case of dual browser testcase)
		boolean executeOnce = true;

		// Will be called in case of unhandled exception in the test case
		// as well as after afterInvocation method of this class does assertAll of all
		// Log.fail in test case

		Config[] testConfigs = TestBase.threadLocalConfig.get();
		if (testConfigs != null)
			for (Config testConf : testConfigs) {
				if (testConf != null) {
					DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
					Date startDate = new Date();
					testConf.logComment("***************EXECUTION OF TESTCASE ENDS HERE at : "
							+ dateFormat.format(startDate) + " ***************");
					testConf.testResult = false;
					testConf.logToStandardOut = true;

					Reporter.setCurrentTestResult(result);
					// Log the unhandled exception thrown by the test cases. This
					// screenshot will appear in Log output, and not in inline
					// testcase logs
					if (result.getThrowable() != null && executeOnce) {
						executeOnce = false;

						String exceptionMessage = result.getThrowable().getMessage();
						if (exceptionMessage == null) {
							testConf.logComment("Unable to get the Failure Reason of testcase:" + testConf.testName);
						}

						if (exceptionMessage == null
								|| !exceptionMessage.equalsIgnoreCase("Ending execution in the middle!")) {
							testConf.logFailureException(result.getThrowable());
						}
					}

					testConf.logger.log(LogStatus.FAIL, "Test Case Failed ---> " + result.getName());
					TestBase.extent.endTest(testConf.logger);

					// else nothing to log, as test case has caught the exception if any
					testConf.endTest(result);
					testConf.attachLogs(testConf.getTestName());
				} else {
					System.out.println("testConfig object not found in onTestFailure");
				}
			}

		TestBase.failedtests.add(result.getMethod());
		TestBase.totaltests.add(result.getMethod());

	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		// Run this after running a test case which failed with soft asserts (Log.Fail)
		// i.e. status as success,
		// to do assertAll, and mark the test case as fail
		if (method.isTestMethod() && testResult.getStatus() == TestResult.SUCCESS) {
			String errorMessage = "";
			Config[] testConfigs = TestBase.threadLocalConfig.get();
			if (testConfigs != null) {
				for (Config testConf : testConfigs) {
					if (testConf != null) {
						try {
							testConf.softAssert.assertAll();
						} catch (AssertionError e) {
							errorMessage = errorMessage + e.getMessage();
						}
					}
				}
			}
			if (errorMessage != "") {
				testResult.setStatus(TestResult.FAILURE);
				testResult.setThrowable(new AssertionError(errorMessage));
				System.out.println("<------ Exiting afterInvocation with errorMessage = " + errorMessage + "------>");
			}
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		Config[] testConfigs = TestBase.threadLocalConfig.get();
		if (testConfigs != null)
			for (Config testConf : testConfigs) {
				if (testConf != null) {
					String message = "Test case skipped " + testConf.getTestName();

					System.out.println(message);
					message = "<font color='Orange'>" + message + "</font></br>";
					Reporter.log(message);

					testConf.logger.log(LogStatus.SKIP, message);
					TestBase.extent.endTest(testConf.logger);
				} else {
					System.out.println("testConfig object not found in onTestSkipped");
				}
			}
		TestBase.skippedtests.add(result.getMethod());
		TestBase.totaltests.add(result.getMethod());
		String className = result.getTestClass().getRealClass().getSimpleName();

		System.out.println("--------------" + result.getMethod() + "-------------" + className);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		Config[] testConfigs = TestBase.threadLocalConfig.get();
		if (testConfigs != null)
			for (Config testConf : testConfigs) {
				if (testConf != null) {
					testConf.endTest(result);
					testConf.attachLogs(testConf.getTestName());
					testConf.logger.log(LogStatus.PASS, testConf.getTestName());
					TestBase.extent.endTest(testConf.logger);
				} else {
					System.out.println("testConfig object not found in onTestSuccess");
				}
			}

		TestBase.passedtests.add(result.getMethod());
		TestBase.totaltests.add(result.getMethod());
		System.out.println(result.getMethod());
	}

	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTestStart(ITestResult result) {
		Config[] testConfigs = TestBase.threadLocalConfig.get();
		if (testConfigs != null)
			for (Config testConf : testConfigs) {
				if (testConf != null) {
					Reporter.setCurrentTestResult(result);
					List<String> reporterOutput = Reporter.getOutput(result);
					if (!Helper.listContainsString(reporterOutput,
							"<B>Test '" + testConf.getTestName() + "' Started on '"))
						Log.Pass("<B>Test '" + testConf.getTestName() + "' Started on '" + testConf.testStartTime
								+ "'</B>", testConf);
					if(testConf.appiumDriver != null && testConf.remoteExecution == true)
						testConf.printNodeIpAddress(testConf.appiumDriver.getSessionId(), "mobile");
				} else {
					System.out.println("testConfig object not found in onTestStart");
				}
			}
	}

	@Override
	public void onFinish(ITestContext context) {

	}

	@Override
	public void onStart(ITestContext context) {
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		// TODO Auto-generated method stub
	}

}