import java.rmi.RemoteException;
import java.rmi.Remote;
import java.time.Duration;
import java.time.Instant;
import raytracer.Disp;
import raytracer.Image;
import raytracer.Scene;


public class CalculClient implements ServiceClient{
    
    public Image calculer(Scene scene, int x, int y, int largeur, int hauteur) throws RemoteException {
        try {
            return scene.compute(x, y, largeur, hauteur);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Erreur de calcul", e);
        }
    }
}