package Utils;

import java.io.File;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.testng.Assert;
import org.testng.Reporter;

import ru.yandex.qatools.allure.annotations.Step;

class Log
{
	
	private static Boolean escapeOutput = false;
	
	public static void Comment(String message, Config testConfig)
	{
		if (testConfig.logToStandardOut)
			logToStandard(message);
		if (!escapeOutput)
		{
			message = "<font color='Black'>" + message + "</font></br>";
		}
		Reporter.log(message);
		testConfig.testLog = testConfig.testLog.concat(message);
	}
	
	@Step("Fail:\"{0}\"")
	public static void Fail(String message, Config testConfig)
	{
		PageInfo(testConfig);
		failure(message, testConfig);
	}
	
	@Step("Fail:\"{0}\"")
	public static void FailWithoutPageInfoLogging(String message, Config testConfig)
	{
		failure(message, testConfig);
	}
	
	public static void failure(String message, Config testConfig)
	{
		String tempMessage = message;
		testConfig.softAssert.fail(message);
		if (testConfig.logToStandardOut)
			logToStandard(message);
		if (!escapeOutput)
		{
			message = "<font color='Red'>" + message + "</font></br>";
		}
		Reporter.log(message);
		testConfig.testLog = testConfig.testLog.concat(message);
		
		// Stop the execution if end execution flag is ON
		if (testConfig.endExecutionOnfailure)
			Assert.fail(tempMessage + " --[Ending execution in the middle!]");
	}
	
	public static void Failfinal(String message, Config testConfig)
	{
		try
		{
			PageInfo(testConfig);
		}
		catch (Exception e)
		{
			testConfig.logWarning("Unable to log page info:- " + ExceptionUtils.getStackTrace(e));
			//Commenting it to prevent exception being thrown in OnTestFailure method of test listener. Otherwise logs don't appear
			//throw e;
		}
	}
	
	private static void logToStandard(String message)
	{
		System.out.println(message);
	}
	
	private static void PageInfo(Config testConfig)
	{
		if (testConfig.enableScreenshot)
		{
			if (testConfig.driver != null && testConfig.testMethod != null)
			{
				File dest = Browser.getScreenShotFile(testConfig);
				Browser.takeScreenShoot(testConfig, dest);
			}
		}
	}
	
	@Step("Pass:\"{0}\"")
	public static void Pass(String message, Config testConfig)
	{
		if (testConfig.logToStandardOut)
			logToStandard(message);
		if (!escapeOutput)
		{
			message = "<font color='Green'>" + message + "</font></br>";
		}
		Reporter.log(message);
		testConfig.testLog = testConfig.testLog.concat(message);
	}
	
	@Step("Warning:\"{0}\"")
	public static void Warning(String message, Config testConfig)
	{
		if (testConfig.logToStandardOut)
			logToStandard(message);
		if (!escapeOutput)
		{
			message = "<font color='Orange'>" + message + "</font></br>";
		}
		Reporter.log(message);
		testConfig.testLog = testConfig.testLog.concat(message);
	}
	
	public static void Warning(String message, Config testConfig, boolean logPageInfo)
	{
		if (logPageInfo)
			PageInfo(testConfig);
		
		Warning(message, testConfig);
	}
	
}
