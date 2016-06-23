package tpl.dbms;

/**
 * Represents a database operation, it includes: transaction id, operation type &amp; item.
 * @author alangr28
 */
public interface Operation {
	
	/**
	 * @return the operation type
	 */
	public OperationType getOperationType();

	/**
	 * @param operationType the operation type to set
	 */
	public void setOperationType(OperationType operationType);

	/**
	 * @return the item
	 */
	public String getItem();

	/**
	 * @param item the item to set
	 */
	public void setItem(String item);

	/**
	 * @return the transaction id
	 */
	public Integer getTransactionId();

	/**
	 * @param transactionId the transaction id to set
	 */
	public void setTransactionId(Integer transactionId) ;
	
}
