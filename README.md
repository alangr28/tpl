# Two Phase Locking implementation in Java
This is a project of my Database Management Systems class. We simulate, particularly, the **Rigorous Two Phase Locking Protocol  (SS2PL)** with the Wait-Die method for dealing with deadlock.

### Description

The input to this program is a file of transaction operations in a particular sequence (a schedule). Each line has a single transaction operation. The possible operations are: b (`begin transaction`), r (`read item`), w (`write item`), and e (`end transaction`). Each operation is followed by a transaction id that is an integer between 1 and 99. For r and w operations, an item name follows between parentheses (item names are single letters from A to Z). An example is given below.

*Example*:
`b1;
r1(Z); 
b2; 
r1(Y); 
w1(Z);
r1(Z);
w1(Z);
b3; 
r2(Y);
e2; 
w1(Y);
r3(Z); 
e1; 
w3(Z); 
e3;`

The output is a log of the most important actions taken by the program when processing each operation (including changes to the transaction table and lock table).

### Internal links: 
* [Main class] (TPL/src/tpl/Main.java)
* [Input samples] (TPL/src/tpl/samples/input)
* [Output samples] (TPL/src/tpl/samples/output)

### Ideas about:
* Use java threads for each transaction.
* Producer-consumer methodology:
  * Producer: Main program. It parses lines and recognizes operations. For `begin transaction` operations, it starts a new thread.  `read item`, `write item` & `end transaction` operations are redirected to ther corresponding transaction (thread).
  * Consumer: Transaction. It has a list of pending operations to perform.
* Sleep a transaction if it does not have pending operations or if it is waiting for an item.
* Use references: Elmasri, Ramez and Sham Navathe. Fundamentals Of Database Systems (7th Edition).
