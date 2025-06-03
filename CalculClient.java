import java.rmi.RemoteException;
import raytracer.Image;
import raytracer.Scene;

public class CalculClient implements ServiceClient {

    // Méthode pour calculer un fragment de l'image en utilisant la scène fournie.
    /**
     * Calcule un fragment de l'image en fonction des paramètres fournis.
     * 
     * @param scene   la scène à utiliser pour le calcul.
     * @param x       la coordonnée x du coin supérieur gauche du fragment.
     * @param y       la coordonnée y du coin supérieur gauche du fragment.
     * @param largeur la largeur du fragment.
     * @param hauteur la hauteur du fragment.
     * @return l'image calculée pour le fragment spécifié.
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    public Image calculer(Scene scene, int x, int y, int largeur, int hauteur) throws RemoteException {
        try {
            System.out.println("Calcul en cours du fragment de x : " + x + " y : " + y + " largeur : " + largeur
                    + " hauteur : " + hauteur);
            return scene.compute(x, y, largeur, hauteur);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Erreur de calcul", e);
        }
    }

    /**
     * Méthode de ping pour vérifier si le client est toujours actif.
     * 
     * @throws RemoteException si une erreur de communication RMI se produit.
     */

    public void ping() throws RemoteException {
        try {
            System.out.println("Ping reçu, le client est actif.");
        } catch (Exception e) {
            throw new RemoteException("Erreur lors du ping", e);
        }
    }
}