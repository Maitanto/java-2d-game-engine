# Lancer le projet

## Avec Eclipse

1. Ouvrir Eclipse.
2. Choisir le dossier `gradius` comme workspace.
3. Créer un nouveau projet Java nommé `Moteur`.
4. Utiliser Java 21.
5. Ne pas créer de fichier `module-info.java`.
6. Lancer le projet avec le bouton **Run**.
7. Sélectionner `Main.java` comme classe principale si Eclipse le demande.

## Lancer le jeu hors Eclipse

Le jeu peut aussi être lancé sans Eclipse, directement depuis le dossier Moteur.

## Avec le fichier .jar
- Dans le dossier Moteur : `java -jar Gradius.jar` 

## Avec le lanceur exécutable

Un lanceur nommé GradiusGame est également fourni dans le dossier Moteur :

- `./GradiusGame`

Si le lanceur n’a pas encore les droits d’exécution, il faut d’abord lui donner les droits :

- `chmod +x GradiusGame`

## Lancer les tests

1. Faire clic droit sur le projet `Moteur`.
2. Aller dans **Properties**.
3. Aller dans **Java Build Path**.
4. Ouvrir l’onglet **Libraries**.
5. Cliquer sur **Add Library**.
6. Sélectionner **JUnit**, puis cliquer sur **Next**.
7. Sélectionner **JUnit 6**, puis cliquer sur **Finish**.
8. Cliquer sur **Apply and Close**.
9. Lancer les tests avec **Run as > JUnit Test**.


## Dépendances

Le projet utilise le fichier `given.jar`.

Si Eclipse ne le détecte pas automatiquement :

1. Faire clic droit sur le projet `Moteur`.
2. Aller dans **Properties > Java Build Path**.
3. Ouvrir l’onglet **Libraries**.
4. Cliquer sur **Add JARs...** ou **Add External JARs...**.
5. Sélectionner `given.jar`.
6. Cliquer sur **Apply and Close**.

## Contrôles du vaisseau
- Maintenir *Flèche droite* pour aller à droite.
- Maintenir *Flèche gauche* pour aller à gauche.
- Maintenir *Flèche haut* pour aller en haut.
- Maintenir *Flèche bas* pour aller en bas.
- Combiner 2 flèches permet de se déplacer en diagonale.
- Maintenir *Espace* pour tirer.
- Appuyer sur *D* ainsi qu'une des flèches pour effectuer un dash dans la direction voulue.

## Mode Debug
- Appuyer sur *Tab* pour afficher les données telles que :
	- Durée moyenne d'un tick
	- FPS
	- Etat courant de l'automate d'une entité
	- Position/Vecteurs vitesse/Vitesse de rotation d'une entité
	- BoundingBox d'une entité
- Appuyer sur *V* pour afficher toute la map, et ainsi voir le déplacement du ViewPort.
- Le *clic molette* combine les deux effets.
