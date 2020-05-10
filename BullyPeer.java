import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import java.util.*;
import java.util.concurrent.*;


public class BullyPeer implements PeerInterface{
    // instance variables

    String[] allIPs = {"18.206.215.175", "54.211.147.15", "52.207.243.243", "54.198.9.88", "54.224.88.131"}; // THIS NEEDS TO BE SET, and it's the same for all nodes

    int myID;
    // information about the leader
    int leaderID;
    boolean amILeader = false;
    boolean haveDeclaredMyself = false;
    boolean haveStartedElection = false;
    //record message metrics
    int numMessagesSent = 0;
    // for measuring time taken from initiation to final coordination message
    long startTime;
    long endTime;
    // ready-available stubs for neighbors, to be filled in during connect method
    Hashtable<Integer, PeerInterface> neighborStubs;
    // used for multithreading
    private ExecutorService pool;

    boolean hasReceivedReply;

    //Hashtable<Integer, ReplyStatus> responseTracker; 


    public BullyPeer(int id){
        // constructor
        myID = id;
        pool = Executors.newFixedThreadPool(10);
        neighborStubs = new Hashtable<Integer, PeerInterface>();
        // set up the responseTracker
        /*
        if (myID != allIPs.length - 1){ // make sure I'm not the highest node
            for (int i = myID + 1; i < allIPs.length; i++){
                responseTracker.put(i, new ReplyStatus()); // add to responseTracker
            }
        }
        */
        hasReceivedReply = false;

    }


    public void receiveElection(int senderID){pool.execute(new ReceiveElection(senderID));}
    
    public void receiveReply(){pool.execute(new ReceiveReply());}

    public void receiveCoordination(int senderID){pool.execute(new ReceiveCoordination(senderID));}


    public void sendElection(){
        // first check if I have the highest ID
        if (myID == allIPs.length - 1){
            // I am the leader
            amILeader = true;
            leaderID = myID;
            // send out coordination
            synchronized(this){
                if (!haveDeclaredMyself){ // make sure I only send out one coordination message
                    haveDeclaredMyself = true;
                    sendCoordination();
                }
            }   
        }else{
            // send election message to all nodes with a higher ID
            try{
                hasReceivedReply = false;
                for (int i = myID + 1; i < allIPs.length; i++){
                    System.out.println("Sending an election message to Peer " + i);
                    neighborStubs.get(i).receiveElection(myID);
                    // add them to waiting list
                    //responseTracker.get(i).startWaiting(10000); // waiting time set to 10 seconds
                    numMessagesSent ++;
                }
                // wait for 10 seconds then check if Peer has heard back from anyone
                Thread.sleep(10000);
                if (!hasReceivedReply){
                    // if not, I am leader and send out coordination message
                    amILeader = true;
                    leaderID = myID;
                    synchronized(this){
                        if (!haveDeclaredMyself){ // make sure I only send out one coordination message
                            haveDeclaredMyself = true;
                            sendCoordination();
                        }
                    }
                }

            }catch(Exception e){
                System.err.println("Peer exception: " + e.toString());
                e.printStackTrace();
            }
        } 
    }
    
    public void sendReply(int destID){
        // 
        try{
            System.out.println("Replying to Peer " + destID);
            neighborStubs.get(destID).receiveReply();
            numMessagesSent ++;
        }catch(Exception e){
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }
        
    }

    public void sendCoordination(){
        try{
            for (int i = 0; i < allIPs.length; i++){
                if (i != myID){ //send message to all neighbors but myself
                    System.out.println("Telling Peer " + i + " that I am the leader");
                    neighborStubs.get(i).receiveCoordination(myID);
                    numMessagesSent ++;
                }
            }
            haveDeclaredMyself = true;
        }catch(Exception e){
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }
            
    }

    


    // connects to neighbors and fills in neighborStubs
    public boolean connect(String[] neighborIPs){
        try{
            for (int i = 0; i < neighborIPs.length; i++){
                if (i != myID){ //connect to all but myself
                    Registry registry = LocateRegistry.getRegistry(neighborIPs[i]);
                    PeerInterface stub = (PeerInterface) registry.lookup("Peer");
                    neighborStubs.put(i, stub);
                    System.out.println("connected to " + neighborIPs[i]+"!");
                }
            }
        	return true;
        } catch (Exception e){
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
    	    return false;
        }
    }

    public static void main(String[] args){
        try{
            Scanner s = new Scanner(System.in);

            BullyPeer p = new BullyPeer(Integer.parseInt(args[0]));
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(p, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Peer", stub);

            String input = "help";
	        boolean connected = false;
            while (!(input.equals("quit"))){
                if (input.equals("help")){
                    System.out.println("Commands:\nhelp - show this message again\nconnect - connect to your neighbors"
                    +"\ne - elect a leader\nr - de-elect leader (all peers need to do this!)");
                }else if(input.equals("connect")){
                    connected = p.connect(p.allIPs);
                }else if (input.equals("r")){
                    // reset the variables
                    p.numMessagesSent = 0;
                    p.amILeader = false;
                    p.leaderID = Integer.MIN_VALUE;
                    p.haveDeclaredMyself = false;
                    p.haveStartedElection = false;
                    p.hasReceivedReply = false;

                }else if(input.equals("e")){
        		    if (!connected){
        		    	System.out.println("Please try to connect again");
                    }
                    else{
        			//start leader election
                        // record start time
                        p.startTime = System.currentTimeMillis();
                        System.out.println("Start time: " + p.startTime);
                        synchronized(p){
                            if (!(p.haveStartedElection)){ // make sure I only send out one election message
                                p.haveStartedElection = true;
                                p.sendElection();
                            }
                        }   
                    }
                }
                else{ System.out.println("Sorry, I don't understand that");}
                System.out.print(">>>");
                input = s.nextLine().trim();
            }

            if(input.equals("quit")){
                s.close();
		        System.exit(0);
            }


        }
        catch(Exception e){
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public class ReceiveElection implements Runnable{
        int senderID;
        public ReceiveElection(int senderID){
            this.senderID = senderID;
        }

        @Override
        public void run(){
            // if I have a higher # than the sender, send Reply and begin an election
            if (myID > senderID){
                sendReply(senderID);
                synchronized(this){
                    if (!haveStartedElection){ // make sure I only send out one election message
                        haveStartedElection = true;
                        sendElection();
                    }
                }  
            }
        }
    }
    public class ReceiveReply implements Runnable{
        public ReceiveReply(){}

        @Override
        public void run(){
            // update responseTracker
            //responseTracker.get(senderID).setDone();
            // give up election 
            hasReceivedReply = true;
        }
    }
    public class ReceiveCoordination implements Runnable{
        int senderID;

        public ReceiveCoordination(int senderID){
            this.senderID = senderID;
        }

        @Override
        public void run(){
            leaderID = senderID;
            amILeader = false;
            System.out.println("Peer " + leaderID + " is the leader now.");
            endTime = System.currentTimeMillis();
            System.out.println("End time: " + endTime);
            System.out.println("Number of messages sent: " + numMessagesSent);
        }
    }
}