package Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TestVariables
{
	
	String applicationName() default "";
	
	String environment() default "";
	
    int [] dataSheetRowNumber() default 0;
	
	String dataSheetName() default "ParameterData" ;
	
	int newCommandTimeout() default 120;

	String remoteExecution() default "";
}
