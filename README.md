## Introduction

Ce projet implémente un **time-traveling debugger** en Java en utilisant l'API Java Debug Interface (JDI). Il permet d'exécuter et de déboguer un programme tout en capturant les états d'exécution et en offrant des fonctionnalités de step-back pour revenir en arrière dans l'exécution.

## Fonctionnalités Principales

1. **Contrôle complet de l'exécution** : Permet de **stepper** (instruction par instruction), d'avancer à la ligne suivante et de continuer l'exécution normalement.
2. **Capture des états non-déterministes** : Stocke les variables locales et les appels non-déterministes (identifiés à l'avance) pour garantir un retour à l'état correct.
3. **Compteur de programme (PC)** : Enregistre la ligne d'exécution actuelle à chaque étape.
4. **Commande `step-back`** : Permet de revenir un pas en arrière dans l'exécution.
5. **Commande `step-back(n)`** : Permet de revenir *n* pas en arrière.
6. **Inspection de l'état de l'exécution** : Permet de voir le `frame`, la `stack`, les `variables` locales, les objets `receiver` et `sender`.

---

## Utilisation

### Interface en ligne de commande

Une fois le programme en pause sur un breakpoint, utilisez les commandes suivantes :

| Commande | Description |
| --- | --- |
| `step` | Exécute l'instruction suivante (Step Into). |
| `step-over` | Passe à la ligne suivante sans entrer dans les appels de méthode. |
| `continue` | Reprend l'exécution jusqu'au prochain breakpoint. |
| `frame` | Affiche la frame actuelle d'exécution. |
| `stack` | Affiche la pile d'appels (call stack). |
| `receiver` | Affiche l'objet courant (`this`). |
| `sender` | Affiche la méthode appelante. |
| `variables` | Liste les variables locales visibles. |
| `step-back` | Revient un pas en arrière. |
| `step-back n` | Revient *n* pas en arrière. |
| `exit` | Quitte le débogueur. |

---

## Fonctionnement du Time-Travel Debugging

### Enregistrement des états

Le débogueur capture l'état du programme à chaque **BreakpointEvent** ou **StepEvent** et enregistre :

- **Ligne d'exécution actuelle** (Compteur de programme, PC).
- **Valeurs des variables locales**.
- **Valeurs des objets modifiés** (dans une future amélioration, pourrait être enrichi avec la heap).
- **Identifiants des appels non-déterministes**.

### Step-back : Revenir en arrière

Lorsqu'un `step-back(n)` est exécuté, le débogueur :

1. **Dispose** la VM de l'exécution actuelle.
2. **Relance** la VM à partir du début.
3. **Rejoue** jusqu'au step *n* désiré en répétant exactement les mêmes entrées non-déterministes.
4. **Arrête** l'exécution en mode suspendu au step correspondant.

---

## Architecture du Code

### Fichiers principaux

| Fichier | Rôle |
| --- | --- |
| `ScriptableDebugger.java` | Implémente le cœur du débogueur et les commandes d'exécution. |
| `TimeTravelManager.java` | Gère l'enregistrement des états pour le time-travel. |
| `ExecutionState.java` | Représente un état d'exécution (PC, variables, heap, appels non-déterministes). |
| `DebugStep.java` | Enregistre chaque étape du programme (classe, méthode, ligne). |
| `JDISimpleDebugger.java` | Classe principale qui démarre le débogueur. |

---

## Améliorations futures

- **Optimisation de la mémoire** : Stocker uniquement les différences d'état au lieu de tout l'historique.
- **Ajout d'un mode graphique** : Une interface UI pour contrôler le débogueur plus facilement.
- **Meilleure gestion de la mémoire** : Actuellement, chaque step est stocké, ce qui peut être coûteux pour de longs programmes.

---

## Auteurs

- **Auteurs** : Moetez Jaoued  et Aimad Ait Mahamed
- **Date** : 07/02/2025