# MOSFET

Mod **Fabric** pour **Minecraft 1.21.11** (loader **0.19.5**) qui ajoute deux
transistors MOSFET pilotables par la redstone :

| Bloc | Nom | Conduction |
|---|---|---|
| `mosfet:nmos_mosfet` | MOSFET NMOS (canal N) | le drain prend la valeur de la source quand **grille ≥ source** |
| `mosfet:cmos_mosfet` | MOSFET CMOS (canal P) | le drain prend la valeur de la source quand **grille < source** |

Le signal est **analogique** : quand le MOSFET conduit, la sortie reprend
exactement la force du signal d'entrée (1 à 15), elle n'est pas régénérée à 15.

## Les trois bornes

Le bloc est orientable (comme un répéteur) : en le posant, la **sortie pointe
devant vous**.

```
                 face avant = DRAIN (sortie)
                          ▲
                          │
        GRILLE ──►  ┌───────────┐
        (gate)      │  MOSFET   │
                    └───────────┘
                          ▲
                          │
                 face arrière = SOURCE (entrée)
```

- **Source** (face arrière) : le signal à faire passer.
- **Grille / gate** (les deux faces latérales) : la borne de commande. Les deux
  côtés sont lus, on garde le plus fort des deux.
- **Drain** (face avant) : la sortie. Elle n'alimente que cette face, comme un
  répéteur, et elle délivre un signal *fort* (elle peut donc traverser un bloc
  plein posé juste devant).

Les trois bornes sont **isolées** entre elles : le bloc n'est pas un conducteur,
donc aucun signal ne « fuit » de la grille vers la source.

## Comportement

### NMOS (canal N)

| Grille | Source | Drain (sortie) |
|---|---|---|
| 15 | 7 | **7** (15 ≥ 7 → ça conduit) |
| 7 | 7 | **7** (7 ≥ 7 → ça conduit) |
| 4 | 7 | **0** (4 < 7 → bloqué) |
| 0 | 0 | **0** |

### CMOS (canal P)

| Grille | Source | Drain (sortie) |
|---|---|---|
| 15 | 7 | **0** (15 ≥ 7 → bloqué) |
| 4 | 7 | **7** (4 < 7 → ça conduit) |
| 7 | 7 | **0** (égalité → bloqué) |
| 0 | 0 | **0** |

En pratique :

- le **NMOS** est un interrupteur « normalement ouvert » piloté par une grille
  **forte** : plus la grille est puissante, plus il laisse passer de signaux
  faibles ;
- le **CMOS (canal P)** est son complémentaire : il laisse passer le signal
  quand la grille est **plus faible** que la source ;
- en mettant un NMOS et un CMOS dos à dos on obtient une logique
  complémentaire (d'où le nom *CMOS*).

La mise à jour de la sortie est différée d'un tick (comme un comparateur), ce
qui évite les boucles de retour instantanées si vous rebranchez la sortie sur
la grille.

## Craft

Les deux recettes sont des miroirs verticaux : le CMOS est l'inverse du NMOS.
(`R` = poudre de redstone, `Q` = quartz, `C` = lingot de cuivre)

```
NMOS :  R Q R        CMOS :  C C C
        C C C                R Q R
```

Les blocs sont aussi disponibles dans l'onglet créatif **Redstone**.

## Installation

1. Installer [Fabric Loader 0.19.5](https://fabricmc.net/use/installer/) pour Minecraft 1.21.11.
2. Installer [Fabric API](https://modrinth.com/mod/fabric-api) (version `0.141.6+1.21.11` ou plus récente).
3. Compiler le mod puis glisser le jar dans `.minecraft/mods` :

```bash
./gradlew build
# le jar se trouve dans build/libs/mosfet-1.0.0.jar
```

Un JDK 21 minimum est nécessaire (`JAVA_HOME`).

## Structure du projet

```
src/main/java/fr/yannis/mosfet/
├── MosfetMod.java              # enregistrement des blocs + onglet créatif
└── block/
    ├── MosfetBlock.java        # socle commun : bornes, redstone, propagation
    ├── NmosMosfetBlock.java    # canal N : conduit si grille >= source
    └── CmosMosfetBlock.java    # canal P : conduit si grille < source
src/main/resources/
├── assets/mosfet/              # modèles, textures, traductions (fr + en)
└── data/mosfet/                # recettes, tables de butin, déblocages
tools/gen_textures.py           # génère les textures 16x16 et l'icône
```

## Licence

MIT — voir [LICENSE](LICENSE).
