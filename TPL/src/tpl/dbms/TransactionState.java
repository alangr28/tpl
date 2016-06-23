package tpl.dbms;

/**
 * States of a transaction.
 * @author alangr28
 */
public enum TransactionState {
	ACTIVE, WAITING, PARTIALLY_COMMITED, COMMITTED, FAILED, ABORTED;
}
