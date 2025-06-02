import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Serveur {
    public static void main(String[] args){
        try{
            CalculServeur cs = new CalculServeur();
            ServiceServeur rd = (ServiceServeur) UnicastRemoteObject.exportObject(cs, 0);
            int defaultPort = 1099;
            Registry reg = LocateRegistry.createRegistry(defaultPort);
            //Registry reg = LocateRegistry.getRegistry(1099);
            reg.rebind("Calculateur", rd);
            System.out.println("Serveur lancé, registry sur le port " + defaultPort);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}