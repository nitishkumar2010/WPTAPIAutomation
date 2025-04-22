package Utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.selendroid.exceptions.ElementNotVisibleException;

/**
 * Utility class file which consist of wrapped method which needs to 
 * be performed over webelements
 * 
 * @author nikumar
 *
 */

public class Element
{

	/**
	 * Locator technique
	 */
	public static enum How
	{
		className, css, id, linkText, name, partialLinkText, tagName, xPath
	};

	/**
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            WebElement to be checked
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void check(Config testConfig, WebElement element, String description)
	{
		testConfig.logComment("Check '" + description + "'");
		if (!element.isSelected())
		{
			try
			{
				clickWithoutLog(testConfig, element);
				Browser.wait(testConfig, 1);
			}
			catch (StaleElementReferenceException e)
			{
				testConfig.logComment("Stale element reference exception. Trying again...");
				clickWithoutLog(testConfig, element);
			}

		}
	}

	/**
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            WebElement to be cleared
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void clear(Config testConfig, WebElement element, String description)
	{
		testConfig.logComment("Clear data of '" + description + "'");

		element.clear();

	}

	/**
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            WebElement to be clicked
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void click(Config testConfig, WebElement element, String description)
	{
		if (testConfig.getRunTimeProperty("browser").equalsIgnoreCase("android_web"))
		{
			clickThroughJS(testConfig, element, description);
		}
		else
		{
			testConfig.logComment("Click on '" + description + "'");

			try
			{
				JavascriptExecutor jse = (JavascriptExecutor)testConfig.driver;
				jse.executeScript("arguments[0].scrollIntoView(false)", element);
			}
			catch(WebDriverException wde)
			{}

			try
			{
				element.click();
			}
			catch (StaleElementReferenceException e)
			{
				testConfig.logComment("Stale element reference exception. Trying again...");
				element.click();

			}
			catch (UnreachableBrowserException e)
			{
				// testConfig.endExecutionOnfailure = true;
				testConfig.logException(e);
			}
		}

	}

	/**
	 * Clicks on element using JavaScript
	 * 
	 * @param testConfig
	 *            For logging
	 * @param elementToBeClicked
	 *            - Element to be clicked
	 * @param description
	 *            For logging
	 */
	public static void clickThroughJS(Config testConfig, WebElement elementToBeClicked, String description)
	{
		JavascriptExecutor js = (JavascriptExecutor) testConfig.driver;

		js.executeScript("arguments[0].click();", elementToBeClicked);
		testConfig.logComment("Clicked on " + description);

	}

	/**
	 * @param Config
	 *            test config instance for the driver
	 * @param element
	 *            WebElement to be double clicked
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void doubleClick(Config testConfig, WebElement element, String description)
	{
		testConfig.logComment("Double Click on '" + description + "'");
		Actions action = new Actions(testConfig.driver);
		action.doubleClick(element).perform();
	}

	/**
	 * Enters the given 'value'in the specified WebElement
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            WebElement where data needs to be entered
	 * @param value
	 *            value to the entered
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void enterData(Config testConfig, WebElement element, String value, String description)
	{
		if (!value.equalsIgnoreCase("{skip}"))
		{

			// encode the html characters so that they get printed correctly
			String message = StringUtils.replaceEach(value, new String[] { "&", "\"", "<", ">" }, new String[] { "&amp;", "&quot;", "&lt;", "&gt;" });
			testConfig.logComment("Enter the " + description + " as '" + message + "'");
			element.clear();
			element.sendKeys(value);

		}
		else
		{
			testConfig.logComment("Skipped data entry for " + description);
		}
	}

	/**
	 * Enters the given 'value'in the specified WebElement after clicking on it
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            WebElement where data needs to be entered
	 * @param value
	 *            value to the entered
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void enterDataAfterClick(Config testConfig, WebElement element, String value, String description)
	{
		if (!value.equalsIgnoreCase("{skip}"))
		{
			// encode the html characters so that they get printed correctly
			String message = StringUtils.replaceEach(value, new String[] { "&", "\"", "<", ">" }, new String[] { "&amp;", "&quot;", "&lt;", "&gt;" });
			testConfig.logComment("Enter the " + description + " as '" + message + "'");
			clickWithoutLog(testConfig, element);
			element.clear();
			Browser.wait(testConfig, 1);
			element.sendKeys(value);

		}
		else
		{
			testConfig.logComment("Skipped data entry for " + description);
		}
	}

	/**
	 * Enters the given 'value'in the specified WebElement without clear
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            WebElement where data needs to be entered
	 * @param value
	 *            value to the entered
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void enterDataWithoutClear(Config testConfig, WebElement element, String value, String description)
	{
		if (!value.equalsIgnoreCase("{skip}"))
		{
			// encode the html characters so that they get printed correctly
			String message = StringUtils.replaceEach(value, new String[] { "&", "\"", "<", ">" }, new String[] { "&amp;", "&quot;", "&lt;", "&gt;" });
			testConfig.logComment("Enter the " + description + " as '" + message + "'");
			element.sendKeys(value);

		}
		else
		{
			testConfig.logComment("Skipped data entry for " + description);
		}
	}

	/**
	 * Enters the given 'value'in the specified File name WebElement
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            Filename WebElement where data needs to be entered
	 * @param value
	 *            value to the entered
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void enterFileName(Config testConfig, WebElement element, String value, String description)
	{
		if (!value.equalsIgnoreCase("{skip}"))
		{

			testConfig.logComment("Enter the " + description + " as '" + value + "'");
			element.sendKeys(value);

		}
		else
		{
			testConfig.logComment("Skipped file entry for " + description);
		}
	}

	
	/**
	 * Gets all the available string options in the Select Element
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            Select WebElement
	 * @return String list of options
	 */
	public static List<String> getAllOptionsInSelect(Config testConfig, WebElement element)
	{
		testConfig.logComment("Retrieve all the Options present for this specified Select WebElement");
		Select sel = new Select(element);
		List<WebElement> elements = sel.getOptions();
		List<String> options = new ArrayList<String>(elements.size());

		for (WebElement e : elements)
		{
			options.add(e.getText());
		}
		return options;
	}

	/**
	 * Gets all the selected options in the Select Element
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            Select WebElement
	 * @return String list of options
	 */
	public static List<String> getAllSelectedOptions(Config testConfig, WebElement element)
	{
		testConfig.logComment("Retrieve all the Options selected for this specified Select WebElement");
		Select sel = new Select(element);

		List<WebElement> elements = sel.getAllSelectedOptions();
		List<String> options = new ArrayList<String>(elements.size());

		for (WebElement e : elements)
		{
			options.add(e.getText());
		}
		return options;
	}

	/**
	 * Get the first selected option in this select webelement
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            WebElement whose first selected value is to be read
	 * @return
	 */
	public static WebElement getFirstSelectedOption(Config testConfig, WebElement element, String description)
	{
		testConfig.logComment("Get the first selected value for " + description);
		try
		{

			Select sel = new Select(element);
			return sel.getFirstSelectedOption();
		}
		catch (StaleElementReferenceException e)
		{
			testConfig.logComment("Stale element reference exception. Trying again...");
			Select sel = new Select(element);
			return sel.getFirstSelectedOption();
		}

	}

	/**
	 * Returns the How locator used to find the specified Webelement
	 * 
	 * @param element
	 * @return String representation of locator
	 */
	public static String getIdentifier(WebElement element)
	{
		String elementStr = element.toString();
		return "[" + elementStr.substring(elementStr.indexOf("->") + 3);
	}

	public static WebElement getLastElementInCollection(Config testConfig, How how, String strDefinition)
	{
		List<WebElement> webElements = getListOfElements(testConfig, how, strDefinition);
		return webElements.get(webElements.size() - 1);
	}

	/**
	 * Gets the list of WebElements using the specified locator technique on the
	 * passed driver page
	 * 
	 * @param Config
	 *            test config instance for the driver
	 * @param how
	 *            Locator technique to use
	 * @param what
	 *            element to be found with given technique (any arguments in
	 *            this string will be replaced with run time properties)
	 * @return List of WebElements Found
	 */
	public static List<WebElement> getListOfElements(Config testConfig, How how, String what)
	{
		int count = 0;
		testConfig.logComment("Get the List of WebElements with " + how + ":" + what);
		try
		{
			switch (how)
			{
			case className:
				return testConfig.driver.findElements(By.className(what));
			case css:
				return testConfig.driver.findElements(By.cssSelector(what));
			case id:
				return testConfig.driver.findElements(By.id(what));
			case linkText:
				return testConfig.driver.findElements(By.linkText(what));
			case name:
				return testConfig.driver.findElements(By.name(what));
			case partialLinkText:
				return testConfig.driver.findElements(By.partialLinkText(what));
			case tagName:
				return testConfig.driver.findElements(By.tagName(what));
			case xPath:
				return testConfig.driver.findElements(By.xpath(what));
			default:
				return null;
			}
		}
		catch (StaleElementReferenceException e1)
		{
			testConfig.logComment("Stale element reference exception. Trying again...");
			// retry
			if(count == 0){
				count++;
				return getListOfElements(testConfig, how, what);
			} else {
				return null;
			}
			
		}
		catch (Exception e)
		{
			testConfig.logWarning("Could not find the list of the elements on page");
			return null;
		}
	}

	/**
	 * Getting out of frame
	 */
	public static void getOutOfFrame(Config testConfig)
	{
		testConfig.driver.switchTo().defaultContent();
	}

	/**
	 * Gets the WebElement using the specified locator technique on the passed
	 * driver page
	 * 
	 * @param Config
	 *            test config instance for the driver
	 * @param how
	 *            Locator technique to use
	 * @param what
	 *            element to be found with given technique (any arguments in
	 *            this string will be replaced with run time properties)
	 * @return found WebElement
	 */
	public static WebElement getPageElement(Config testConfig, How how, String what)
	{
		int count = 0;
		if(!(testConfig.getRunTimeProperty("disableGetPageElementLogs")!=null && testConfig.getRunTimeProperty("disableGetPageElementLogs").equalsIgnoreCase("true")))
		{
			testConfig.logComment("Get the WebElement with " + how + ":" + what);
		}

		what = Helper.replaceArgumentsWithRunTimeProperties(testConfig, what);

		try
		{
			switch (how)
			{
			case className:
				return testConfig.driver.findElement(By.className(what));
			case css:
				return testConfig.driver.findElement(By.cssSelector(what));
			case id:
				return testConfig.driver.findElement(By.id(what));
			case linkText:
				return testConfig.driver.findElement(By.linkText(what));
			case name:
				return testConfig.driver.findElement(By.name(what));
			case partialLinkText:
				return testConfig.driver.findElement(By.partialLinkText(what));
			case tagName:
				return testConfig.driver.findElement(By.tagName(what));
			case xPath:
				return testConfig.driver.findElement(By.xpath(what));
			default:
				return null;
			}
		}
		catch (StaleElementReferenceException e1)
		{
			testConfig.logComment("Stale element reference exception. Trying again...");
			// retry
			Browser.wait(testConfig, 3);
			testConfig.logComment("Retrying getting element" + how + ":" + what);
			if(count == 0){
				count++;
				return getPageElement(testConfig, how, what);
			} else {
				return null;
			}
		}
		catch (NoSuchElementException e)
		{
			testConfig.logWarning("Could not find the element on page", true);
			return null;
		}

	}

	/**
	 * @param testConfig
	 *            Config instance used for logging
	 * @param how
	 *            locator strategy to find element
	 * @param what
	 *            element locator
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 * @return
	 */
	public static String getText(Config testConfig, How how, String what, String description)
	{
		testConfig.logComment("Get text of '" + description + "'");
		String text = null;
		try
		{
			WebElement elm = Element.getPageElement(testConfig, how, what);
			text = Element.getText(testConfig, elm, description);
		}
		catch (StaleElementReferenceException e)
		{
			testConfig.logComment("Stale element reference exception. Trying again...");
			WebElement elm = Element.getPageElement(testConfig, how, what);
			text = Element.getText(testConfig, elm, description);

		}
		return text;
	}

	/**
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            WebElement whose text is needed
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static String getText(Config testConfig, WebElement element, String description)
	{
		testConfig.logComment("Get text of '" + description + "'");
		String text = null;
		try
		{
			text = element.getText();
		}
		catch (StaleElementReferenceException e)
		{
			testConfig.logComment("Stale element reference exception. Trying again...");

			text = element.getText();

		}

		return text;
	}

	public static Boolean isElementDeleted(Config testConfig, How how, String what)
	{
		Boolean isDeleted = false;
		testConfig.logComment("Get the WebElement with " + how + ":" + what);
		what = Helper.replaceArgumentsWithRunTimeProperties(testConfig, what);
		WebElement element = null;
		try
		{
			switch (how)
			{
			case className:
				element = testConfig.driver.findElement(By.className(what));
				break;
			case css:
				element = testConfig.driver.findElement(By.cssSelector(what));
				break;
			case id:
				element = testConfig.driver.findElement(By.id(what));
				break;
			case linkText:
				element = testConfig.driver.findElement(By.linkText(what));
				break;
			case name:
				element = testConfig.driver.findElement(By.name(what));
				break;
			case partialLinkText:
				element = testConfig.driver.findElement(By.partialLinkText(what));
				break;
			case tagName:
				element = testConfig.driver.findElement(By.tagName(what));
				break;
			case xPath:
				element = testConfig.driver.findElement(By.xpath(what));
				break;
			default:
				testConfig.logFail("Invalid strategy to locate element");
			}
			if (element == null)
				testConfig.logFail("Failed to find element");
		}
		catch (NoSuchElementException e)
		{
			isDeleted = true;
		}

		return isDeleted;
	}

	public static Boolean IsElementDisplayed(Config testConfig, WebElement element)
	{
		Boolean visible = true;
		if (element == null)
			return false;
		try
		{
			testConfig.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
			visible = element.isDisplayed();
		}
		catch (StaleElementReferenceException e)
		{
			testConfig.logComment("Stale element reference exception. Trying again...");
			testConfig.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
			visible = element.isDisplayed();

		}
		catch (NoSuchElementException e)
		{
			visible = false;
		}
		catch (ElementNotVisibleException e)
		{
			visible = false;
		}

		finally
		{
			Long ObjectWaitTime = Long.parseLong(testConfig.getRunTimeProperty("ObjectWaitTime"));
			testConfig.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ObjectWaitTime));
		}
		return visible;
	}

	public static Boolean IsElementEnabled(Config testConfig, WebElement element)
	{
		Boolean visible = true;
		if (element == null)
			return false;
		try
		{
			testConfig.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
			visible = element.isEnabled();
		}
		catch (StaleElementReferenceException e)
		{
			testConfig.logComment("Stale element reference exception. Trying again...");
			testConfig.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
			visible = element.isDisplayed();

		}
		catch (NoSuchElementException e)
		{
			visible = false;
		}
		catch (ElementNotVisibleException e)
		{
			visible = false;
		}

		finally
		{
			Long ObjectWaitTime = Long.parseLong(testConfig.getRunTimeProperty("ObjectWaitTime"));
			testConfig.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ObjectWaitTime));
		}
		return visible;
	}

	/**
	 * Presses the given Key in the specified WebElement
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            Filename WebElement where data needs to be entered
	 * @param Key
	 *            key to the entered
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void KeyPress(Config testConfig, WebElement element, Keys key, String description)
	{
		testConfig.logComment("Press the key '" + key.toString() + "' on " + description + "");
		element.sendKeys(key);

	}

	/**
	 * Method used to scroll up and down horizontally in browser
	 * 
	 * @param testConfig
	 * @param from
	 * @param to
	 */
	public static void pageScroll(Config testConfig, String from, String to)
	{
		JavascriptExecutor js = (JavascriptExecutor) testConfig.driver;
		js.executeScript("window.scrollBy(" + from + "," + to + ")");
	}


	/**
	 * Selects the given 'value' attribute for the specified WebElement
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            WebElement to select
	 * @param value
	 *            value to the selected
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void selectValue(Config testConfig, WebElement element, String value, String description)
	{
		if (!value.equalsIgnoreCase("{skip}"))
		{
			testConfig.logComment("Select the " + description + " dropdown value '" + value + "'");

			Select sel = new Select(element);
			sel.selectByValue(value);

		}
		else
		{
			testConfig.logComment("Skipped value selection for " + description);
		}
	}

	/**
	 * Selects the given visible text 'value' for the specified WebElement
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            WebElement to select
	 * @param value
	 *            visible text value to the selected
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void selectVisibleText(Config testConfig, WebElement element, String value, String description)
	{
		if (!value.equalsIgnoreCase("{skip}"))
		{
			testConfig.logComment("Select the " + description + " dropdown text '" + value + "'");

			Select sel = new Select(element);
			sel.selectByVisibleText(value);

			try
			{
				sel = new Select(element);
				element.click();
				sel.selectByVisibleText(value);
			}
			catch(Exception e){}
		}
		else
		{
			testConfig.logComment("Skipped text selection for " + description);
		}
	}

	/**
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            WebElement to be submitted
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void submit(Config testConfig, WebElement element, String description)
	{
		testConfig.logComment("Submit '" + description + "'");
		element.submit();

	}

	/**
	 * Verifies if element is absent on the page
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            element to be verified
	 * @param description
	 *            description logical name of specified WebElement, used for
	 *            Logging purposes in report
	 */
	public static void verifyElementNotPresent(Config testConfig, WebElement element, String description)
	{
		try
		{
			if (!IsElementDisplayed(testConfig, element))
			{
				testConfig.logPass("Verified the absence of element '" + description + "' on the page");
			}

			else
			{
				testConfig.logFail("Element '" + description + "' is present on the page");
			}
		}
		catch (StaleElementReferenceException e)
		{
			testConfig.logComment("Stale element reference exception. Trying again...");
			if (!IsElementDisplayed(testConfig, element))
			{
				testConfig.logPass("Verified the absence of element '" + description + "' on the page");
			}

			else
			{
				testConfig.logFail("Element '" + description + "' is present on the page");
			}
		}
	}

	/**
	 * Verifies if element is present on the page
	 * 
	 * @param testConfig
	 *            Config instance used for logging
	 * @param element
	 *            element to be verified
	 * @param description
	 *            description logical name of specified WebElement, used for
	 *            Logging purposes in report
	 */
	public static void verifyElementPresent(Config testConfig, WebElement element, String description)
	{
		if (element.isDisplayed())
		{
			testConfig.logPass("Verified the presence of element '" + description + "' on the page");
		}
		else
		{
			testConfig.logFail("Element '" + description + "' is not present on the page");
		}

	}

	
	public static void waitForElementDisplay(Config testConfig, WebElement element)
	{

		testConfig.logComment("Waiting for element to become Visible : " + element.toString());
		try
		{
			for (int i = 1; i <= 50; i++)
			{
				if (IsElementDisplayed(testConfig, element))
					break;
				else
				{
					if (i == 50)
					{
						testConfig.logComment("element : " + element.toString().split("->")[1] + " not found on the page : " + testConfig.driver.getTitle());
					}
				}
			}
		}
		catch (NoSuchElementException e)
		{
			testConfig.logFail("Element is not present on page");
		}
		catch(Exception e)
		{
			testConfig.logFail("Element is not present on page");
		}

	}

	/**
	 * waits for element to disappear
	 */
	public static void waitForElementToDisappear(Config testConfig, WebElement elementName)
	{
		try
		{

			for (int i = 1; i <= 50; i++)
			{
				if (!(IsElementDisplayed(testConfig, elementName)))
					break;
				else
				{
					if (i == 50)
					{
						testConfig.logComment("element : " + elementName.toString().split("->")[1] + " is visible on the page : " + testConfig.driver.getTitle());
					}
				}
			}

		}
		catch (NoSuchElementException e)
		{
			testConfig.logComment("element is not present on page");
		}

	}

	/**
	 * Wait for element to be stale on the page
	 * 
	 * @param Config
	 *            test config instance for the driver instance on which element
	 *            is to be searched
	 * @param element
	 *            element to be searched
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void waitForStaleness(Config testConfig, WebElement element, String description)
	{
		testConfig.logComment("Wait for element '" + description + "' to be stable on the page.");
		Long ObjectWaitTime = Long.parseLong(testConfig.getRunTimeProperty("ObjectWaitTime"));
		WebDriverWait wait = new WebDriverWait(testConfig.driver, Duration.ofSeconds(ObjectWaitTime));
		try
		{
			wait.until(ExpectedConditions.stalenessOf(element));
		}
		catch (org.openqa.selenium.TimeoutException tm)
		{
			throw new TimeoutException("Waited for element " + description + " to get stale for " + ObjectWaitTime + " seconds");
		}
	}

	/**
	 * Wait for element to be visible on the page
	 * 
	 * @param Config
	 *            test config instance for the driver instance on which element
	 *            is to be searched
	 * @param element
	 *            element to be searched
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 * @param timeInSeconds
	 *            Polling time
	 */
	public static void waitForVisibility(Config testConfig, WebElement element, int timeInSeconds, String description)
	{
		testConfig.logComment("Wait for element '" + description + "' to be visible on the page.");
		WebDriverWait wait = new WebDriverWait(testConfig.driver, Duration.ofSeconds(timeInSeconds));
		try
		{
			wait.until(ExpectedConditions.visibilityOf(element));
		}
		catch (org.openqa.selenium.TimeoutException tm)
		{
			throw new TimeoutException(description + " not found after waiting for " + timeInSeconds + " seconds");
		}
	}

	/**
	 * Wait for element to be visible on the page
	 * 
	 * @param Config
	 *            test config instance for the driver instance on which element
	 *            is to be searched
	 * @param element
	 *            element to be searched
	 * @param description
	 *            logical name of specified WebElement, used for Logging
	 *            purposes in report
	 */
	public static void waitForVisibility(Config testConfig, WebElement element, String description)
	{
		testConfig.logComment("Wait for element '" + description + "' to be visible on the page.");
		Long ObjectWaitTime = Long.parseLong(testConfig.getRunTimeProperty("ObjectWaitTime"));
		WebDriverWait wait = new WebDriverWait(testConfig.driver, Duration.ofSeconds(ObjectWaitTime));
		try
		{
			wait.until(ExpectedConditions.visibilityOf(element));
		}
		catch (TimeoutException tm)
		{
			throw new TimeoutException(description + " not found after waiting for " + ObjectWaitTime + " seconds");
		}
	}

	/**
	 * Waits for text to be present in value attribute of specified element
	 * 
	 * @param testConfig
	 * @param element
	 * @param textToBePresentInValueAttribiute
	 * @param description
	 */
	public static void waitTillElementHasValue(Config testConfig, WebElement element, String textToBePresentInValueAttribiute, String description)
	{
		testConfig.logComment("Wait for element '" + description + "' to have :-" + textToBePresentInValueAttribiute + " in value attribute");
		Long ObjectWaitTime = Long.parseLong(testConfig.getRunTimeProperty("ObjectWaitTime"));

		WebDriverWait wait = new WebDriverWait(testConfig.driver, Duration.ofSeconds(ObjectWaitTime));
		try
		{
			wait.until(ExpectedConditions.textToBePresentInElementValue(element, textToBePresentInValueAttribiute));
		}
		catch (TimeoutException tm)
		{
			throw new TimeoutException("Waited for text:'" + textToBePresentInValueAttribiute + "' to be present as value in element:" + description + " for " + ObjectWaitTime + " seconds");
		}
	}

	public static void pressEnter(Config testConfig)
	{
		Actions action = new Actions(testConfig.driver);
		action.sendKeys(Keys.ENTER).perform();
	}

	/**
	 * Get element within another element
	 * @param testConfig
	 * @param element - Element in which another element need to search
	 * @param how - How to search element
	 * @param what - What properties need to search
	 * @return WebElement
	 */
	public static WebElement getElementWithinAnotherElement(Config testConfig, WebElement element, How how, String what)
	{
		int count = 0;
		what = Helper.replaceArgumentsWithRunTimeProperties(testConfig, what);

		try
		{
			switch (how)
			{
			case className:
				return element.findElement(By.className(what));
			case css:
				return element.findElement(By.cssSelector(what));
			case id:
				return element.findElement(By.id(what));
			case linkText:
				return element.findElement(By.linkText(what));
			case name:
				return element.findElement(By.name(what));
			case partialLinkText:
				return element.findElement(By.partialLinkText(what));
			case tagName:
				return element.findElement(By.tagName(what));
			case xPath:
				return element.findElement(By.xpath(what));
			default:
				return null;
			}
		}
		catch (StaleElementReferenceException e1)
		{
			testConfig.logComment("Stale element reference exception. Trying again...");
			Browser.wait(testConfig, 3);
			testConfig.logComment("Retrying getting element" + how + ":" + what);
			if (count == 0) {
				count++;
				return getPageElement(testConfig, how, what);
			} else {
				return null;
			}
		}
		catch (NoSuchElementException e)
		{
			testConfig.logWarning("Could not find the element on page", true);
			return null;
		}
	}

	/**
	 * Click without logging
	 * @param testConfig
	 * @param element
	 */
	private static void clickWithoutLog(Config testConfig, WebElement element)
	{
		try
		{
			JavascriptExecutor jse = (JavascriptExecutor)testConfig.driver;
			jse.executeScript("arguments[0].scrollIntoView(false)", element);
			element.click();
		}
		catch(WebDriverException wde)
		{
			element.click();
		}
	}

	/**
	 * Get attribute value
	 * @param testConfig
	 * @param element
	 * @param attributeName
	 * @param comment
	 * @return attributeValue
	 */
	public static String getAttribute(Config testConfig, WebElement element, String attributeName, String comment)
	{
		testConfig.logComment("Getting value of attribute '" + attributeName + "' for : " + comment);
		String value = "";
		try
		{
			value = element.getAttribute(attributeName);
		}
		catch(Exception wde)
		{
			testConfig.logComment("Exception occurred in fetching value of attribute '" + attributeName + "' for :" + comment + " : " + wde.getMessage());
		}

		return value;
	}

	/**
	 * Get css value
	 * @param testConfig
	 * @param element
	 * @param css
	 * @param comment
	 * @return cssValue
	 */
	public static String getCSSValue(Config testConfig, WebElement element, String css, String comment)
	{
		testConfig.logComment("Getting value of CSS '" + css + "' for :" + comment);
		String value = "";
		try
		{
			value = element.getCssValue(css);
		}
		catch(Exception wde)
		{
			testConfig.logComment("Exception occurred in fetching value of css '" + css + "' for :" + comment + " : " + wde.getMessage());
		}

		return value;
	}

	
	/**
	 * Execute javascript on given elements
	 * @param testConfig
	 * @param javaScriptToExecute
	 * @param element
	 * @return result
	 */
	public static Object executeJavaScript(Config testConfig, String javaScriptToExecute, Object...element)
	{
		testConfig.logComment("Execute javascript:-" + javaScriptToExecute);
		JavascriptExecutor javaScript = (JavascriptExecutor) testConfig.driver;
		return javaScript.executeScript(javaScriptToExecute, element);
	}
}
