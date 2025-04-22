package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.util.RetryAnalyzerCount;

/**
 *  * This class is used to retry failed test case.When any test case fails this
 * analyzer will retry that test case. To increase or decrease count of retry,
 * edit maxCount property of this class. usage: put given attribute in @test
 * annotation. @Test(retryAnalyzer=RetryAnalyzer.class
 * 
 * @author nikumar
 *
 */

public class RetryAnalyzer extends RetryAnalyzerCount
{
	
	@Override
	public boolean retryMethod(ITestResult result)
	{
		int count = 0;
		int trackingCount = 0;
		Properties prop = new Properties();
		String dir = null;
		FileInputStream input = null;
		
		try
		{
			//Reading Property file to get Retry Count.
			dir = new File(System.getProperty("user.dir")).getParent();
			dir = dir + File.separator + "Common" + File.separator + "Common.properties";
			input = new FileInputStream(dir);
			prop.load(input);
		}
		catch (FileNotFoundException e)
		{
			count = 1;
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		count = Integer.parseInt(prop.getProperty("RetryCount"));
		
		if (!result.isSuccess())
		{
			while (count != 0)
			{
				count--;
				trackingCount++;
				result.setStatus(ITestResult.SUCCESS_PERCENTAGE_FAILURE);
				String message = Thread.currentThread().getName() + "Error in '" + result.getName() + "' with status '" + result.getStatus() + "'. Retrying '" + trackingCount + "' times.";
				Reporter.log(message, true);
				return true;
			}
		}
		return false;
		
	}
}
