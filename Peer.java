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

public class Peer implements PeerInterface{
    // instance variables
    Vector<String> allIPs = {};
    String leftIP; // will need to be modified for Syn Ring and Bully
    String rightIP;
    int numMessagesSent = 0
    long startTime;
    long endTime;

    Hashtable<String, RingInterface> neighborStubs;
    private ExecutorService pool;


    public Peer(int id){
        // constructor
        // based on id (and algorithm), sets up left and right ip's if needed (sync ring)
        pool = Executors.newFixedThreadPool(10);
    }


    public void receiveElection(){
        // in rmiregistry
        // schedule Runnable
        pool.execute(new ReceiveElection());
    }
    

    public void receiveReply(){
        // in rmiregistry
        pool.execute(new ReceiveReply());
    }


	
    public void sendElection(){
        numMessagesSent ++;
    }
    
    public void sendReply(){
        numMessagesSent ++;
    }

/*
    //optional, for bully algorithm: amLeader and sendAck
    public void sendCoordination(){}
    public void sendAck(){}
    // needs to be added to interface and registered
    public void receiveCoordination(){}
    public void receiveAck(){}
*/

    // connects to neighbors and fills in neighborStubs
    public boolean connect(String[] neighborIPs){
        // this is from assignmnet 2 so it will need to be changed more
        /*
        try{
        for (int i = 0; i < neighborIPs.length; i++){
            Registry registry = LocateRegistry.getRegistry(neighborIPs[i]);
            FileSharingInterface stub = (FileSharingInterface) registry.lookup(neighborIPs[i]);
            neighbors.put(neighborIPs[i],stub);
            System.out.println("connected to " + neighborIPs[i]+"!");
        }
    	return true;
        } catch (Exception e){
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
    	return false;
        }*/
        return false;
    }

    public static void main(String[] args){
        try{
            Scanner s = new Scanner(System.in);

            Peer p = new Peer(args[0]);
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(p, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(args[0], stub);

            String input = "help";
	        boolean connected = false;
            while (!(input.equals("quit"))){
                if (input.equals("help")){
                    System.out.println("Commands:\nhelp - show this message again\nconnect - connect to your neighbors"
                    +"\ne - elect a leader");
                }else if(input.equals("connect")){
                    connected = p.connect(ips);

                }else if(input.equals("e")){
        		    if (!connected){
        		    	System.out.println("Please try to connect again");
                    }
                    else{
        			//start leader election
                        // record start time
                        startTime = Systme.currentTimeMillis();
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
}

public class ReceiveElection implements Runnable{
    public ReceiveElection(){}

    @Override
    public void run(){}
}
public class ReceiveReply implements Runnable{
    public ReceiveReply(){}

    @Override
    public void run(){}
}