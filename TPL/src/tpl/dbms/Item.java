package tpl.dbms;

import java.util.AbstractQueue;
import java.util.List;


/**
 * Interface that represents a database item, it keeps information like: item identifier (key), value, lock state, read_timestamp, 
 * write_timestamp, transactions holding this item and transactions waiting for it.
 * @author alangr28
 */
public interface Item {

	/**
	 * @return the item key
	 */
	public String getKey();

	/**
	 * @return the item value
	 */
	public String getValue();
	
	/**
	 * Sets the item value 
	 * @param value the value to set
	 */
	public void setValue(String value);

	/**
	 * @return the lock state
	 */
	public LockState getLockState();

	/**
	 * @param lockState the lock state to set
	 */
	public void setLockState(LockState lockState);

	/**
	 * @return the largest timestamp among all the timestamps of transactions that have successfully read this item
	 */
	public Integer getReadTimeStamp();
	/**
	 * Sets the largest timestamp among all the timestamps of transactions that have successfully read this item
	 * @param readTimeStamp the read_timestamp to set
	 */
	public void setReadTimeStamp(Integer readTimeStamp);

	/**
	 * @return the largest timestamp among all the timestamps of transactions that have successfully written this item
	 */
	public Integer getWriteTimeStamp();

	/**
	 * Sets the largest timestamp among all the timestamps of transactions that have successfully written this item
	 * @param writeTimeStamp the write_timestamp to set
	 */
	public void setWriteTimeStamp(Integer writeTimeStamp);
	/**
	 * @return the transaction holding the exclusive lock (on the item)
	 */
	public Transaction getTransactionHoldingExclusiveLock();
	/**
	 * @param transactionHoldingExclusiveLock the transaction holding the exclusive lock (on the item) to set
	 */
	public void setTransactionHoldingExclusiveLock(Transaction transactionHoldingExclusiveLock);

	/**
	 * @return the waiting queue of transactions
	 */
	public AbstractQueue<Transaction> getWaitingQueue();

	/**
	 * @return the transactions holding a shared lock
	 */
	public List<Transaction> getTransactionsHoldingSharedLock();
	
}
