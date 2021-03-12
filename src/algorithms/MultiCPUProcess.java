package algorithms;

import java.util.ArrayList;
import java.awt.Point;
import java.util.Random;

public class MultiCPUProcess extends Thread {
    private ArrayList<Point> points;
    private double score;
    private int nb_iterations;
    private int taux_random;

    KMresult kmean_min;


    double getScore() {
        return score;
    }

    KMresult getSolution() {
        if(kmean_min != null) return kmean_min;
        return null;
    }

    MultiCPUProcess (ThreadGroup tg, String name, ArrayList<Point> points, int nb_iterations, int taux_random) {
        super(tg,name);
        this.points = points;
        this.nb_iterations = nb_iterations;
        this.taux_random = taux_random;

        this.score = Double.MAX_VALUE;
    }

    public void run() {
        Double score_min = Double.MAX_VALUE;
        Random r = new Random();
        kmean_min = null;

        Double last_score = Double.MAX_VALUE;
        for (int i = 0; i < nb_iterations; i++){
            try {
                KMresult tmp = DefaultTeam.kmeans(points, taux_random, r);
                if(tmp == null) continue; // new
                //Double score_tmp = Evaluator.score(tmp.kmeans);
                /*
                // Comme c'est deterministe, si on est déjà passé dessus on connais le score final
                if(last_score.equals(score_tmp) || DefaultTeam.solutionsMap.contains(score_min)) break;
                if(i == (nb_iterations - 1) && last_score > score_tmp){
                    //System.out.println("Encore dans la cours ! " + last_score + " - " + score_tmp);
                    nb_iterations++;
                }
                last_score = score_tmp;

                 */

                if(tmp.score < score_min){
                    kmean_min = tmp;
                    score_min = tmp.score;
                }
                //System.out.println("score_tmp : " + score_tmp + " - score_min : " + score_min + " score_tmp < score_min " + (score_tmp < score_min));

            } catch (Exception e){
                //System.out.println(e.getMessage());
            }

        }
        DefaultTeam.solutionsMap.add(score_min);
        if(kmean_min != null) DefaultTeam.petit_canard(kmean_min.kmeans, kmean_min.barycentres);
    }
}
