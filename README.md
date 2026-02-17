# CSP Inventory

## Lancer en dev

```bash
mvn javafx:run
```

Pre-requis: Java 21 et Maven.

## Build portable

```bash
mvn -Pportable clean package
```

Le build produit un ZIP portable dans `target/`:

- archive: `target/csp-inventory-portable.zip`

Contenu:

- `start.sh` (macOS / Linux)
- `start.bat` (Windows)
- `app/` (jar + toutes les dependances Java)
- `CSPInventory/` (dossiers accessibles `models`, `backups`, `data`)

Important: la JVM n'est pas incluse.
Il faut Java 21 installe sur le PC cible.

## Build Windows sans Java installe (EXE/MSI)

Sur Windows 11, tu peux generer un package natif avec runtime embarque via `jpackage`:

```bat
build-windows-package.bat
```

Resultat:

- `target\installer\CSPInventory\CSPInventory.exe` (app-image, pret a lancer)

Pour generer aussi un installateur MSI:

```bat
build-windows-package.bat msi
```

Resultat supplementaire:

- `target\installer\CSPInventory-*.msi`

Pre-requis (machine de build Windows):

- JDK 21 (avec `jpackage`)
- Maven
- Optionnel pour MSI: WiX Toolset (si `jpackage` le demande)

## Dossiers accessibles (models / backups / data)

Au lancement, l'application cree un dossier externe:

- par defaut: `~/CSPInventory`
- personnalisable via variable env `CSPINVENTORY_HOME`
- ou via option JVM `-Dcspinventory.home=/chemin/vers/dossier`

Structure creee automatiquement:

- `data/inventory.db` (base SQLite)
- `models/` (images de modeles ajoutables/modifiables par l'utilisateur)
- `backups/` (backup DB auto au demarrage)

Les images de `models/` sont prioritaires. Si aucune image externe ne correspond, l'app garde le fallback sur les images embarquees.
