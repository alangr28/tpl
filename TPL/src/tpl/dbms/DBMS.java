package tpl.dbms;

/**
 * Class of a DataBase Management System (DBMS) capable to simulate
 * the behavior of processing a schedule of transaction operations.
 * @author alangr28
 */
public class DBMS {
	
	private BufferPool bufferPool;
	
	/**
	 * Constructor.  
	 */
	public DBMS(){
		bufferPool = new BufferPool(this);
	}
	
	
	/**
	 * 
	 * @param file the file path
	 */
	public void processSchedule(String file){
		(new DBMSThread(this)).processSchedule(file);
			
	}
	
	/**
	 * @return the BufferPool
	 */
	public BufferPool getBufferPool(){
		return bufferPool;
	}


}
