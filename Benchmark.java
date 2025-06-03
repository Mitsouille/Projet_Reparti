import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

// Classe principale pour le benchmark du raytracer
public class Benchmark extends JFrame {
    private JTextArea outputArea;
    private JButton runButton;
    private JTextField decoupeField;
    private JComboBox<String> typeDecoupeBox;
    private JComboBox<String> modeBox;

    public Benchmark() {
        setTitle("Benchmark Raytracer");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel label1 = new JLabel("Nombre de découpes :");
        label1.setBounds(20, 20, 160, 25);
        add(label1);

        decoupeField = new JTextField("100");
        decoupeField.setBounds(180, 20, 80, 25);
        add(decoupeField);

        JLabel label2 = new JLabel("Type de découpe :");
        label2.setBounds(20, 60, 160, 25);
        add(label2);

        typeDecoupeBox = new JComboBox<>(new String[] { "ligne", "carre" });
        typeDecoupeBox.setBounds(180, 60, 100, 25);
        add(typeDecoupeBox);

        JLabel label3 = new JLabel("Mode de rendu :");
        label3.setBounds(20, 100, 160, 25);
        add(label3);

        modeBox = new JComboBox<>(new String[] { "Séquentiel", "Multithread" });
        modeBox.setBounds(180, 100, 120, 25);
        add(modeBox);

        runButton = new JButton("Lancer Benchmark");
        runButton.setBounds(350, 20, 200, 30);
        add(runButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBounds(20, 150, 540, 280);
        add(scrollPane);

        runButton.addActionListener(e -> lancerBenchmark());
    }

    private void lancerBenchmark() {
        outputArea.setText("");
        int nbDecoupe = Integer.parseInt(decoupeField.getText());
        String typeDecoupe = (String) typeDecoupeBox.getSelectedItem();
        boolean isMulti = modeBox.getSelectedItem().equals("Multithread");

        new Thread(() -> {
            try {
                // Connexion RMI
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                ServiceServeur serveur = (ServiceServeur) registry.lookup("Calculateur");

                // Attente active si aucun client connecté
                List<ServiceClient> clients = serveur.getClients();
                while (clients.isEmpty()) {
                    System.out.println("En attente de clients RMI...");
                    Thread.sleep(1000);
                    clients = serveur.getClients();
                }

                int nbClients = clients.size();
                outputArea.append("Nombre de clients connectés : " + nbClients + "\n");

                String[] args = {
                        "simple.txt",
                        "1048",
                        "1048",
                        Boolean.toString(isMulti),
                        Integer.toString(nbDecoupe),
                        typeDecoupe
                };

                long start = System.currentTimeMillis();
                LancerRaytracer.main(args);
                long end = System.currentTimeMillis();

                long duration = end - start;
                outputArea.append("Temps " + (isMulti ? "multithread" : "séquentiel") + " : " + duration + " ms\n");

                // Sauvegarde CSV
                String csvLine = nbClients + "," + nbDecoupe + "," + typeDecoupe + "," +
                        (isMulti ? "multithread" : "sequentiel") + "," + duration;
                sauvegarderCSV("statistiques.csv", csvLine);

            } catch (Exception ex) {
                ex.printStackTrace();
                outputArea.append("Erreur : " + ex.getMessage());
            }
        }).start();
    }

    // Méthode pour sauvegarder les résultats dans un fichier CSV

    private void sauvegarderCSV(String nomFichier, String nouvelleLigne) {
        try {
            File fichier = new File(nomFichier);
            boolean existe = fichier.exists();

            if (!existe) {
                try (FileWriter fw = new FileWriter(fichier)) {
                    fw.write("nb_clients,nb_decoupe,type_decoupe,mode,temps_ms\n");
                }
            }

            // Vérification si la ligne existe déjà
            BufferedReader reader = new BufferedReader(new FileReader(fichier));
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                if (ligne.startsWith("nb_clients"))
                    continue; // Ignorer l'en-tête
                String[] parts = ligne.split(",");
                if (parts.length >= 4) {

                    // Comparer les 4 premiers champs pour éviter les doublons
                    String keyExistante = parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3];
                    String keyNouvelle = nouvelleLigne.substring(0, nouvelleLigne.lastIndexOf(","));
                    if (keyExistante.equals(keyNouvelle)) {
                        reader.close();
                        return;
                    }
                }
            }

            // Écriture de la nouvelle ligne
            FileWriter fw = new FileWriter(fichier, true);
            fw.write(nouvelleLigne + "\n");
            fw.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le CSV : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Benchmark().setVisible(true));
    }
}
