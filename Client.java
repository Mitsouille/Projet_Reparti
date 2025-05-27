import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Client{

    public static void main(String args[]){
        String serveur = "localhost";
        int port = 1099;
        
        try {
            Registry reg = LocateRegistry.getRegistry(serveur, port);
            
            ServiceServeur serveurD = (ServiceServeur) reg.lookup("Calculateur");

            CalculClient cc = new CalculClient();
            ServiceClient rd = (ServiceClient) UnicastRemoteObject.exportObject(cc, 0);
        
            serveurD.enregistrerClient(rd);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    
    
}
