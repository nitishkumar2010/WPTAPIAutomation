package WPTLighthouse;

import org.testng.annotations.Test;

import Utils.Config;
import Utils.TestBase;
import WPTLighthouse.APIHelperLighthouse.PageTypeLighthouse;

public class RunWPTLighthouseSearchPageDesktop extends TestBase {

	@Test(timeOut = DEFAULT_TEST_TIMEOUT, dataProvider = "GetTestConfig", description = "Run Home Page Performance Test", groups = "1")
	public void runSearchPagePerformanceTest(Config testConfig) {

		String fileName = testConfig.getRunTimeProperty("FileName");
		String url = testConfig.getRunTimeProperty("SearchPage");
		int mobileRun = 0;
		
		APIHelperLighthouse apiHelper = new APIHelperLighthouse(testConfig);
		apiHelper.submitAPIAndGetResponse(url, PageTypeLighthouse.SearchPage, fileName, mobileRun);
		//apiHelper.readValuesAndExtractDifference(PageTypeLighthouse.SearchPage);
	}
	
}
