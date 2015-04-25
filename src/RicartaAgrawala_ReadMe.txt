/*******************************************************************/
Note : 
 - Run the program in the machines that are in same Sub-Network
/*******************************************************************/


Step 1 : Copy all java files to the required folder 

Step 2 : Compile all java files. Command : javac *.java

step 3 : Run program using the Command : java RicartAgravala <ProcessID> 
		 Process ID for each machine is unique and Integer ID.
		 
Step 4 : Any time in any machine to Enter Critical Section press Return(Enter) Key.

********************************************************************
sample Execution :
********************************************************************
Machine Name : medusa
java RicartAgravala 1

Machine Name : buddy
java RicartAgravala 2

Machine Name : doors
java RicartAgravala 3

Machine Name : yes
java RicartAgravala 4

Machine Name : kansas
java RicartAgravala 5

press return key in machine "doors",
then press return key in machine  "buddy",
then again press return key in machine "medusa".

********************************************************************
Messages Printed:
********************************************************************

****Machine Name : doors****
State : WANTED
OK message received from Process : 4
OK message received from Process : 1
OK message received from Process : 2
OK message received from Process : 5
Entering Critical Section !!
state : HELD
Adding Process 2 to Queue
state : HELD
Adding Process 1 to Queue
State : RELEASED

 
****Machine Name : buddy****
State : WANTED
OK message received from Process : 4
OK message received from Process : 5
OK message received from Process : 1
state : WANTED
Adding Process 1 to Queue
OK message received from Process : 3
Entering Critical Section !!
State : RELEASED


****Machine Name : medusa****
State : WANTED
OK message received from Process : 4
OK message received from Process : 5
OK message received from Process : 3
OK message received from Process : 2
Entering Critical Section !!
State : RELEASED