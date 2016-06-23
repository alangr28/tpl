package tpl.dbms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A DBMSThread is the thread of execution in the DBMS, able to simulate the behavior of 
 * processing a sequence of transaction operations.
 * @author alangr28
 */
public class DBMSThread {
	
	/**
	 * Intended delay between each operation processing
	 */
	public static final int DELAY_BETWEEN_OPERATIONS = 100; //milliseconds
	
	/**
	 * Thread Pool for transactions
	 */
	private final Map<Integer, Transaction> transactions;
	/**
	 * DBMS instance
	 */
	private final DBMS dbms;
	/**
	 * Timestamp counter
	 */
	private int timeStamp;
	
	/**
	 * Constructor
	 * @param dbms the DBMS
	 */
	public DBMSThread(DBMS dbms){
		transactions = new HashMap<Integer, Transaction> ();
		this.dbms = dbms;
		timeStamp = 0;
	}
	
	/**
	 * Creates a new transaction with id transactionId into the Thread Pool
	 * @param transactionId transaction identifier
	 * @throws OperationException if transaction already exists
	 */
	private void createTransaction(Integer transactionId) throws OperationException {
		if(!transactions.containsKey(transactionId)){
			Transaction Ti = new TransactionImpl(transactionId, ++timeStamp, this);
			transactions.put(transactionId, Ti);
			new Thread(Ti).start();
			
			Log.outputMsg(Ti.getTransactionName()+" was created. **");
			Log.outputMsg("ts("+Ti.getTransactionName()+")=" + Ti.getTimeStamp()+".");
		} else {
			throw new OperationException(transactions.get(transactionId).getTransactionName()+" already exists!");
		}
	}
	
	/**
	 * Appends the operation into the pending tasks of the transaction
	 * @param transactionId
	 * @param operation 
	 * @throws OperationException if transaction does not exist
	 */
	private void queueOperation(Integer transactionId, Operation operation) throws OperationException {
		if(transactions.containsKey(transactionId))
			transactions.get(transactionId).addOperation(operation);
		else 
			throw new OperationException("T"+transactionId+" does not exist!");
	}

	/**
	 * Process the file and simulates the behavior of the schedule provided
	 * @param file Path of the file
	 */
	public void processSchedule(String file) {

		try{
			try(BufferedReader br = new BufferedReader(new FileReader(file))) {
				//Read line
			    String line = br.readLine();
			    while (line != null) {
			    	
			    	//Parse & process line
			    	processOperation(ScheduleParser.parseLine(line));
			    	
			    	//intentioned delay
			    	try {
					    Thread.sleep(DELAY_BETWEEN_OPERATIONS);
					} catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
			    	
			    	//Read next line
			        line = br.readLine();
			    }
			    br.close();
			}
			catch(IOException e){ System.err.println(e.getMessage()); }
		}
		catch(OperationException e){ 
			System.err.println(e.getMessage()); 
			System.exit(0); 
		}
	}
	
	/**
	 * Process the operation
	 * @param operation
	 * @throws OperationException if either a transaction already exists or does not exist at all 
	 */
	private void processOperation(Operation operation) throws OperationException {
		switch(operation.getOperationType()){
		case BEGIN_TRANSACTION:
			createTransaction(operation.getTransactionId());
			break;
		case END_TRANSACTION:
		case READ_ITEM:
		case WRITE_ITEM:
			queueOperation(operation.getTransactionId(), operation);
			break;
		}	
	}
	
	
	/**
	 * @return the transactions (thread pool)
	 */
	public Map<Integer, Transaction> getTransactions(){
		return transactions;
	}
	
	/**
	 * @return the Buffer Pool 
	 */
	public BufferPool getBufferPool(){
		return dbms.getBufferPool();
	}
	
	
}
