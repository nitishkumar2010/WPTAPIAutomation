package WPTLighthouse;

import org.testng.annotations.Test;

import Utils.Config;
import Utils.TestBase;
import WPTLighthouse.APIHelperLighthouse.PageTypeLighthouse;

public class RunWPTLighthouseHomePageDesktop extends TestBase {

	@Test(timeOut = DEFAULT_TEST_TIMEOUT, dataProvider = "GetTestConfig", description = "Run Home Page Performance Test", groups = "1")
	public void runHomePagePerformanceTest(Config testConfig) {

		String fileName = testConfig.getRunTimeProperty("FileName");
		String url = testConfig.getRunTimeProperty("HomePage");
		int mobileRun = 0;
		
		APIHelperLighthouse apiHelper = new APIHelperLighthouse(testConfig);
		apiHelper.submitAPIAndGetResponse(url, PageTypeLighthouse.HomePage, fileName, mobileRun);
		//apiHelper.readValuesAndExtractDifference(PageTypeLighthouse.HomePage);
	}
	
}
