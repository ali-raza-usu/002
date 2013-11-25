package utilities;


import java.io.File;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;


public class Utilities {
	
	public static void println(String _string)
	{
		System.out.println(_string);
	}

	public static void print(String _string)
	{
		System.out.print(_string);
	}
	
	public static void configureLogger(String _file) {
		
		
		FileAppender appender = null;
		Layout layout = new PatternLayout("%r [%t] %d{HH:mm:ss,SSS} %-5p %c %l - %m%n");
		try {				
			File  file = new File(_file);
			if(file!=null)
				file.delete();
			appender = new FileAppender(layout, _file);
			BasicConfigurator.configure(appender);
		} catch (Exception e) {
			Utilities.println("Can't locate file");
		}
	}
		
	public static byte[] getNewVersion(IMessage msg) {
			return null;	
	}
}
