import java.rmi.Remote; 
import java.rmi.RemoteException; 

public interface PeerInterface extends Remote{
    public void receiveElection() throws RemoteException;
    public void receiveReply() throws RemoteException;
}