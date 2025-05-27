import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Serveur {
    public static void main(String[] args){
        try{
            CalculServeur cs = new CalculServeur();
            ServiceServeur rd = (ServiceServeur) UnicastRemoteObject.exportObject(cs, 0);
            Registry reg = LocateRegistry.createRegistry(1099);
            //Registry reg = LocateRegistry.getRegistry(1099);
            reg.rebind("Calculateur", rd);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}