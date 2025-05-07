package Utils;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.html5.LocationContext;
import org.openqa.selenium.remote.Augmentable;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import io.github.bonigarcia.wdm.WebDriverManager;
import ru.yandex.qatools.allure.annotations.Attachment;

/**
 * Utility class file for all browser specific actions
 * 
 * @author nikumar
 *
 */

public class Browser {

	// This class overrides the setCompressionQuality() method to workaround
	// a problem in compressing JPEG images using the javax.imageio package.
	public static class MyImageWriteParam extends JPEGImageWriteParam {
		public MyImageWriteParam() {
			super(Locale.getDefault());
		}

		// This method accepts quality levels between 0 (lowest) and 1 (highest)
		// and simply converts
		// it to a range between 0 and 256; this is not a correct conversion
		// algorithm.
		// However, a proper alternative is a lot more complicated.
		// This should do until the bug is fixed.
		@Override
		public void setCompressionQuality(float quality) {
			if (quality < 0.0F || quality > 1.0F) {
				throw new IllegalArgumentException("Quality out-of-bounds!");
			}
			this.compressionQuality = 256 - (quality * 256);
		}
	}

	/**
	 * Brings current window on focus
	 * 
	 * @param testConfig
	 */
	public static void bringToFocus(Config testConfig) {

		String currentWindowHandle = testConfig.driver.getWindowHandle();
		((JavascriptExecutor) testConfig.driver).executeScript("alert('Test')");
		testConfig.driver.switchTo().alert().accept();
		testConfig.driver.switchTo().window(currentWindowHandle);

		testConfig.logComment("Brought current window to focus");
	}

	/**
	 * Refresh browser once
	 */
	public static void browserRefresh(Config testConfig) {
		// testConfig.driver.navigate().refresh();
		executeJavaScript(testConfig, "location.reload();");
		testConfig.logComment("Refreshing the browser...");
	}

	@Attachment(value = "Screenshot", type = "image/png")
	private static byte[] captureScreenshot(Config testConfig) {
		byte[] screenshot = null;

		try {

			if (testConfig.driver.getClass().isAnnotationPresent(Augmentable.class) || testConfig.driver.getClass()
					.getName().startsWith("org.openqa.selenium.remote.RemoteWebDriver$$EnhancerByCGLIB")) {
				WebDriver augumentedDriver = new Augmenter().augment(testConfig.driver);
				screenshot = ((TakesScreenshot) augumentedDriver).getScreenshotAs(OutputType.BYTES);
			} else {
				screenshot = ((TakesScreenshot) testConfig.driver).getScreenshotAs(OutputType.BYTES);
			}

		} catch (UnhandledAlertException alert) {
			testConfig.logWarning(ExceptionUtils.getFullStackTrace(alert));
		} catch (NoSuchWindowException NoSuchWindowExp) {
			testConfig.logWarning("NoSuchWindowException:Screenshot can't be taken. Probably browser is not reachable");
			// test case will end, setting this as null will prevent taking
			// screenshot again in cleanup
			testConfig.driver = null;
		} catch (WebDriverException webdriverExp) {
			testConfig.logWarning("Unable to take screenshot:- " + ExceptionUtils.getFullStackTrace(webdriverExp));
		}
		return screenshot;
	}

	/**
	 * Close the current window, quitting the browser if it's the last window
	 * currently open.
	 * 
	 * @param Config
	 *            test config instance for the browser to be closed
	 */
	public static void closeBrowser(Config testConfig) {
	try {
			if (testConfig.driver != null) {
				testConfig.logComment("Close the browser window with URL:- " + testConfig.driver.getCurrentUrl()
				+ ". And title as :- " + testConfig.driver.getTitle());
				testConfig.driver.close();
			}
		} catch (UnreachableBrowserException e) {
			testConfig.logWarning(ExceptionUtils.getFullStackTrace(e));
		}
	}

	// Reads the jpeg image in infile, compresses the image,
	// and writes it back out to outfile.
	// compressionQuality ranges between 0 and 1,
	// 0-lowest, 1-highest.
	private static void compressJpegFile(File infile, File outfile, float compressionQuality) {
		try {
			// Retrieve jpg image to be compressed
			RenderedImage rendImage = ImageIO.read(infile);

			// Find a jpeg writer
			ImageWriter writer = null;
			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("gif");
			if (iter.hasNext()) {
				writer = iter.next();
			}

			// Prepare output file
			ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
			writer.setOutput(ios);

			// Set the compression quality
			ImageWriteParam iwparam = new MyImageWriteParam();
			iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwparam.setCompressionQuality(compressionQuality);

			// Write the image
			writer.write(new IIOImage(rendImage, null, null));

			// Cleanup
			ios.flush();
			writer.dispose();
			ios.close();
		}

		catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError outOfMemoryError) {
			outOfMemoryError.printStackTrace();
		}
	}

	/**
	 * Delete the cookies of the given browser instance
	 * 
	 * @param Config
	 *            test config instance
	 */
	public static void deleteCookies(Config testConfig) {
		if (testConfig.driver != null) {
			testConfig.logComment("Delete all cookies!!");
			testConfig.driver.manage().deleteAllCookies();
		}
	}

	public static void waitForPageTitleToContain(Config testConfig, String title) {
		testConfig.logComment("Wait for page title to contain '" + title + "'.");
		WebDriverWait wait = new WebDriverWait(testConfig.driver,Duration.ofSeconds(10));
		wait.until(ExpectedConditions.titleContains(title));
	}

	/**
	 * Executes JavaScript in the context of the currently selected frame or window
	 * in the Config driver instance.
	 * 
	 * @param testConfig
	 * @param javaScriptToExecute
	 * @return
	 */
	public static Object executeJavaScript(Config testConfig, String javaScriptToExecute) {
		testConfig.logComment("Execute javascript:-" + javaScriptToExecute);
		JavascriptExecutor javaScript = (JavascriptExecutor) testConfig.driver;
		return javaScript.executeScript(javaScriptToExecute);
	}

	private static String getCallerClassName() {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		for (int i = 1; i < stElements.length; i++) {
			StackTraceElement ste = stElements[i];
			if (!ste.getClassName().equals(Browser.class.getName()) && !ste.getClassName().contains("Helper")
					&& ste.getClassName().indexOf("java.lang.Thread") != 0) {
				return ste.getClassName();
			}
		}
		return null;
	}

	/**
	 * Uses the specified method name to generate a destination file name where
	 * PageHTML can be saved
	 * 
	 * @param Config
	 *            test config instance
	 * @return file using which we can save PageHTML
	 */
	public static File getPageHTMLFile(Config testConfig) {
		File dest = getScreenShotDirectory(testConfig);
		return new File(dest.getPath() + File.separator + getPageHTMLFileName(testConfig.testMethod));
	}

	private static String getPageHTMLFileName(Method testMethod) {
		String nameScreenshot = testMethod.getDeclaringClass().getName() + "." + testMethod.getName();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		return dateFormat.format(date) + "_" + nameScreenshot + ".html";
	}

	private static File getScreenShotDirectory(Config testConfig) {
		File dest = new File(testConfig.getRunTimeProperty("ResultsDir") + File.separator + "html" + File.separator);
		return dest;
	}

	/**
	 * Uses the specified method name to generate a destination file name where
	 * screenshot can be saved
	 * 
	 * @param Config
	 *            test config instance
	 * @return file using which we can call takescreenshot
	 */
	public static File getScreenShotFile(Config testConfig) {
		File dest = getScreenShotDirectory(testConfig);
		return new File(dest.getPath() + File.separator + getScreenshotFileName(testConfig.testMethod));
	}

	private static String getScreenshotFileName(Method testMethod) {
		String nameScreenshot = testMethod.getDeclaringClass().getName() + "." + testMethod.getName();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		return dateFormat.format(date) + "_" + nameScreenshot + ".png";
	}

	/**
	 * To return back to previous page
	 * 
	 * @param testConfig
	 * @param url
	 */
	public static void goBack(Config testConfig) {
		testConfig.logComment("Clicking on back button on browser");
		testConfig.driver.navigate().back();
	}

	public static File lastFileModified(Config testConfig, String dir) {
		File fl = new File(dir);
		File[] files = fl.listFiles();
		Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

		return files[0];
	}

	/**
	 * @param testConfig
	 *            - element of Config
	 * @param path
	 *            - path of the folder where file is present
	 * @param name
	 *            - some text that is present in file name
	 * @return - file name of the last modified file with matching text
	 */
	public static File lastFileModifiedWithDesiredName(Config testConfig, String path, String name) {
		File fl = new File(path);
		File choise = null;
		List<File> arrayOfSortedFiles = new ArrayList<File>();
		long lastMod = Long.MIN_VALUE;
		for (int retry = 0; retry <= 5; retry++) {
			// making a list of files in download folder
			System.out.println("Wait for file to download");
			Browser.wait(testConfig, 5);
			File[] files = fl.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isFile();
				}
			});
			// Matching names of desired file
			for (File file : files) {
				if (file.getName().contains(name))
					arrayOfSortedFiles.add(file);
			}
			if (arrayOfSortedFiles.size() > 0)
				break;
			else
				continue;
		}
		// Finding matching file which has been last modified
		for (File matchingfile : arrayOfSortedFiles) {
			if (matchingfile.lastModified() > lastMod) {
				choise = matchingfile;
				lastMod = matchingfile.lastModified();
			}
		}
		if (choise == null)
			Log.Fail("No File found with name" + name, testConfig);
		else
			System.out.println("The file chosen is as: " + choise.getName());
		return choise;
	}

	/**
	 * Navigate to driver the URL specified
	 * 
	 * @param Config
	 *            test config instance
	 * @param url
	 *            URL to be navigated
	 */
	public static void navigateToURL(Config testConfig, String url) {
		if (testConfig.driver == null) {
			testConfig.openBrowser();
		}
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date startDate = new Date();
		testConfig.logComment("Navigate to web page- '" + url + "' at:- " + dateFormat.format(startDate));
		try {
			testConfig.driver.get(url);
		} catch (UnhandledAlertException ua) {
			testConfig.logWarning("Alert appeared during navigation");
		}
	}

	/**
	 * Opens the new browser instance using the given config
	 * 
	 * @return new browser instance
	 * @throws IOException
	 */
	public static WebDriver openBrowser(Config testConfig) {
		WebDriver driver = null;
		String browser = testConfig.getRunTimeProperty("Browser");

		// Code to handle Local Execution and Jenkins Execution WITHOUT Selenium
		// Grid
		testConfig.logComment("Launching '" + browser + "' browser in local machine");

		switch (browser.toLowerCase()) {
		case "firefox":
			FirefoxProfile firefoxProfile = new FirefoxProfile();
			firefoxProfile = setFireFoxProfile(testConfig, firefoxProfile);

			/*DesiredCapabilities ffCapability = DesiredCapabilities.firefox();
			ffCapability.setCapability("firefox_profile", firefoxProfile);

			FirefoxBinary firefoxBinary = new FirefoxBinary();
			firefoxBinary.setTimeout(java.util.concurrent.TimeUnit.SECONDS.toMillis(90));
			 */
			driver = new FirefoxDriver();
			break;

		case "chrome":
			//System.setProperty("webdriver.chrome.driver",System.getProperty("user.dir") + File.separator + "lib" + File.separator + "chromedriver.exe");
			//System.setProperty("webdriver.chrome.driver", File.separator + "usr" + File.separator + "bin" + File.separator + "chromedriver");
			// System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + File.separator + "lib" + File.separator + "chromedriver.exe");
			// System.setProperty("webdriver.chrome.driver", File.separator + "usr" +
			// File.separator + "bin" + File.separator + "chromedriver");
			
			WebDriverManager.chromedriver().setup();
			HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
			chromePrefs.put("profile.default_content_settings.popups", 0);
			chromePrefs.put("download.default_directory", testConfig.downloadPath);
			chromePrefs.put("profile.default_content_setting_values.geolocation", 1); 

			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.setExperimentalOption("prefs", chromePrefs);

			//chromeOptions.addArguments("--headless");
			//chromeOptions.addArguments("--window-size=1366,768");
			chromeOptions.addArguments("--remote-allow-origins=*");
			
			chromeOptions.addArguments("--start-maximized");
			driver = new ChromeDriver(chromeOptions);

			((LocationContext)driver).setLocation(new Location(30.2672, -97.7431, 0));

			//String username = testConfig.getRunTimeProperty("Username");
			//String password = testConfig.getRunTimeProperty("Password");

			/*
			 * if(!testConfig.getRunTimeProperty("Environment").equals("BRPProd")) {
			 * DevTools dev = ((ChromeDriver) driver).getDevTools(); dev.createSession();
			 * //dev.send(Network.enable(Optional.<Integer>empty(),
			 * Optional.<Integer>empty(), Optional.<Integer>empty())); Map<String, Object>
			 * map = new HashMap<>(); //map.put("Authorization", "Basic " + new String(new
			 * Base64().encode((username + ":" + password).getBytes())));
			 * dev.send(Network.setExtraHTTPHeaders(new Headers(map))); }
			 */
			break;

		default:
			Assert.fail(browser + "- is not supported");
		}

		if (driver != null) {
			Long objectWaitTime = Long.parseLong(testConfig.getRunTimeProperty("ObjectWaitTime"));
		    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(objectWaitTime));
		    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(objectWaitTime * 3));
		    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(objectWaitTime * 3));

			// Deciding browser's size based on mobileWeb or Web
			if (!testConfig.isMobile) {
				// driver.manage().window().setPosition(new Point(0, 0));
				// driver.manage().window().setSize(new Dimension(334, 494));
			}
			{
				driver.manage().window().maximize();
			}
		}

		return driver;
	}

	private static FirefoxProfile setFireFoxProfile(Config testConfig, FirefoxProfile firefoxProfile) {
		if (testConfig.isMobile) {
			firefoxProfile.setPreference("general.useragent.override", testConfig.getRunTimeProperty("MobileUAString"));
		}

		firefoxProfile.setPreference("dom.max_chrome_script_run_time", 0);
		firefoxProfile.setPreference("dom.max_script_run_time", 0);

		firefoxProfile.setPreference("browser.download.folderList", 2);
		firefoxProfile.setPreference("browser.download.dir", testConfig.downloadPath);

		// automatically download excel files
		firefoxProfile.setPreference("browser.helperApps.alwaysAsk.force", false);
		firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/x-msdos-program, application/x-unknown-application-octet-stream, application/vnd.ms-powerpoint, application/excel, application/vnd.ms-publisher, application/x-unknown-message-rfc822, application/vnd.ms-excel, application/msword, application/x-mspublisher, application/x-tar, application/zip, application/x-gzip,application/x-stuffit,application/vnd.ms-works, application/powerpoint, application/rtf, application/postscript, application/x-gtar, video/quicktime, video/x-msvideo, video/mpeg, audio/x-wav, audio/x-midi, audio/x-aiff, text/html, application/octet-stream");
		firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
		firefoxProfile.setPreference("browser.startup.homepage_override.mstone", "ignore");
		firefoxProfile.setPreference("startup.homepage_welcome_url.additional", "about:blank");

		// Turning Auto Update OFF for Firefox.
		firefoxProfile.setPreference("app.update.enabled", false);
		firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/x-msdos-program, application/x-unknown-application-octet-stream, application/vnd.ms-powerpoint, application/excel, application/vnd.ms-publisher, application/x-unknown-message-rfc822, application/vnd.ms-excel, application/msword, application/x-mspublisher, application/x-tar, application/zip, application/x-gzip,application/x-stuffit,application/vnd.ms-works, application/powerpoint, application/rtf, application/postscript, application/x-gtar, video/quicktime, video/x-msvideo, video/mpeg, audio/x-wav, audio/x-midi, audio/x-aiff, text/html, application/octet-stream");
		firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);

		return firefoxProfile;
	}

	/**
	 * Quits this driver, closing every associated window.
	 * 
	 * @param Config
	 *            test config instance for the browser to be quit
	 */
	public static void quitBrowser(Config testConfig) {
	try {
			if (testConfig.driver != null) {
				testConfig.logComment("Quit the browser");
				testConfig.driver.quit();
			}
		} catch (UnreachableBrowserException e) {
			testConfig.logWarning(ExceptionUtils.getFullStackTrace(e));
		}	
	}

	/**
	 * Takes the screenshot of the current active browser window
	 * 
	 * @param Config
	 *            test config instance
	 * @param destination
	 *            file to which screenshot is to be saved
	 */
	public static void takeScreenShoot(Config testConfig, File destination) {
		try {
			if (testConfig.driver != null) {
				byte[] screenshot = null;

				try {
					screenshot = captureScreenshot(testConfig);
				} catch (NullPointerException ne) {
					testConfig.logWarning(
							"NullPointerException:Screenshot can't be taken. Probably browser is not reachable");
					testConfig.driver = null;
				}

				if (screenshot != null) {
					try {
						FileUtils.writeByteArrayToFile(destination, screenshot);

						float compressionQuality = (float) 0.5;
						try {
							compressionQuality = Float
									.parseFloat(testConfig.getRunTimeProperty("ScreenshotCompressionQuality"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						compressJpegFile(destination, destination, compressionQuality);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (testConfig.getRunTimeProperty("APKUrl") == null) {
					if (testConfig.driver != null)
						testConfig.logComment("<B>Page URL</B>:- " + testConfig.driver.getCurrentUrl());
					else
						testConfig.logComment("Driver is NULL");
				}
				String href = getResultsURLOnRunTime(testConfig, destination.getPath());
				testConfig.logComment(
						"<B>Screenshot</B>:- <a href=" + href + " target='_blank' >" + destination.getName() + "</a>");
			}
		} catch (UnreachableBrowserException e) {
			testConfig.enableScreenshot = false;
			testConfig.logWarning("Unable to take screenshot:- " + ExceptionUtils.getStackTrace(e));
		} catch (Exception e) {
			testConfig.enableScreenshot = false;
			testConfig.logWarning("Unable to take screenshot:- " + ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}

	/**
	 * Pause the execution for given seconds
	 * 
	 * @param seconds
	 */
	public static void wait(Config testConfig, int seconds) {
		int milliseconds = seconds * 1000;
		try {
			Thread.sleep(milliseconds);
			testConfig.logComment("Wait for '" + seconds + "' seconds");

		} catch (InterruptedException e) {

		}
	}
	
	/**
	 * Pause the execution for given seconds
	 * 
	 * @param seconds
	 */
	public static void waitWithoutLogging(Config testConfig, int seconds) {
		int milliseconds = seconds * 1000;
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {

		}
	}

	/**
	 * Waits for the given WebElement to appear on the specified browser instance
	 * 
	 * @param Config
	 *            test config instance
	 * @param element
	 *            element to be searched
	 */
	public static void waitForPageLoad(Config testConfig, WebElement element) {
		waitForPageLoad(testConfig, element, testConfig.getRunTimeProperty("ObjectWaitTime"));
	}

	/**
	 * Waits for the given WebElement to appear on the specified browser instance
	 * 
	 * @param Config
	 *            test config instance
	 * @param element
	 *            element to be searched
	 * @param ObjectWaitTime
	 *            - max time to wait for the object
	 */
	public static void waitForPageLoad(Config testConfig, WebElement element, String objectWaitTime) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date startDate = new Date();
		double timeTaken = 0;
		WebDriverWait wait = null;
		// Increase the timeout value
		Long ObjectWaitTime = Long.parseLong(objectWaitTime);
		String callingClassName = getCallerClassName();
		String currentPageName = callingClassName.substring(callingClassName.lastIndexOf('.') + 1);

		testConfig.logComment("Started waiting for '" + currentPageName + "' to load at:- "
				+ dateFormat.format(startDate) + ". Wait upto " + ObjectWaitTime + " seconds.");

		wait = new WebDriverWait(testConfig.driver,Duration.ofSeconds(ObjectWaitTime));
		testConfig.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

		try {
			wait.until(ExpectedConditions.visibilityOf(element));
		} catch (StaleElementReferenceException e) {
			testConfig.logWarning(
					"StaleElementReferenceException occured, wait upto additional " + ObjectWaitTime + " seconds.");

			try {
				wait.until(ExpectedConditions.visibilityOf(element));
			} catch (Exception exc) {
				testConfig.endExecutionOnfailure = true;
				Date endDate = new Date();
				testConfig.logWarning("'" + currentPageName + "' NOT loaded even after :- "
						+ (endDate.getTime() - startDate.getTime()) / 1000.00 + " seconds. Exiting...");
				throw exc;
			}
		} catch (TimeoutException e) {
			testConfig.logWarning("'" + currentPageName + "' still not loaded, so wait upto additional "
					+ ObjectWaitTime + " seconds.");
			try {
				wait.until(ExpectedConditions.visibilityOf(element));
			} catch (TimeoutException tm) {
				testConfig.endExecutionOnfailure = true;
				Date endDate = new Date();
				timeTaken = (endDate.getTime() - startDate.getTime()) / 1000.00;
				testConfig.logWarning(
						"'" + currentPageName + "' NOT loaded even after :- " + timeTaken + " seconds. Exiting...");
				throw new TimeoutException(
						"'" + currentPageName + "' did not load after waiting for " + 2 * ObjectWaitTime + " seconds");// approximate
				// time
			} catch (Exception ee) {
				testConfig.endExecutionOnfailure = true;
				Date endDate = new Date();
				timeTaken = (endDate.getTime() - startDate.getTime()) / 1000.00;
				testConfig.logWarning(
						"'" + currentPageName + "' NOT loaded even after :- " + timeTaken + " seconds. Exiting...");
				throw ee;
			}
		}

		catch (WebDriverException webDriverException) {
			testConfig.logComment("\nWebDriverException or InterruptedException appeared, So trying again...");
			Thread.interrupted();

			for (int i = 1; i <= 5; i++) {
				try {
					wait.until(ExpectedConditions.visibilityOf(element));
				} catch (Throwable exception) {
					if (exception.getClass().toString().contains("InterruptedException")) {
						testConfig
						.logComment("InterruptedException appeared " + (i + 1) + " times, So trying again...");
						Thread.interrupted();
						testConfig.logComment("***********************************************");
						testConfig.logComment(ExceptionUtils.getFullStackTrace(webDriverException));
						testConfig.logComment("***********************************************");
					} else if (exception.getClass().toString().contains("NoSuchElementException")) {
						testConfig.endExecutionOnfailure = true;
						Date endDate = new Date();
						timeTaken = (endDate.getTime() - startDate.getTime()) / 1000.00;
						testConfig.logWarning("'" + currentPageName + "' NOT loaded even after :- " + timeTaken
								+ " seconds. Exiting...");
						throw exception;
					} else {
						testConfig.logComment("\n<-----Exception in waitForPageLoad()----->");
						// testConfig.logComment(ExceptionUtils.getFullStackTrace(exception));
						throw exception;
					}
				}
			}
		}

		ObjectWaitTime = Long.parseLong(testConfig.getRunTimeProperty("ObjectWaitTime"));
		testConfig.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ObjectWaitTime));


		Date endDate = new Date();
		double timeTaken1 = 0;
		double timeTaken2 = 0;
		timeTaken1 = (endDate.getTime() - startDate.getTime()) / 1000.00;

		timeTaken = timeTaken1 + timeTaken2;
		testConfig.logComment(currentPageName + " with Loader loaded in :- " + timeTaken + " seconds.");

		if (timeTaken > 120)
			testConfig.logComment("<B><font color='Red'>" + currentPageName + " is loaded after " + timeTaken / 60
					+ " minutes.</font></B>");
	}

	/**
	 * This function return the URL of a file on runtime depending on LOCAL or
	 * OFFICIAL Run
	 * 
	 * @param testConfig
	 * @param fileURL
	 * @return
	 */
	public static String getResultsURLOnRunTime(Config testConfig, String fileURL) {
		String resultsIP = "";

		if (Config.RunType.equalsIgnoreCase("official"))
			resultsIP = "http://" + testConfig.getRunTimeProperty("RemoteAddress") + ":8084" +"//";

		return resultsIP + fileURL;
	}
}