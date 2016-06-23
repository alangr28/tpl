package tpl.dbms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Implements Transaction interface.
 * @author alangr28
 */
public class TransactionImpl implements Transaction{ 
	
	/**
	 * transaction timestamp
	 */
	public final int timeStamp;	
	/**
	 * transaction identifier
	 */
	private final Integer transactionId;
	/**
	 * operations to perform 
	 */
	private final List<Operation> pendingOperations;
	/**
	 * DBMS Thread 
	 */
	private final DBMSThread dbmsThread;
	/**
	 * List of locks (on items) already acquired 
	 */
	private final List<String> acquiredLocks;
	/**
	 * Index of the current operation to perform in pendingOperations list
	 */
	private volatile int currentOperation;
	/**
	 * transaction state
	 */
	private volatile TransactionState transactionState;	
	/**
	 * true if there are pending operations to perform, false if there are not
	 */
	private volatile boolean emptyList;
	/**
	 * Evaluates if this transaction (thread) should be finished
	 */
	private volatile boolean shutdown;
	
	/**
	 * Constructor.
	 * @param transactionId the transaction identifier
	 * @param timeStamp the transaction timestamp
	 * @param dbmsThread DBMSThread
	 */
	public TransactionImpl(int transactionId, int timeStamp, DBMSThread dbmsThread){
		this.transactionId = transactionId;
		this.timeStamp = timeStamp;
		transactionState = TransactionState.ACTIVE;
		shutdown = false;
		currentOperation = 0;
		emptyList = true;
		pendingOperations = new ArrayList<Operation>();
		this.dbmsThread = dbmsThread;
		acquiredLocks = Collections.synchronizedList(new ArrayList<String>());
	}
	
	@Override
	public void addOperation(Operation operation){
		pendingOperations.add(operation);
	    synchronized(this){
	    	emptyList = false;
	    	notify();
	    }
		
	}
	
	@Override
	public void run() {
		while(!shutdown){
			if(transactionState == TransactionState.ACTIVE){
				//Wait until get next operation
				synchronized(this){
					try {
						while(emptyList){ wait(); }
					} catch (InterruptedException e) { System.err.println(e.getMessage()); }
				}
				//Process operation
				processNextOperation();
			}
			else if (transactionState == TransactionState.ABORTED){
				//Wait a little while and then restart
				try {
					Thread.sleep(DBMSThread.DELAY_BETWEEN_OPERATIONS*2);
				} catch (InterruptedException e) { e.printStackTrace(); }
				//Restart
				synchronized(this){
					transactionState = TransactionState.ACTIVE;
					notify();
				}
			}
		}
	}
	
	@Override
	public TransactionState getTransactionState() {
		return transactionState;
	}
	
	
	/**
	 * Sets a new transaction state
	 * @param transactionState
	 */
	private void setTransactionState(TransactionState transactionState){
		this.transactionState = transactionState;
		switch(transactionState){
		case ABORTED:
			Log.outputMsg(getTransactionName()+" was aborted!");
			break;
		case ACTIVE:
			Log.outputMsg(getTransactionName()+" is active.");
			break;
		case COMMITTED:
			Log.outputMsg(getTransactionName()+" was committed. **");
			break;
		case FAILED:
			Log.outputMsg(getTransactionName()+" aborted & failed! **");
			break;
		case PARTIALLY_COMMITED:
			Log.outputMsg(getTransactionName()+" is partially committed.");
			break;
		case WAITING:
			Log.outputMsg(getTransactionName()+" is waiting...");
			break;
		default:
			break;
		
		}
	}
	
	@Override
	public int getTimeStamp(){
		return timeStamp;
	}
	
	@Override
	public String getTransactionName(){
		return "T"+transactionId;
	}
	
	@Override
	public int getTransactionId() {
		return transactionId;
	}
	
	/**
	 * @author alangr28
	 *
	 */
	public static class PQsort implements Comparator<Transaction> {
		@Override
		public int compare(Transaction t1, Transaction t2) {
			return ((TransactionImpl) t2).timeStamp - ((TransactionImpl) t1).timeStamp;
		}
	}
	
	/**
	 * Upgrades the lock on item for this transaction, if requested
	 * @param lockState
	 * @param item
	 * @return if there was a lock upgrade
	 */
	private boolean upgradeLockProcessed(LockState lockState, Item item){
		
		boolean processed = false;
		
		if(lockState == LockState.EXCLUSIVE_LOCKED
				&& item.getLockState() == LockState.SHARE_LOCKED  
				&& item.getTransactionsHoldingSharedLock().size()==1 
				&& item.getTransactionsHoldingSharedLock().get(0).equals(this)){
			
			// -- Thomas write rule --
			if(item.getReadTimeStamp() > timeStamp){
				//some younger transaction has already read the value of 
				//item x before T had a chance to write x
				//thus violating the timestamp ordering.
				Log.outputMsg("Thomas write rule. tsR("+item.getKey()+")="+item.getReadTimeStamp() 
				+" > ts("+getTransactionName()+")="+ timeStamp+". Timestamp ordering violation. ");
				abortAndFailed();
			}
			else if (item.getWriteTimeStamp() > timeStamp){
				//some younger transaction has already written the value of 
				//item X before T had a chance to write X
				//then skip obsolete update
				Log.outputMsg("Thomas write rule. tsW("+item.getKey()+")="+item.getWriteTimeStamp() 
				+" > ts("+getTransactionName()+")="+ timeStamp+". Obsolete write. Operation skipped. ");
				operationSucceed();
			}
			else{
				//Lock upgrade & write item operations are safe
				item.setLockState(LockState.EXCLUSIVE_LOCKED);
				item.getTransactionsHoldingSharedLock().remove(0);
				item.setTransactionHoldingExclusiveLock(this);
				Log.outputMsg(getTransactionName()+" upgraded lock on "+item.getKey()+".");
				//write(x)
				item.setValue(item.getKey()); 
				Log.outputMsg(getTransactionName()+" wrote "+item.getKey()+"!");
				operationSucceed();
			}
			processed = true;
		}
		return processed;
	}
	
	/**
	 * Wait-Die protocol
	 * @param lockState
	 * @param item
	 * @return if Wait-Die rules were applied
	 */
	private boolean waitDieProtocolProcessed(LockState lockState, Item item){
		Transaction Tj;
		Integer minTid;
		boolean processed = false;
		
		if(item.getLockState() == LockState.EXCLUSIVE_LOCKED ){
			// -- Wait-die Protocol--
			Tj = dbmsThread.getTransactions().get(item.getTransactionHoldingExclusiveLock().getTransactionId());
		
			if(this.transactionId < Tj.getTransactionId()){
				//This transaction is older than Tj
				//Then it is OK to wait for x
				Log.outputMsg("Wait-die Protocol. "+getTransactionName()+" requested "+item.getKey()
				+", but it is 'older' than "+Tj.getTransactionName()+". Waiting...");
				item.getWaitingQueue().add(this);
				setTransactionState(TransactionState.WAITING);
			} else {
				//This transaction is younger than Tj
				//Abort and restart later with the same timestamp
				Log.outputMsg("Wait-die Protocol. "+getTransactionName()+" requested "+item.getKey()
				+", but it is 'younger' than "+
				Tj.getTransactionName()+".");
				abortAndRestart();
			}
			processed = true;
		}
		else if(item.getLockState() == LockState.SHARE_LOCKED && lockState == LockState.EXCLUSIVE_LOCKED){
			
			// -- Wait-die Protocol--
			minTid = transactionId;
			
			for(Transaction Th : item.getTransactionsHoldingSharedLock()){
				minTid = Math.min(minTid, Th.getTransactionId());
			}
			
			if(minTid == transactionId){
				//Ti (this transaction) is the oldest transaction
				//Then it is OK to wait for x
				Log.outputMsg("Wait-die Protocol. Another transaction has an exclusive lock on "+
				item.getKey()+". "+getTransactionName()+" is the oldest transaction. Waiting...");
				item.getWaitingQueue().add(this);
				setTransactionState(TransactionState.WAITING);
				
			} else {
				Log.outputMsg("minTid != tid"+minTid +"=="+ transactionId);
				//This transaction is younger than Tj
				//Abort and restart later with the same timestamp
				Log.outputMsg("Wait-die Protocol. Another transaction has an exclusive lock on "+
				item.getKey()+". "+getTransactionName()+" is the youngest transaction. ");
				abortAndRestart();
			}
			processed = true;
		}
		return processed;
	}
	
	/**
	 * Rigorous Two-Phase Locking rules
	 * @param lockState
	 * @param item
	 */
	private void rigorousTwoPhaseLocking(LockState lockState, Item item){
		
		boolean lockAlreadyAcquired = 
				((item.getLockState() == LockState.EXCLUSIVE_LOCKED
					&& item.getTransactionHoldingExclusiveLock().equals(this))
				||(lockState == LockState.SHARE_LOCKED
					&& item.getLockState() == LockState.SHARE_LOCKED
					&& item.getTransactionsHoldingSharedLock().contains(this)));
		
		//Check if this transaction already has the appropriate lock on the item
		if (!lockAlreadyAcquired){	
			//If it does not, then request it
			if(upgradeLockProcessed(lockState, item)) return;
			if(waitDieProtocolProcessed(lockState, item)) return;
		} else {
			Log.outputMsg(this.getTransactionName() + " already has the appropriate lock on "+item.getKey()+".");
		}
		
		if(lockState == LockState.SHARE_LOCKED){
			//Read_lock					
			// -- Time stamp Protocol --
			if(item.getWriteTimeStamp() > timeStamp){
				//Some younger transaction has already written the value of item X before 
				//this transaction had a chance to read X.
				//Abort and roll back
				Log.outputMsg("Timestamp Protocol. Some younger transaction wrote the value of the item +"+item.getKey()+". ");
				abortAndFailed();
			}
			else {
				//Reading x is safe
				if(!lockAlreadyAcquired){
					item.setLockState(LockState.SHARE_LOCKED);
					item.getTransactionsHoldingSharedLock().add(this);
					acquiredLocks.add(item.getKey());
				}
				//read(x)
				item.getValue(); 
				Log.outputMsg(getTransactionName()+" read "+item.getKey()+"!");					
				//update tsR of x
				item.setReadTimeStamp(Math.max(item.getReadTimeStamp(), timeStamp));
				Log.outputMsg("tsR("+item.getKey()+")= "+item.getReadTimeStamp()+".");
				operationSucceed();
			}
		}
		else if(lockState == LockState.EXCLUSIVE_LOCKED){
			//Write_lock	
			// -- Thomas write rule --
			if(item.getReadTimeStamp() > timeStamp){
				//some younger transaction has already read the value of 
				//item x before T had a chance to write x
				//thus violating the timestamp ordering.
				Log.outputMsg("Thomas write rule. tsR("+item.getKey()+")="+item.getReadTimeStamp() 
				+" > ts("+getTransactionName()+")="+ timeStamp+". Timestamp ordering violation. ");
				abortAndFailed();
			}
			else if (item.getWriteTimeStamp() > timeStamp){
				//some younger transaction has already written the value of 
				//item X before T had a chance to write X
				//then skip obsolete update
				Log.outputMsg("Thomas write rule. tsW("+item.getKey()+")="+item.getWriteTimeStamp() 
				+" > ts("+getTransactionName()+")="+ timeStamp+". Obsolete write. Operation skipped. ");
				operationSucceed();
			}
			else{
				//Writing x is safe
				if(!lockAlreadyAcquired){
					item.setLockState(LockState.EXCLUSIVE_LOCKED);
					item.setTransactionHoldingExclusiveLock(this);
					acquiredLocks.add(item.getKey());
				}
				//write(x)
				item.setValue(item.getKey());  
				Log.outputMsg(getTransactionName()+" wrote "+item.getKey()+"!");
				//update tsW of x
				item.setWriteTimeStamp(timeStamp);
				Log.outputMsg("tsW("+item.getKey()+")= "+timeStamp+".");
				operationSucceed();
			}
		}
	}
	
	/**
	 * Requests a shared or exclusive lock for the item 
	 * @param lockState type of lock requested
	 * @param itemKey the item key
	 */
	private void lockItem(LockState lockState, String itemKey) {
		Item x;
		
		if(lockState == LockState.EXCLUSIVE_LOCKED)
			Log.outputMsg(getTransactionName()+" requested an exclusive lock on "+itemKey+".");
		else if(lockState == LockState.SHARE_LOCKED)
			Log.outputMsg(getTransactionName()+" requested a shared lock on "+itemKey+".");
		
		x = dbmsThread.getBufferPool().getItem(itemKey);
		
		synchronized(x){
			// -- Two Phase Locking Protocol Rigorous (including Thomas Write rule) --
			// a transaction T does not release any of its locks (exclusive or shared) until after it commits or aborts
			rigorousTwoPhaseLocking(lockState, x);
		}
	}
	
	/**
	 * Marks current operation as succeeded
	 */
	private void operationSucceed(){
		currentOperation++;
		emptyList = (pendingOperations.size() == currentOperation);
	}
	
	/**
	 * Releases each lock this transaction has.
	 * If there were some transaction waiting for an item, wake it up. 
	 */
	private void releaseLocks() {
		Item x;
		Transaction Tj;
		//synchronized(acquiredLocks){
			//Release locks & wake up transactions from waiting list
			for(Iterator<String> it = acquiredLocks.iterator(); it.hasNext(); ) {
				x = dbmsThread.getBufferPool().getItem(it.next());
				synchronized(x){
					//Free locks
					if(x.getLockState() == LockState.EXCLUSIVE_LOCKED 
							&& x.getTransactionHoldingExclusiveLock().equals(this)){
						x.setLockState(LockState.UNLOCKED);
						x.setTransactionHoldingExclusiveLock(null);
					}
					if(x.getLockState() == LockState.SHARE_LOCKED
							&& x.getTransactionsHoldingSharedLock().contains(this)){
						x.getTransactionsHoldingSharedLock().remove(this);
						if(x.getTransactionsHoldingSharedLock().isEmpty()){
							x.setLockState(LockState.UNLOCKED);
						}
					}
					Log.outputMsg(getTransactionName()+" released its lock on "+x.getKey()+".");
					
					//Next transaction
					Tj = x.getWaitingQueue().poll();
					if(Tj!=null){
						synchronized(Tj){
							((TransactionImpl) Tj).setTransactionState(TransactionState.ACTIVE);
							Log.outputMsg(Tj.getTransactionName()+" woke up!");
							Tj.notify();
						}
					}
				}
				it.remove();
			}

	}
	
	/**
	 * Does roll-back for all previous operations
	 */
	private void rollBack(){
		//There's no need to change values of items,
		//it's not the purpose of this program (project).
		//Releasing resources is the only option.
		Log.outputMsg(getTransactionName()+" did rollback.");
		releaseLocks();
	}

	/**
	 * Commits this transaction
	 */
	private void commit(){
		setTransactionState(TransactionState.PARTIALLY_COMMITED);
		setTransactionState(TransactionState.COMMITTED);
		releaseLocks();
		shutdown = true;
	}

	/**
	 * Aborts and terminates this transaction (no restart)
	 */
	private void abortAndFailed(){
		setTransactionState(TransactionState.FAILED);
		rollBack();
		shutdown = true;
	}
	
	/**
	 * Aborts and restarts this transaction 
	 */
	private void abortAndRestart(){
		rollBack();
		setTransactionState(TransactionState.ABORTED);
		currentOperation = 0;
	}
	
	/**
	 * Process next operation. It must lock (preferably) the entire transaction.
	 */
	private void processNextOperation(){
		synchronized(this){
			Operation nextOperation = pendingOperations.get(currentOperation);
			Log.outputMsg("-----");
			
			switch(nextOperation.getOperationType()){
			case END_TRANSACTION:
				commit();
				break;
			case READ_ITEM:
				lockItem(LockState.SHARE_LOCKED, nextOperation.getItem());
				break;
			case WRITE_ITEM:
				lockItem(LockState.EXCLUSIVE_LOCKED, nextOperation.getItem());
				break;
			default:
				break;
			}
			notify();
		} 
	}

	
}

	

