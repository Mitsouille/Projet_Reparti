import java.rmi.RemoteException;
import raytracer.Image;
import raytracer.Scene;


public class CalculClient implements ServiceClient{
    
    public Image calculer(Scene scene, int x, int y, int largeur, int hauteur) throws RemoteException {
        try {
            System.out.println("Calcul en cours du fragment de x : " + x + " y : " + y + " largeur : " + largeur + " hauteur : " + hauteur);
            return scene.compute(x, y, largeur, hauteur);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Erreur de calcul", e);
        }
    }
}