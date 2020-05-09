import java.rmi.Remote; 
import java.rmi.RemoteException; 

public interface PeerInterface extends Remote{
    public void receiveElection(int senderID) throws RemoteException;
    public void receiveReply() throws RemoteException;
    public void receiveCoordination(int senderID) throws RemoteException;
}