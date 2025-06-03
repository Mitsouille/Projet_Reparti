import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import raytracer.Disp;
import raytracer.Image;
import raytracer.Scene;

public class LancerRaytracer {

    public static String aide = "Raytracer : synthèse d'image par lancé de rayons (https://en.wikipedia.org/wiki/Ray_tracing_(graphics))\n\n"
            + "Usage : java LancerRaytracer [fichier-scène] [largeur] [hauteur] [multi-threading] [nbDécoupe] [typeDécoupe]\n"
            + "\tfichier-scène : la description de la scène (par défaut simple.txt)\n"
            + "\tlargeur : largeur de l'image calculée (par défaut 512)\n"
            + "\thauteur : hauteur de l'image calculée (par défaut 512)\n"
            + "\tmulti-threading : true ; false pour séquentiel. Par défaut séquentiel\n"
            + "\tnbDécoupe : nombre de découpes (par défaut 100)\n"
            + "\ttypeDécoupe : ligne ou carre (par défaut ligne)";

    public static void main(String args[]) {

        // Le fichier de description de la scène si pas fournie
        String fichier_description = "simple.txt";

        // largeur et hauteur par défaut de l'image à reconstruire
        int largeur = 1048, hauteur = 1048;

        boolean thread = false;
        int nbDecoupe = 100;
        String typeDecoupe = "ligne";

        if (args.length > 0) {
            fichier_description = args[0];
            if (args.length > 1)
                largeur = Integer.parseInt(args[1]);
            if (args.length > 2)
                hauteur = Integer.parseInt(args[2]);
            if (args.length > 3)
                thread = Boolean.parseBoolean(args[3]);
            if (args.length > 4)
                nbDecoupe = Integer.parseInt(args[4]);
            if (args.length > 5)
                typeDecoupe = args[5];
        } else {
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
        // On boucle est demande à chaque machine un nombre d'image en fonction du
        // nombre de machines dispo

        String serv = "localhost";
        int port = 1099;

        try {
            Registry reg = LocateRegistry.getRegistry(serv, port);
            ServiceServeur serveur = (ServiceServeur) reg.lookup("Calculateur");

            // Attente tant qu'aucun client
            List<ServiceClient> clients = new ArrayList<>();
            while (clients.isEmpty()) {
                System.out.println("En attente de clients RMI...");
                Thread.sleep(1000);
                clients = serveur.getClients();
            }
            System.out.println("Clients connectés : " + clients.size());

            // Vérification de disponibilité effective
            List<ServiceClient> clientsActifs = new ArrayList<>();
            for (ServiceClient c : clients) {
                try {
                    c.ping();
                    clientsActifs.add(c);
                } catch (RemoteException e) {
                    System.out.println("Client inaccessible retiré : " + c);
                }
            }

            if (clientsActifs.isEmpty()) {
                System.out.println("Aucun client disponible.");
                return;
            }

            if (typeDecoupe.equals("ligne")) {
                int fragmentHeight = hauteur / nbDecoupe;
                if (thread)
                    calculerImageThread(clientsActifs, nbDecoupe, scene, disp, fragmentHeight, h, l);
                else
                    calculerImageSequentiel(clientsActifs, nbDecoupe, disp, scene, fragmentHeight, h, l);
            } else if (typeDecoupe.equals("carre")) {
                int nx = (int) Math.sqrt(nbDecoupe);
                int ny = (nbDecoupe + nx - 1) / nx;
                int fragW = largeur / nx;
                int fragH = hauteur / ny;

                if (thread)
                    calculerImageThreadCarre(clientsActifs, nx, ny, scene, disp, fragW, fragH, largeur, hauteur);
                else
                    calculerImageSequentielCarre(clientsActifs, nx, ny, disp, scene, fragW, fragH, largeur, hauteur);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Calcul de l'image en mode séquentiel pour les fragments en ligne
    /**
     * Calcule l'image en mode séquentiel en découpant l'image en fragments
     * horizontaux.
     *
     * @param sc             Liste des clients disponibles.
     * @param nbDecoupe      Nombre de découpes à effectuer.
     * @param disp           L'affichage où les fragments seront affichés.
     * @param scene          La scène à utiliser pour le calcul.
     * @param fragmentHeight Hauteur de chaque fragment.
     * @param h              Hauteur totale de l'image.
     * @param l              Largeur totale de l'image.
     * @throws RemoteException Si une erreur de communication RMI se produit.
     */
    public static void calculerImageSequentiel(List<ServiceClient> sc, int nbDecoupe, Disp disp, Scene scene,
            int fragmentHeight, int h, int l) throws RemoteException {
        // Copie de la liste
        List<ServiceClient> clients = new ArrayList<>(sc);

        // Pour chaque fragment
        for (int i = 0; i < nbDecoupe; i++) {
            int y = i * fragmentHeight;
            int nh = (i == nbDecoupe - 1) ? h - y : fragmentHeight;
            // Permet de savoir si on peut passer à la machine suivantex
            boolean frgCalcule = false;

            int startClient = i % clients.size();
            int tentative = 0;
            // Le nb de tentative est égal au nombre de client
            // On boucle sur tout les clients si l'un échoue, histoire que le morceau soit
            // calculé par un autre
            while (tentative < clients.size() && !frgCalcule) {
                int indexClient = (startClient + tentative) % clients.size();
                ServiceClient client = clients.get(indexClient);

                try {
                    Image imageFragment = client.calculer(scene, 0, y, l, nh);
                    disp.setImage(imageFragment, 0, y);
                    // Si tout va bien on passe au suivant
                    frgCalcule = true;

                } catch (RemoteException e) {
                    // Sinon on le retire de la liste histoire de ne pas perdre de temps à chaque
                    // fois
                    System.out.println("Le client à échouer, on l'enlève de la liste des clients");
                    clients.remove(indexClient);
                    continue;
                }
                tentative++;
            }

            if (!frgCalcule) {
                System.out.println("Le fragment " + i + " n'a pas été calculé");
            }
        }
    }

    // Calcul de l'image en mode multithread pour les fragments en ligne
    /**
     * Calcule l'image en mode multithread en découpant l'image en fragments
     * horizontaux.
     *
     * @param clientsList    Liste des clients disponibles.
     * @param nbDecoupe      Nombre de découpes à effectuer.
     * @param scene          La scène à utiliser pour le calcul.
     * @param disp           L'affichage où les fragments seront affichés.
     * @param fragmentHeight Hauteur de chaque fragment.
     * @param h              Hauteur totale de l'image.
     * @param l              Largeur totale de l'image.
     */
    public static void calculerImageThread(List<ServiceClient> clientsList, int nbDecoupe, Scene scene, Disp disp,
            int fragmentHeight, int h, int l) {
        // Index partagé pour attribuer les lignes
        AtomicInteger currentIndex = new AtomicInteger(0);
        List<ServiceClient> clients = Collections.synchronizedList(new ArrayList<>(clientsList));
        List<Thread> threads = new ArrayList<>();

        // Lancer un thread par serveur
        for (ServiceClient client : clientsList) {
            Thread t = new Thread(() -> {
                while (true) {
                    int i = currentIndex.getAndIncrement();
                    if (i >= nbDecoupe)
                        break;

                    int y = i * fragmentHeight;
                    int nh = (i == nbDecoupe - 1) ? h - y : fragmentHeight;

                    try {
                        Image imageFragment = client.calculer(scene, 0, y, l, nh);
                        disp.setImage(imageFragment, 0, y);
                    } catch (RemoteException e) {
                        System.out.println("Client déconnecté (thread ligne), on le retire.");
                        synchronized (clients) {
                            clients.remove(client);
                        }
                        break;
                    }
                }
            });
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Calcul de l'image en mode séquentiel pour les fragments carrés
    /**
     * Calcule l'image en mode séquentiel en découpant l'image en fragments carrés.
     *
     * @param clients Liste des clients disponibles.
     * @param nx      Nombre de fragments en largeur.
     * @param ny      Nombre de fragments en hauteur.
     * @param disp    L'affichage où les fragments seront affichés.
     * @param scene   La scène à utiliser pour le calcul.
     * @param fragW   Largeur de chaque fragment.
     * @param fragH   Hauteur de chaque fragment.
     * @param largeur Largeur totale de l'image.
     * @param hauteur Hauteur totale de l'image.
     * @throws RemoteException Si une erreur de communication RMI se produit.
     */
    public static void calculerImageSequentielCarre(List<ServiceClient> clients, int nx, int ny, Disp disp, Scene scene,
            int fragW, int fragH, int largeur, int hauteur) throws RemoteException {
        // Pour chaque fragment carré
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                int x = i * fragW;
                int y = j * fragH;
                int w = (i == nx - 1) ? largeur - x : fragW;
                int h = (j == ny - 1) ? hauteur - y : fragH;

                boolean frgCalcule = false;
                int startClient = (i + j) % clients.size();
                int tentative = 0;

                while (tentative < clients.size() && !frgCalcule) {
                    int indexClient = (startClient + tentative) % clients.size();
                    ServiceClient client = clients.get(indexClient);

                    try {
                        Image imageFragment = client.calculer(scene, x, y, w, h);
                        disp.setImage(imageFragment, x, y);
                        frgCalcule = true;
                    } catch (RemoteException e) {
                        System.out.println("Client RMI déconnecté (carre), on ignore : " + client);
                    }
                    tentative++;
                }
            }
        }
    }

    // Calcul de l'image en mode multithread pour les fragments carrés
    /**
     * Calcule l'image en mode multithread en découpant l'image en fragments carrés.
     *
     * @param clientsList Liste des clients disponibles.
     * @param nx          Nombre de fragments en largeur.
     * @param ny          Nombre de fragments en hauteur.
     * @param scene       La scène à utiliser pour le calcul.
     * @param disp        L'affichage où les fragments seront affichés.
     * @param fragW       Largeur de chaque fragment.
     * @param fragH       Hauteur de chaque fragment.
     * @param largeur     Largeur totale de l'image.
     * @param hauteur     Hauteur totale de l'image.
     */
    public static void calculerImageThreadCarre(List<ServiceClient> clientsList, int nx, int ny, Scene scene, Disp disp,
            int fragW, int fragH, int largeur, int hauteur) {
        AtomicInteger currentIndex = new AtomicInteger(0);
        int total = nx * ny;
        List<ServiceClient> clients = Collections.synchronizedList(new ArrayList<>(clientsList));
        List<Thread> threads = new ArrayList<>();

        for (ServiceClient client : clientsList) {
            Thread t = new Thread(() -> {
                while (true) {
                    int i = currentIndex.getAndIncrement();
                    if (i >= total)
                        break;

                    int x = (i % nx) * fragW;
                    int y = (i / nx) * fragH;
                    int w = (i % nx == nx - 1) ? largeur - x : fragW;
                    int h = (i / nx == ny - 1) ? hauteur - y : fragH;

                    try {
                        Image imageFragment = client.calculer(scene, x, y, w, h);
                        disp.setImage(imageFragment, x, y);
                    } catch (RemoteException e) {
                        System.out.println("Client déconnecté (thread carre), on le retire.");
                        synchronized (clients) {
                            clients.remove(client);
                        }
                        break;
                    }
                }
            });
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
