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

    private final List<ServiceClient> listeClient = new ArrayList<>();

    @Override
    public synchronized void enregistrerClient(ServiceClient var1) throws RemoteException {
        listeClient.add(var1);
    }

    @Override
    public synchronized List<ServiceClient> getClients() throws RemoteException {
        // On renvoie la liste, ou une copie
        return new ArrayList<>(listeClient);
    }
}
