package tpl.dbms;

/**
 * Implements interface Operation.
 * @author alangr28
 */
public class OperationImpl implements Operation {
	
	private OperationType operationType;
	private String item;
	private Integer transactionId;
	
	/**
	 * Constructor
	 * @param transactionId the transaction identifier
	 * @param operationType operation type
	 * @param item item
	 */
	public OperationImpl(Integer transactionId, OperationType operationType, String item){
		this.transactionId = transactionId;
		this.operationType = operationType;
		this.item = item;
	}

	/**
	 * @return the operation type
	 */
	public OperationType getOperationType() {
		return operationType;
	}

	/**
	 * @param operationType the operation type to set
	 */
	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	/**
	 * @return the item
	 */
	public String getItem() {
		return item;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(String item) {
		this.item = item;
	}

	/**
	 * @return the transaction id
	 */
	public Integer getTransactionId() {
		return transactionId;
	}

	/**
	 * @param transactionId the transaction id to set
	 */
	public void setTransactionId(Integer transactionId) {
		this.transactionId = transactionId;
	}
	
	
}
