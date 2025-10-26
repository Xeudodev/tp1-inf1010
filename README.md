# TP1 INF1010 - UQTR

> [!NOTE]
>
> [Repo Github](https://github.com/Xeudodev/tp1-inf1010/)

Une petite application client/serveur en Java qui gère un répertoire universitaire (étudiants, professeurs, assistants) via des sockets TCP.

Le serveur conserve les données pendant l'éxécution; un ou plusieurs clients se connectent, envoient des commandes et reçoivent des réponses.

## Exigences

- Java 7 ou plus récent (JRE pour exécuter, JDK correspondant pour compiler)
- Un terminal (CMD/PowerShell sous Windows ou shell sous Linux)

Mot de passe administrateur (pour les actions privilégiées ajouter/supprimer/lister rouge) : `admin123`

## Installation / Dépendances

Aucune dépendance externe. Uniquement le JDK.

Conseils facultatifs :

- Assurez-vous que `javac` et `java` sont dans votre variable PATH.
- Sous Windows, définissez `JAVA_HOME` et ajoutez `%JAVA_HOME%\bin` au PATH si nécessaire.
- Sous Linux, vérifiez avec `java -version` et `javac -version`.

## Compilation

Depuis le répertoire racine du projet (où se trouvent les fichiers `.java`) :

Windows (CMD ou PowerShell) :

```sh
javac -d . *.java
```

Linux :

```sh
javac -d . *.java
```

Cette commande compile toutes les sources dans le répertoire courant (package par défaut).

## Exécution

1. Démarrez le serveur (il charge des données d’exemple de l’UQTR au démarrage sur le port 8080) :

Windows (CMD/PowerShell) :

```sh
java Server
```

Linux :

```sh
java Server
```

1. Dans un autre terminal, lancez un ou plusieurs clients :

Windows (CMD/PowerShell) :

```
java Client
```

Linux :

```sh
java Client
```

Le client affiche un menu (lister/rechercher/ajouter/supprimer/lister rouge).
Pour les actions administratives, il demandera le mot de passe (`admin123`).

## Notes

- Hôte par défaut : `localhost`, port : `8080`
- Assurez-vous que le port 8080 est libre (aucun autre service ne l’utilise).
- Le serveur garde la connexion ouverte et attend des requêtes terminées par un saut de ligne ; il répond et termine chaque réponse par `END`.
- Des données d’exemple pour l’UQTR (Informatique, Musique) sont préchargées pour faciliter les tests.

## Dépannage

- **Connexion refusée ou expirée** : vérifiez que le serveur est lancé avant le client, contrôlez le pare-feu et les paramètres d’hôte/port.
- **Erreurs de compilation** : assurez-vous d’utiliser un JDK (et non seulement un JRE) et que `javac` est disponible.

## Démonstration rapide

1. Lancez le serveur.
2. Lancez un client et choisissez « Lister les membres d’une catégorie », puis `3) ÉTUDIANT` pour voir les étudiants chargés.
3. Essayez « Lister les professeurs par domaine » et entrez `Informatique`.
4. Pour ajouter, supprimer ou mettre sur liste rouge, entrez le mot de passe administrateur : `admin123`