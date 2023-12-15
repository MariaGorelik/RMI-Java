package rmiDoctor;

import java.rmi.*;
//import java.rmi.registry.LocateRegistry;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;

/**
 * This class implements the remote methods defined by the RemoteBank
 * interface.  It has a serious shortcoming, though: all account data is
 * lost when the server goes down.
 **/
public class RemoteDoctorServer extends UnicastRemoteObject implements Doctor.RemoteDoctor
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5298657303149865024L;

	/** 
     * This nested class stores data for a single account with the bank 
     **/
    class Account {
        String userFullName;
        String day = null;
        String time = null;
        String phone = null;
        String complaint = null;

        //String password;                      // account password
        //int balance;                          // account balance
        //List<String> transactions = new ArrayList<String>();  // account transaction history
        Account(String surname, String name, String patronymic) {
            this.userFullName = surname + name + patronymic;
            //transactions.add("Account opened at " + new Date());
        }
    }
    
    /** 
     * This hashtable stores all open accounts and maps from account name
     * to Account object
     **/
    Map<String,Account> accounts = new HashMap<String,Account>();

    private static int MAX_USERS = 100;

    private static final int DAYS_SIZE = 6;
    private static final int TIMES_SIZE = 9;

    static TreeMap<String,Integer> daysToInt = new TreeMap<String,Integer>();
    static {
        daysToInt.put("Monday", 0);
        daysToInt.put("Tuesday", 1);
        daysToInt.put("Wednesday", 2);
        daysToInt.put("Thursday", 3);
        daysToInt.put("Friday", 4);
        daysToInt.put("Saturday", 5);
    }

    static String[] daysToStr = new String[DAYS_SIZE];
    static {
        daysToStr[0] = "Monday";
        daysToStr[1] = "Tuesday";
        daysToStr[2] = "Wednesday";
        daysToStr[3] = "Thursday";
        daysToStr[4] = "Friday";
        daysToStr[5] = "Saturday";
    }

    static TreeMap<String,Integer> timesToInt = new TreeMap<String,Integer>();
    static {
        timesToInt.put("10.00", 0);
        timesToInt.put("11.00", 1);
        timesToInt.put("12.00", 2);
        timesToInt.put("13.00", 3);
        timesToInt.put("14.00", 4);
        timesToInt.put("15.00", 5);
        timesToInt.put("16.00", 6);
        timesToInt.put("17.00", 7);
        timesToInt.put("18.00", 8);
    }

    static String[] timesToStr = new String[TIMES_SIZE];
    static {
        timesToStr[0] = "10.00";
        timesToStr[1] = "11.00";
        timesToStr[2] ="12.00";
        timesToStr[3] = "13.00";
        timesToStr[4] = "14.00";
        timesToStr[5] = "15.00";
        timesToStr[6] ="16.00";
        timesToStr[7] = "17.00";
        timesToStr[8] = "18.00";
    }

    static Account records[][] = new Account[DAYS_SIZE][TIMES_SIZE];
    static
    {
        for (int i = 0; i < DAYS_SIZE; i++)
        {
            for (int j = 0; j < TIMES_SIZE; j++)
            {
                records[i][j] = null;
            }
        }
    }
    
    /**
     * This constructor doesn't do anything, but because the superclass 
     * constructor throws an exception, the exception must be declared here
     **/
    public RemoteDoctorServer() throws RemoteException { super(); }
    
    /** 
     * Open a bank account with the specified name and password 
     * This method is synchronized to make it thread safe, since it 
     * manipulates the accounts hashtable.
     **/
    public synchronized void connect(String surname, String name, String patronymic)
	throws RemoteException, Doctor.RecordException
    {
        // Check if there is already an account under that name
        if (accounts.get(surname + name + patronymic) != null)
            throw new Doctor.RecordException("Account already exists.");
        // Otherwise, it doesn't exist, so create it.
        Account acct = new Account(surname, name, patronymic);
        // And register it
        accounts.put(acct.userFullName, acct);
        System.out.print("Patient " + acct.userFullName + " is registered");
    }
    
    /**
     * This internal method is not a remote method.  Given a name and password
     * it checks to see if an account with that name and password exists.  If
     * so, it returns the Account object.  Otherwise, it throws an exception.
     **/
    Account verify(String surname, String name, String patronymic) throws Doctor.RecordException {
        synchronized(accounts) {
            Account acct = (Account)accounts.get(surname + name + patronymic);
            if (acct == null) throw new Doctor.RecordException("No such account");
            return acct;
        }
    }
    
    /** 
     * Close the named account.  This method is synchronized to make it 
     * thread safe, since it manipulates the accounts hashtable.
     **/
    public synchronized void disconnect(String surname, String name, String patronymic)
	throws RemoteException, Doctor.RecordException
    {
        Account acct;
        acct = verify(surname, name, patronymic);
        accounts.remove(acct.userFullName);
        System.out.print("Patient " + acct.userFullName + " disconnected");
        // Before changing the balance or transactions of any account, we first
        // have to obtain a lock on that account to be thread safe.
        synchronized (acct) {
            if (acct.day != null && acct.time != null)
            {
                records[daysToInt.get(acct.day)][timesToInt.get(acct.time)] = null;
            }
        }
    }

    public static boolean isValidDay(String d)
    {
        if (d.equals("Monday") || d.equals("Tuesday") || d.equals("Wednesday") || d.equals("Thursday") ||
                d.equals("Friday") || d.equals("Saturday"))
        {
            return true;
        }
        return false;
    }

    public static boolean isValidTime(String t)
    {
        if (t.equals("10.00") || t.equals("11.00") || t.equals("12.00") || t.equals("13.00") ||
                t.equals("14.00") || t.equals("15.00") || t.equals("16.00") || t.equals("17.00") || t.equals("18.00"))
        {
            return true;
        }
        return false;
    }
    
    /** Deposit the specified FunnyMoney to the named account */
    public void record(String surname, String name, String patronymic, String day,
                       String time, String phone, String complaint)
	throws RemoteException, Doctor.RecordException
    {
        Account acct = verify(surname, name, patronymic);
        synchronized(acct) {
            if (!isValidDay(day) || !isValidTime(time))
            {
                throw new Doctor.RecordException("Incorrect day or(and) time type");
            }
            if (records[daysToInt.get(day)][timesToInt.get(time)] != null)
            {
                throw new Doctor.RecordException("This time is not available, try another option");
            }
            acct.day = day;
            acct.time = time;
            acct.complaint = complaint;
            acct.phone = phone;
            records[daysToInt.get(day)][timesToInt.get(time)] = acct;
            System.out.print("Patient " + acct.userFullName + " registered:");
            System.out.print("Day: " + acct.day + " time: " + acct.time + " phone: " + acct.phone);
            System.out.print("Complaint: " + acct.complaint);
        }
    }
    
    /** Withdraw the specified amount from the named account */
//    public Doctor.FunnyMoney withdraw(String name, String password, int amount)
//	throws RemoteException, Doctor.RecordException
//    {
//        Account acct = verify(name, password);
//        synchronized(acct) {
//            if (acct.balance < amount)
//                throw new Doctor.RecordException("Insufficient Funds");
//            acct.balance -= amount;
//            acct.transactions.add("Withdrew " + amount + " on "+new Date());
//            return new Doctor.FunnyMoney(amount);
//        }
//    }
    
    /** Return the current balance in the named account */
    public String checkRecord(String surname, String name, String patronymic)
	throws RemoteException, Doctor.RecordException
    {
        Account acct = verify(surname, name, patronymic);
        synchronized(acct)
        {
            String rec = "Day : " + acct.day + "\n"
                    + "Time : " + acct.time + "\n"
                    + "Phone : " + acct.phone + "\n"
                    + "Complaint : " + acct.complaint + "\n";
            return rec;
        }
    }

    public String options()
    {
        Vector<String> opt = new Vector<String>();
        for (int i = 0; i < DAYS_SIZE; i++)
        {
            for (int j = 0; j < TIMES_SIZE; j++)
            {
                if (records[i][j] == null)
                {
                    opt.add(daysToStr[i] + " " + timesToStr[j]);
                }
            }
        }
        StringBuilder optlist = new StringBuilder();
        for (String str: opt) {
            optlist.append("\n").append(str);
        }
        String strlist = optlist.toString();
        return strlist;
    }
    
    /** 
     * Return a Vector of strings containing the transaction history
     * for the named account
     **/
//    public List<String> getTransactionHistory(String name, String password)
//	throws RemoteException, Doctor.RecordException
//    {
//        Account acct = verify(name, password);
//        synchronized(acct) { return acct.transactions; }
//    }
    
    /**
     * The main program that runs this RemoteBankServer.
     * Create a RemoteBankServer object and give it a name in the registry.
     * Read a system property to determine the name, but use "FirstRemote"
     * as the default name.  This is all that is necessary to set up the
     * service.  RMI takes care of the rest.
     **/
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "192.168.11.151"); // added
            // Create a bank server object
            RemoteDoctorServer doctor = new RemoteDoctorServer();
            Registry registry = LocateRegistry.createRegistry(8080);
            // Figure out what to name it
            //String name = System.getProperty("doctorname", "FirstRemote");
            String name = "doc";
            registry.rebind(name, doctor);
            // Name it that
            //Naming.rebind(name, doctor);
            // Tell the world we're up and running
            System.out.println(name + " is open and ready for patients.");
        }
        catch (Exception e) {
            System.err.println(e);
            System.err.println("Usage: java [-Ddoctorname=<name>] " +
		            "RemoteDoctorServer");
            System.exit(1); // Force exit because there may be RMI threads
        }
    }
}
