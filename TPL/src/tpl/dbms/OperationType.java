package tpl.dbms;

/**
 * Types of transaction operations.
 * The possible operations are: begin transaction, read item, write item &amp; end transaction.
 * @author alangr28
 */
public enum OperationType {
	BEGIN_TRANSACTION, READ_ITEM, WRITE_ITEM, END_TRANSACTION;
}
