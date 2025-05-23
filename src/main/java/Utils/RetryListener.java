package Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;

/**
 * Listener for RetryAnalyzer class.
 * 
 * @author nikumar
 *
 */

public class RetryListener implements IAnnotationTransformer
{
	
	@Override
	public void transform(ITestAnnotation annotation, Class testClass, Constructor arg2, Method arg3)
	{
		
		IRetryAnalyzer retry = annotation.getRetryAnalyzer();
		if (retry == null)
		{
			annotation.setRetryAnalyzer(RetryAnalyzer.class);
		}
		
	}
	
}
