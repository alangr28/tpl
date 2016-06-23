package tpl.dbms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser of (basic) database schedules; including operations like begin transaction, 
 * end transaction, write item &amp; read item.
 * @author alangr28
 */
public class ScheduleParser {
	
	/**
	 * Parses a line.
	 * Each line has a single transaction operation. 
	 * The possible operations are: b (begin transaction), r (read item), w (write item), and e (end transaction). 
	 * Each operation will be followed by a transaction id that is an integer between 1 and 99. 
	 * For r and w operations, an item name follows between parentheses (item names are single letters from A to Z). 
	 * @param line input
	 * @return the operation
	 * @throws OperationException if the line is not recognized
	 */
	public static Operation parseLine(String line) throws OperationException{
		OperationType operationType = null;
		final String regex_b, regex_r, regex_w, regex_e;
		Pattern patternTransacionId, patternItem; 
		Matcher matcher;
		Integer transactionId = -1;
		String item = null;
				
		//Regular expression for Begin transaction line
		regex_b = "\\s*[bB]\\d+;\\s*";
		//Regular expression for Read item line
		regex_r = "\\s*[rR]\\d+\\s*\\(\\s*[A-Z]\\s*\\)\\s*;\\s*";
		//Regular expression for Write item line
		regex_w = "\\s*[wW]\\d+\\s*\\(\\s*[A-Z]\\s*\\)\\s*;\\s*";
		//Regular expression for End transaction line
		regex_e = "\\s*[eE]\\d+;\\s*";
		
		//Helps to extract transaction id
		patternTransacionId = Pattern.compile("\\d+");
		//Helps to extract item
		patternItem = Pattern.compile("\\(\\s*[A-Z]\\s*\\)");
		
		Log.outputMsg("\""+line+"\" parsed.");
		
		//Classifies operation
		if(line.matches(regex_b)) {
			operationType = OperationType.BEGIN_TRANSACTION;
		} else if(line.matches(regex_r)) {
			operationType = OperationType.READ_ITEM;
		} else if(line.matches(regex_w)) {
			operationType = OperationType.WRITE_ITEM;
		} else if(line.matches(regex_e)) {
			operationType = OperationType.END_TRANSACTION;
		} else {
			throw new OperationException("Error while parsing: " + line);
		}
		
		//Gets transaction id
		matcher = patternTransacionId.matcher(line);
		if(matcher.find()) 
			transactionId = Integer.parseInt(matcher.group());
		
		//Gets item if read/write operation
		if (operationType == OperationType.READ_ITEM || operationType == OperationType.WRITE_ITEM){
			matcher = patternItem.matcher(line);
			if(matcher.find()) 
				item = matcher.group().replace(")","").replace("(","").trim();
		}
		
		return new OperationImpl(transactionId, operationType, item);
	}
	
	
}
