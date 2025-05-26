# Projet Réparti


                +----------------------+
                |  Serveur de nœuds    |   ← Processus fixe (écoute sur un port)
                | (répertoire central) |
                +----------------------+
                         ▲
                         | (requête: "Donne-moi les nœuds dispo")
                         ▼
             +----------------------------+
             | Programme de contrôle      | ← Processus mobile
             | (client, découpe la scène) |
             +----------------------------+
                      /       |        \
           envoie    /        |         \    envoie des sous-calculs
                    ▼         ▼          ▼
           +-----------+ +-----------+ +-----------+
           | Nœud 1    | | Nœud 2    | | Nœud 3    | ← Processus mobiles (entrent/sortent)
           +-----------+ +-----------+ +-----------+

                         ↑       ↑        ↑
                       Résultats de calculs
                         (renvoyés au programme de contrôle)

| Élément               | Rôle                             | Type        |
| --------------------- | -------------------------------- | ----------- |
| Serveur de nœuds      | Donne la liste des nœuds         | **Fixe**    |
| Programme de contrôle | Lance le calcul, centralise tout | **Mobile**  |
| Nœuds de calcul       | Calculent une partie             | **Mobiles** |

| Échange                   | Type de donnée                                                                     |
| ------------------------- | ---------------------------------------------------------------------------------- |
| Client ↔ Serveur de nœuds | Requêtes HTTP / JSON (ex: liste d’IP)                                              |
| Client → Nœuds            | Tâche de calcul (ex: JSON avec coordonnées de l’image à traiter, paramètres, etc.) |
| Nœuds → Client            | Résultat du calcul (ex: image partielle en binaire ou JSON avec données)           |


Pour faire des calcules en parallèles il suffit d'utiliser des Thread !