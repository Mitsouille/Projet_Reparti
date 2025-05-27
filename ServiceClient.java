import java.time.Duration;
import java.time.Instant;
import raytracer.Disp;
import raytracer.Image;
import raytracer.Scene;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface ServiceClient extends Remote{
    Image calculer(Scene scene, int x, int y, int largeur, int hauteur) throws RemoteException;
}