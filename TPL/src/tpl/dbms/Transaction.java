package tpl.dbms;

/**
 * Represents a database transaction.
 * @author alangr28
 */
public interface Transaction extends Runnable{
	
	/**
	 * Inserts the operation into transaction's pending tasks.
	 * The Transaction object must process all operations in the order they were added.
	 * @param operation the operation to add
	 */
	public void addOperation(Operation operation);

	/**
	 * @return transaction timestamp
	 */
	public int getTimeStamp();
	
	/**
	 * @return the transaction identifier
	 */
	public int getTransactionId();
	
	/**
	 * @return transaction name
	 */
	public String getTransactionName();
	
	/**
	 * @return transaction state
	 */
	public TransactionState getTransactionState();
	
}
