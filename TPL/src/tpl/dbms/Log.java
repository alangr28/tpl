package tpl.dbms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Handles all log messages.
 * @author alangr28
 */
public class Log {
	
	/**
	 * Enables log messages 
	 */
	public static boolean enabled = true;

	/**
	 * Path of output file  
	 */
	public static String outputPath = null;
	
	/**
	 * Store all log messages
	 */
	private static StringBuffer bf = new StringBuffer();
	
	/**
	 * Outputs the message
	 * @param message the message
	 */
	public static void outputMsg(String message){
		if(enabled){
			System.out.print(message+"\n");
			bf.append(message+"\n");
		}
	}

	/**
	 * Writes all log messages to a file
	 */
	public static void writeToDisk(){
		if(enabled && outputPath != null){

			File file = new File(outputPath);
			file.getParentFile().mkdirs();
		    
	        try {
	            PrintWriter pw = new PrintWriter(file);
	            pw.println(bf.toString());
	            pw.close();
	        } catch (FileNotFoundException e) {
				e.printStackTrace();
	        }
		}
	}

}
