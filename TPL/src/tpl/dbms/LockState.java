package tpl.dbms;

/**
 * Lock state of a database item.
 * @author alangr28
 */
public enum LockState {
	SHARE_LOCKED, EXCLUSIVE_LOCKED, UNLOCKED;
}
