import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.time.Duration;
import java.time.Instant;
import raytracer.Disp;
import raytracer.Image;
import raytracer.Scene;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CalculServeur implements ServiceServeur {

    private List<ServiceClient> listeClient = new ArrayList<>();

    @Override
    public synchronized void enregistrerClient(ServiceClient var1) throws RemoteException {
        listeClient.add(var1);
    }

    @Override
    public synchronized List<ServiceClient> getClients() throws RemoteException {
        List<ServiceClient> clientsValides = new ArrayList<>();
        for (ServiceClient c : listeClient) {
            try {
                c.ping();
                clientsValides.add(c);
            } catch (RemoteException e) {
                System.out.println("Client déconnecté détecté, on le retire de la liste.");
            }
        }

        this.listeClient = clientsValides;

        return new ArrayList<>(clientsValides);
    }

}
