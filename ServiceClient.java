import java.time.Duration;
import java.time.Instant;
import raytracer.Disp;
import raytracer.Image;
import raytracer.Scene;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface ServiceClient extends Remote {
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
    Image calculer(Scene scene, int x, int y, int largeur, int hauteur) throws RemoteException;

    // Méthode de ping pour vérifier si le client est toujours actif.
    /**
     * Méthode de ping pour vérifier si le client est toujours actif.
     * 
     * @throws RemoteException si une erreur de communication RMI se produit.
     */
    public void ping() throws RemoteException;
}