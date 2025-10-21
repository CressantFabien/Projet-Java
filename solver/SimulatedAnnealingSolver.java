package solver;

import model.*; 
import graph.*; 
import java.util.Random;

public class SimulatedAnnealingSolver {

    private final Neighborhood neighborhood; 
    private final DisjuctiveGrapheValidator validator; 
    private final double initialTemperature;
    private final double coolingRate;
    private final int maxIterations; 


    public SimulatedAnnealingSolver(DisjuctiveGrapheValidator validator, double initialTemp, double cooling, int iterations) {
        this.validator = validator;
        this.initialTemperature = initialTemp;
        this.coolingRate = cooling;
        this.maxIterations = iterations;
        this.neighborhood = new Neighborhood(); // On crée l'outil ici
    }

    public Schedule solve(Schedule initialSchedule) {
        System.out.println("LOG: Lancement de l'optimisation par Recuit Simulé...");

        // 1. Valider la solution initiale (pour avoir son score 'f')
        validator.validateSchedule(initialSchedule);
        if (!initialSchedule.isFeasible()) {
            System.err.println("ERREUR: La solution initiale n'est pas faisable !");
            return initialSchedule; // Ou gérer l'erreur autrement
        }

        Schedule currentSchedule = initialSchedule;
        Schedule bestSchedule = initialSchedule.clone(); // Garder une copie de la meilleure
        double currentTemperature = this.initialTemperature;

        // 2. La grande boucle d'optimisation
        for (int i = 0; i < this.maxIterations; i++) {

            // a) Générer un voisin
            Schedule neighborSchedule = neighborhood.generateRandomNeighbor(currentSchedule);

            // b) Valider le voisin
            boolean neighborIsFeasible = validator.validateSchedule(neighborSchedule);

            // c) Décider si on accepte le voisin
            if (neighborIsFeasible) {
                // Calculer la différence de score (delta f)
                // On utilise TES getters pour 'f'
                double currentScore = currentSchedule.getF();
                double neighborScore = neighborSchedule.getF();
                double delta = neighborScore - currentScore;

                // Appliquer le critère d'acceptation
                if (accept(delta, currentTemperature)) {
                    currentSchedule = neighborSchedule; // On accepte le voisin comme nouvelle base

                    // Si ce voisin est le meilleur trouvé jusqu'à présent...
                    if (currentSchedule.getF() > bestSchedule.getF()) {
                        bestSchedule = currentSchedule.clone(); // ...on le sauvegarde
                        System.out.println("    -> Nouvelle meilleure solution trouvée (Score: " + bestSchedule.getF() + ") à l'itération " + i);
                    }
                }
            }
            // (Si le voisin n'est pas faisable, on ne fait rien, on continue)

            // d) Refroidir la température
            currentTemperature *= this.coolingRate;

            // Afficher la progression (optionnel)
            if (i % 1000 == 0) {
                System.out.println("Itération " + i + " / " + maxIterations + " | Temp: " + currentTemperature + " | Score actuel: " + currentSchedule.getF());
            }
        }

        System.out.println("LOG: Optimisation terminée.");
        System.out.println("Meilleur score final trouvé : " + bestSchedule.getF());
        return bestSchedule;
    }


    
    private boolean accept(double delta, double temperature) {
        // Si c'est une amélioration, on accepte toujours
        if (delta > 0) {
            return true;
        }
        // Si c'est moins bien, on accepte avec une probabilité
        // qui dépend de la différence et de la température
        // Attention: Si temperature devient très proche de 0, Math.exp peut déborder
        if (temperature < 1e-9) return false; // Eviter division par zero / overflow
        
        // Probabilité = exp(delta / T)
        return Math.exp(delta / temperature) > Math.random(); // Math.random() donne un nombre entre 0.0 et 1.0
    }
}