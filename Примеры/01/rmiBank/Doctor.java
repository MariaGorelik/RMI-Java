package rmiDoctor;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.List;

/**
 * This class is a placeholder that simply contains other classes and 
 * for interfaces remote banking.
 **/
public class Doctor {
    /**
     * This is the interface that defines the exported methods of the 
     * bank server.
     **/
    public interface RemoteDoctor extends Remote {
        /** Open a new account, with the specified name and password */
        public void connect(String surname, String name, String patronymic)
	    throws RemoteException, RecordException;
	
        /** Close the named account */
        void disconnect(String surname, String name, String patronymic)
	    throws RemoteException, RecordException;
	
        /** Deposit money into the named account */
        public void record(String surname, String name, String patronymic, String day,
                           String time, String phone, String complaint)
	    throws RemoteException, RecordException;

        String options()
        throws RemoteException, RecordException;

//        /** Withdraw the specified amount of money from the named account */
//        public FunnyMoney withdraw(String name, String password, int amount)
//	    throws RemoteException, BankingException;
	
        /** Return the amount of money in the named account */
        public String checkRecord(String surname, String name, String patronymic)
	    throws RemoteException, RecordException;
	
        /** 
	 * Return a List of Strings  list the transaction history
	 * of the named account 
	 **/
//        public List<String> getTransactionHistory(String name, String password)
//	    throws RemoteException, BankingException;
    }
    
    /**
     * This simple class represents a monetary amount.  This implementation
     * is really nothing more than a wrapper around an integer.  It is a useful
     * to demonstrate that RMI can accept arbitrary non-String objects as
     * arguments and return them as values, as long as they are Serializable.
     * A more complete implementation of this FunnyMoney class might bear
     * a serial number, a digital signature, and other security features to 
     * ensure that it is unique and non-forgeable.
     **/
//    public static class FunnyMoney implements java.io.Serializable {
//		/**
//		 *
//		 */
//		private static final long serialVersionUID = 1L;
//		public int amount;
//        public FunnyMoney(int amount) { this.amount = amount; }
//    }
    
    /**
     * This is a type of exception used to represent exceptional conditions
     * related to banking, such as "Insufficient Funds" and  "Invalid Password"
     **/
    public static class RecordException extends Exception {
		private static final long serialVersionUID = 1L;
		public RecordException(String msg) { super(msg); }
    }
    
    /**
     * This class is a simple stand-alone client program that interacts
     * with a RemoteBank server.  It invokes different RemoteBank methods
     * depending on its command-line arguments, and demonstrates just how
     * simple it is to interact with a server using RMI.
     **/
    public static class Client {
        public static void main(String[] args) {
            try {
                // Figure out what RemoteBank to connect to by reading a system
                // property (specified on the command line with a -D option to
                // java) or, if it is not defined, use a default URL.  Note
                // that by default this client tries to connect to a server on
                // the local machine
                String ip = "192.168.11.8";
                Registry registry = LocateRegistry.getRegistry(ip, 8080);
                String url = "doc";
                //String url = System.getProperty("doctor", "rmi:///FirstRemote");

                // Now look up that RemoteBank server using the Naming object,
                // which contacts the rmiregistry server.  Given the url, this
                // call returns a RemoteBank object whose methods may be
                // invoked remotely
                RemoteDoctor doctor = (RemoteDoctor) registry.lookup(url);
                
                // Convert the user's command to lower case
                String cmd = args[0].toLowerCase();
		
                // Now, go test the command against a bunch of possible options
                if (cmd.equals("connect")) {           // Open an account
                    doctor.connect(args[1], args[2], args[3]);
                    System.out.println("User connected.");
                }
                else if (cmd.equals("disconnect")) {     // Close an account
                    doctor.disconnect(args[1], args[2], args[3]);
                    System.out.println("Goodbye!");
                } else if (cmd.equals("options"))
                {
                    String opt = doctor.options();
                    System.out.println(opt);
                }
                else if (cmd.equals("record")) {   // Deposit money
                    doctor.record(args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
                    System.out.println("You have successfully registered");
                }
//                else if (cmd.equals("withdraw")) {  // Withdraw money
//                    FunnyMoney money = bank.withdraw(args[1], args[2],
//						    Integer.parseInt(args[3]));
//                    System.out.println("Withdrew " + money.amount +
//				       " wooden nickels.");
//                }
                else if (cmd.equals("checkrecord")) {   // Check account balance
                    String rec = doctor.checkRecord(args[1], args[2], args[3]);
                    System.out.println("Your record:\n");
                    System.out.println(rec + "\n");
                }
//                else if (cmd.equals("history")) {   // Get transaction history
//                    List<String> transactions =
//                    		bank.getTransactionHistory(args[1], args[2]);
//                    for(int i = 0; i < transactions.size(); i++)
//                        System.out.println(transactions.get(i));
//                }
                else System.out.println("Unknown command");
            }
            // Catch and display RMI exceptions
            catch (RemoteException e) { System.err.println(e); }
            // Catch and display Banking related exceptions
            catch (RecordException e) { System.err.println(e.getMessage()); }
            // Other exceptions are probably user syntax errors, so show usage.
            catch (Exception e) { 
                System.err.println(e);
                System.err.println("Usage: java [-Ddoctor=<url>] Doctor$Client " +
				   "<cmd> <surname> <name> <patronymic> [<other>]");
                System.err.println("where cmd is: connect, disconnect, options, record, checkrecord");
            }
        }
    }
}
