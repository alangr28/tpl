package tpl.dbms;

/**
 * Custom Exception to handle errors involved while processing a database operation
 * @author alangr28
 */
public class OperationException extends Exception {

	private static final long serialVersionUID = -1615965346346615307L;

	/**
	 * Constructor
	 */
	public OperationException(){}
	
	/**
	 * Constructor that accepts a message
	 * @param message the message
	 */
    public OperationException(String message)
    {
       super(message);
    }
}
