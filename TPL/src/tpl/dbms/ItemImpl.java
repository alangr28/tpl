package tpl.dbms;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import tpl.dbms.TransactionImpl.PQsort;


/**
 * Implementation of Item Interface.
 * @author alangr28
 */
public class ItemImpl implements Item{

	private String key, value;

	private final AbstractQueue<Transaction> waitingQueue;
	
	private final int PriorityQueueInitialCapacity = 8;
	
	private LockState lockState;

	private Integer readTimeStamp;

	private Integer writeTimeStamp;

	private final List<Transaction> transactionsHoldingSharedLock;

	private Transaction transactionHoldingExclusiveLock;
	
	/**
	 * Item constructor
	 * @param key the item key
	 * @param value Value of the item
	 */
	public ItemImpl(String key, String value){
		this.key = key;
		this.value = value;
		writeTimeStamp = -1;
		readTimeStamp = -1;
		transactionHoldingExclusiveLock = null;
		transactionsHoldingSharedLock = Collections.synchronizedList(new ArrayList<Transaction>());
		waitingQueue = new PriorityQueue<Transaction>(PriorityQueueInitialCapacity, new PQsort());
		lockState = LockState.UNLOCKED;
	}

	/**
	 * @return the item key
	 */
	@Override
	public String getKey() {
		return key;
	}

	/**
	 * @return the item value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the item value 
	 * @param value the value to set
	 */
	@Override
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the lock state
	 */
	@Override
	public LockState getLockState() {
		return lockState;
	}

	/**
	 * @param lockState the lock state to set
	 */
	@Override
	public void setLockState(LockState lockState) {
		this.lockState = lockState;
	}

	/**
	 * @return the largest timestamp among all the timestamps of transactions that have successfully read this item
	 */
	@Override
	public Integer getReadTimeStamp() {
		return readTimeStamp;
	}

	/**
	 * Sets the largest timestamp among all the timestamps of transactions that have successfully read this item
	 * @param readTimeStamp the read_timestamp to set
	 */
	@Override
	public void setReadTimeStamp(Integer readTimeStamp) {
		this.readTimeStamp = readTimeStamp;
	}

	/**
	 * @return the largest timestamp among all the timestamps of transactions that have successfully written this item
	 */
	@Override
	public Integer getWriteTimeStamp() {
		return writeTimeStamp;
	}

	/**
	 * Sets the largest timestamp among all the timestamps of transactions that have successfully written this item
	 * @param writeTimeStamp the write_timestamp to set
	 */
	@Override
	public void setWriteTimeStamp(Integer writeTimeStamp) {
		this.writeTimeStamp = writeTimeStamp;
	}

	/**
	 * @return the transaction holding the exclusive lock (on the item)
	 */
	@Override
	public Transaction getTransactionHoldingExclusiveLock() {
		return transactionHoldingExclusiveLock;
	}

	/**
	 * @param transactionHoldingExclusiveLock the transaction holding the exclusive lock (on the item) to set
	 */
	@Override
	public void setTransactionHoldingExclusiveLock(Transaction transactionHoldingExclusiveLock) {
		this.transactionHoldingExclusiveLock = transactionHoldingExclusiveLock;
	}

	/**
	 * @return the waiting queue of transactions
	 */
	@Override
	public AbstractQueue<Transaction> getWaitingQueue() {
		return waitingQueue;
	}

	/**
	 * @return the transactions holding a shared lock
	 */
	@Override
	public List<Transaction> getTransactionsHoldingSharedLock() {
		return transactionsHoldingSharedLock;
	}
	
	
}
