import java.rmi.RemoteException;
import java.rmi.Remote;
import java.time.Duration;
import java.time.Instant;
import raytracer.Disp;
import raytracer.Image;
import raytracer.Scene;
import java.util.List;

public interface ServiceServeur extends Remote {
    void enregistrerClient(ServiceClient client) throws RemoteException;
    List<ServiceClient> getClients() throws RemoteException;
}
