# Projet Réparti


                       [Serveur de RMI Registry]

                           ↑          ↓ (lookup)

             +------------------------------------+
             | Programme de contrôle (client RMI) |
             +------------------------------------+
                      /       |        \
               appel /        |         \  appel
                    ▼         ▼          ▼
           +-----------+ +-----------+ +-----------+
           | Nœud 1    | | Nœud 2    | | Nœud 3    |
           | (serveur) | | (serveur) | | (serveur) |
           +-----------+ +-----------+ +-----------+

                   ↑ chaque nœud enregistre un objet distant dans le RMI registry


| Composant           | Équivalent en RMI                                         |
| ------------------- | --------------------------------------------------------- |
| Serveur de nœuds    | Objet RMI ou composant qui fournit une **liste de nœuds** |
| Nœuds de calcul     | Objets RMI qui implémentent l’interface `Calculateur`     |
| Programme principal | Client RMI qui fait `lookup` sur les nœuds et les appelle |



Pour faire des calcules en parallèles il suffit d'utiliser des Thread !