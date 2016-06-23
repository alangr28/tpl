package tpl.dbms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Buffer Pool Class. It simulates to attend Database I/O Requests.
 * All persistent database data is staged through the DBMS buffer pool.
 * @author alangr28
 */
public class BufferPool {
	
	/**
	 * Set of items 
	 */
	private Map<String, Item> items;
	
	/**
	 * The DBMS 
	 */
	private DBMS dbms;
	
	/**
	 * Items available in the Database. Item names are single letters from A to Z
	 */
	public static final char[] ITEMS_KEYS = new String("ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
	
	/**
	 * @param dbms the DBMS
	 */
	public BufferPool(DBMS dbms){
		// Items from A to Z
		items = new ConcurrentHashMap<String, Item>();
		
		String key;
		Item item;
		for(int i = 0; i< ITEMS_KEYS.length; i++){
			key = Character.toString(ITEMS_KEYS[i]);
			item = new ItemImpl(key.toUpperCase(), key.toLowerCase());
			items.put(key, item);
		}
		this.dbms = dbms;
	}
	
	/**
	 * @param key the item key
	 * @return the item
	 */
	public Item getItem(String key){
		Item item = null;
		synchronized(items){
			if(items.containsKey(key))
				item = items.get(key);
		}
		return item;
	}
	
	/**
	 * @return the DBMS
	 */
	public DBMS getDBMS(){
		return dbms;
	}
	
}
