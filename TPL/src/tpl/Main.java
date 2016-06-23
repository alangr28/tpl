package tpl;

import tpl.dbms.DBMS;
import tpl.dbms.Log;

/**
 * Main program to test the DBMS processSchedule method.
 * The last one, capable to simulate
 * the behavior of processing a schedule of transaction operations
 * by the Rigorous Two Phase Locking Protocol.
 * More info: 
 * Elmasri, Ramez and Sham Navathe. Fundamentals Of Database Systems. (7th Edition)
 * @author alangr28
 */
public class Main {

	/**
	 * @param args args
	 */
	public static void main(String[] args){
		
		String input_path = "/Users/alangr28/Documents/workspace/TPL/src/tpl/samples/input/sample_input_3.txt";
		String output_path = "/Users/alangr28/Documents/workspace/TPL/src/tpl/samples/output/sample_output3.txt";
		
		Log.outputPath = output_path;
		(new DBMS()).processSchedule(input_path);	
		Log.writeToDisk();
		 
	}
	
}
