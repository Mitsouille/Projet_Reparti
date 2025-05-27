import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.time.Duration;
import java.time.Instant;
import raytracer.Disp;
import raytracer.Image;
import raytracer.Scene;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;



public class LancerRaytracer {

    public static String aide = "Raytracer : synthèse d'image par lancé de rayons (https://en.wikipedia.org/wiki/Ray_tracing_(graphics))\n\nUsage : java LancerRaytracer [fichier-scène] [largeur] [hauteur]\n\tfichier-scène : la description de la scène (par défaut simple.txt)\n\tlargeur : largeur de l'image calculée (par défaut 512)\n\thauteur : hauteur de l'image calculée (par défaut 512)\n";
     
    public static void main(String args[]){

        // Le fichier de description de la scène si pas fournie
        String fichier_description="simple.txt";

        // largeur et hauteur par défaut de l'image à reconstruire
        int largeur = 1048, hauteur = 1048;
        
        if(args.length > 0){
            fichier_description = args[0];
            if(args.length > 1){
                largeur = Integer.parseInt(args[1]);
                if(args.length > 2)
                    hauteur = Integer.parseInt(args[2]);
            }
        }else{
            System.out.println(aide);
        }
        
   
        // création d'une fenêtre 
        Disp disp = new Disp("Raytracer", largeur, hauteur);
        
        // Initialisation d'une scène depuis le modèle 
        Scene scene = new Scene(fichier_description, largeur, hauteur);
        
        // Calcul de l'image de la scène les paramètres : 
        // - x0 et y0 : correspondant au coin haut à gauche
        // - l et h : hauteur et largeur de l'image calculée
        // Ici on calcule toute l'image (0,0) -> (largeur, hauteur)
        
        int x0 = 0, y0 = 0;
        int l = largeur, h = hauteur;
                

        // On demande la liste des machines dispo 
        // On boucle est demande à chaque machine un nombre d'image en fonction du nombre de machines dispo

        String serv = "localhost";
        int port = 1099;
        
        try {
            int nbDecoupe = 100;
            int fragmentHeight = hauteur / nbDecoupe;

            // Obtenir les serveurs
            Registry reg = LocateRegistry.getRegistry(serv, port);
            ServiceServeur serveur = (ServiceServeur) reg.lookup("Calculateur");

            // Demander au serveur la liste des clients de calcul
            List<ServiceClient> clients = serveur.getClients();

            // Index partagé pour attribuer les lignes
            AtomicInteger currentIndex = new AtomicInteger(0);

            // Lancer un thread par serveur
            for (ServiceClient client : clients) {
                new Thread(() -> {
                    try {
                        while (true) {
                            int i = currentIndex.getAndIncrement();
                            if (i >= nbDecoupe) break;

                            int y = i * fragmentHeight;
                            int nh = (i == nbDecoupe - 1) ? h - y : fragmentHeight;

                            // Demander le calcul de l’image sur cette portion
                            Image imageFragment = client.calculer(scene, 0, y, l, nh);

                            // Afficher le résultat immédiatement
                            disp.setImage(imageFragment, 0, y);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }	
}
