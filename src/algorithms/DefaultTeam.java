package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class DefaultTeam {
  public static TreeSet<Double> solutionsMap = new TreeSet<>();
  private static boolean mainControlled = false;
  private String hash = null;

  public static void main(String[] args){
    DefaultTeam df = new DefaultTeam();
    mainControlled = true;
    ArrayList<Points> points_list = IO.getAllPoints();

    int iteration = 0;
    double score_sum = 0;
    for(Points points : points_list){
      df.hash = points.hash;
      ArrayList<ArrayList<Point>> res = df.calculKMeans(points.points);
      score_sum += Evaluator.score(res);
      System.out.println(">>> Iteration " + ++iteration + " - Moyenne : " + (score_sum / iteration));
    }

  }

  public ArrayList<ArrayList<Point>> calculKMeans(ArrayList<Point> points) {
    solutionsMap = new TreeSet<>();

    double scoreMin = Integer.MAX_VALUE;
    KMresult result = IO.getBestKM(points, mainControlled ? this.hash : null);
    if(result != null) scoreMin = result.score;

    for (int i=0;i<10;i++) {
      KMresult tmp = multi_CPU_kmeans(points);
      if(tmp != null){
        double tmp_score = Evaluator.score(tmp.kmeans);
        if(tmp_score < scoreMin) {
          scoreMin = tmp_score;
          result = tmp;
        }
        System.out.println("scoreMin : " + scoreMin + " - tmp_score : " + tmp_score);
      }

    }

    result.score = scoreMin;
    IO.save(result, points, mainControlled ? this.hash : null);
    
    return result.kmeans;
  }

  private KMresult multi_CPU_kmeans(ArrayList<Point> points) {
    Random r = new Random();
    int NB_ITERATIONS = 500;
    int TAUX_RANDOM = 400 + r.nextInt(400);
    int np = Runtime.getRuntime().availableProcessors();

    // override number of available processors for tests purposes
    //np = 4;

    ThreadGroup tg = new ThreadGroup("main");
    List<MultiCPUProcess> sims = new ArrayList<MultiCPUProcess>();

    for (int i=0, position = 0;i<np;i++) {
      sims.add(new MultiCPUProcess(tg, "KM"+i, points, NB_ITERATIONS, TAUX_RANDOM));

    }

    int i=0;
    while (i<sims.size()){
      if (tg.activeCount()<np){ // do we have available CPUs?
        MultiCPUProcess sim = sims.get(i);
        sim.start();
        i++;
      } else {
        try {Thread.sleep(100);} /*wait 0.1 second before checking again*/
        catch (InterruptedException e) {e.printStackTrace();}
      }
    }

    // on attend que tous les preocessus soient termines avant de continuer
    while(tg.activeCount()>0) { // wait for threads to finish
      try {Thread.sleep(100);}
      catch (InterruptedException e) {e.printStackTrace();}
    }

    // On recupere et concatene les resultats
    double scoreMin = Integer.MAX_VALUE;
    KMresult result = null;

    for (i=0;i<sims.size();i++) {
      KMresult tmp = sims.get(i).getSolution();
      if(tmp != null){
        double tmp_score = Evaluator.score(tmp.kmeans);
        if(tmp_score < scoreMin) {
          scoreMin = tmp_score;
          result = tmp;
        }
      }
    }
    for(MultiCPUProcess sim_to_stop : sims){
      sim_to_stop.stop();
    }

    return result;
  }

  public static KMresult kmeans(ArrayList<Point> points, int tr, Random r){

    double max_x = 0;
    double max_y = 0;
    double min_x = Integer.MAX_VALUE;
    double min_y = Integer.MAX_VALUE;
    for(int i = 0; i < points.size(); i++){
      if(points.get(i).x > max_x) max_x = points.get(i).x;
      if(points.get(i).y > max_y) max_y = points.get(i).y;
      if(points.get(i).x < min_x) min_x = points.get(i).x;
      if(points.get(i).y < min_y) min_y = points.get(i).y;
    }

    ArrayList<Point> barycentres = new ArrayList<>();
    barycentres.add(new Point((int) (max_x/2 + min_x) + (r.nextInt(tr) - tr/2), (int) (max_y/2 + min_y) + (r.nextInt(tr) - tr/2)));
    barycentres.add(new Point((int) (max_x/4 + min_x) + (r.nextInt(tr) - tr/2), (int) (max_y/4 + min_y) + (r.nextInt(tr) - tr/2)));
    barycentres.add(new Point((int) (max_x/4 + min_x) + (r.nextInt(tr) - tr/2), (int) (max_y/4 * 3 + min_y) + (r.nextInt(tr) - tr/2)));
    barycentres.add(new Point((int) (max_x/4 * 3 + min_x) + (r.nextInt(tr) - tr/2), (int) (max_y/4 + min_y) + (r.nextInt(tr) - tr/2)));
    barycentres.add(new Point((int) (max_x/4 * 3 + min_x) + (r.nextInt(tr) - tr/2), (int) (max_y/4 * 3 + min_y) + (r.nextInt(tr) - tr/2)));

    ArrayList<ArrayList<Point>> kmeans = new ArrayList<ArrayList<Point>>();
    kmeans.add(new ArrayList<Point>());
    kmeans.add(new ArrayList<Point>());
    kmeans.add(new ArrayList<Point>());
    kmeans.add(new ArrayList<Point>());
    kmeans.add(new ArrayList<Point>());

    for(Point point : points){
      double best_distance = Integer.MAX_VALUE;
      int best_bary = -1;
      for(int i = 0; i < barycentres.size(); i++){
        if(barycentres.get(i).distance(point) < best_distance){
          best_bary = i;
          best_distance = barycentres.get(i).distance(point);
        }
      }
      kmeans.get(best_bary).add(point);
    }

    Double last_score = Double.MAX_VALUE;
    int last_score_count = 0;
    Double score_min = Double.MAX_VALUE;
    ArrayList<ArrayList<Point>> kmeans_min = null;
    ArrayList<Point> barycentres_min = null;

    int n = 5;
    for(int i = 0; i < n; i++){
      for(int j = 0; j < barycentres.size(); j++){
        barycentres.set(j, milieu(kmeans.get(j)));
      }
      kmeans = rempli_avec_plus_proche(barycentres, points);


      Double score_tmp = Evaluator.score(kmeans);
      // Comme c'est deterministe, si on est d??j?? pass?? dessus on connais le score final
      if(i == (n-1) && last_score > score_tmp && (solutionsMap.first() > score_tmp)){
        solutionsMap.add(score_tmp);
        n++;
      }
      last_score = score_tmp;

      // on garde le meilleur
      if(score_tmp < score_min){
        kmeans_min = kmeans;
        barycentres_min = new ArrayList<>(barycentres);
        score_min = score_tmp;
      }
    }
    return new KMresult(kmeans_min, barycentres_min, score_min);
  }

  public static Point milieu(ArrayList<Point> points){
    long total_x = 0;
    long total_y = 0;

    for(Point point : points){
      total_x += point.x;
      total_y += point.y;
    }

    return new Point((int) total_x / points.size(), (int) total_y / points.size());
  }

  public static ArrayList<ArrayList<Point>> rempli_avec_plus_proche(ArrayList<Point> barycentres, ArrayList<Point> points){
    ArrayList<ArrayList<Point>> result = new ArrayList<ArrayList<Point>>();
    for(int i = 0; i < barycentres.size(); i++){
      result.add(new ArrayList<Point>());
    }

    for(Point point : points){
      double best_distance = Integer.MAX_VALUE;
      int best_bary = -1;
      for(int i = 0; i < barycentres.size(); i++){
        if(barycentres.get(i).distance(point) < best_distance){
          best_bary = i;
          best_distance = barycentres.get(i).distance(point);
        }
      }
      result.get(best_bary).add(point);
    }

    return result;
  }

  public static ArrayList<ArrayList<Point>> petit_canard(ArrayList<ArrayList<Point>> kmeans, ArrayList<Point> barycentres){
    ArrayList<Double> cluster_moy = new ArrayList<>();
    // On recupere la distance moyenne des points par rapport au centre pour chaque cluster
    for(int cluster = 0; cluster < barycentres.size(); cluster++){
      cluster_moy.add(Evaluator.distanceFromC(kmeans.get(cluster), new double[] {barycentres.get(cluster).x, barycentres.get(cluster).y}) / kmeans.get(cluster).size());
    }
    // Pour chaque cluster
    for(int cluster = 0; cluster < barycentres.size(); cluster++){
      // Pour chaque point du cluster
      for(int i = 0; i < kmeans.get(cluster).size(); i++){
        // on prends la distance entre le point i et le centre de son cluster
        double distance_own = kmeans.get(cluster).get(i).distance(barycentres.get(cluster));
        // on retient la diff??rence la plus basse entre sa distance avec le centre de
        // son cluster et la distance moyenne des points de ce cluster avec son centre
        double best_cluster_moy = Math.abs(distance_own - cluster_moy.get(cluster));
        //double best_cluster_moy = (Math.abs((distance_own / kmeans.get(cluster).size()) - cluster_moy.get(cluster)));
        // on retient l'index du cluster satifaisant cela
        int best_cluster = cluster;
        // Pour chaque cluster
        for(int other_cluster = 0; other_cluster < barycentres.size(); other_cluster++){
          // on prends la distance entre le point i et le centre de l'autre cluster
          double distance_other = kmeans.get(cluster).get(i).distance(barycentres.get(other_cluster));
          // on retient la diff??rence entre sa distance avec le centre de l'autre cluster
          // et la distance moyenne des points de ce cluster avec son centre
          double diff_distance_other = Math.abs(distance_other - cluster_moy.get(other_cluster));
          //double diff_distance_other = (Math.abs((distance_other / kmeans.get(best_cluster).size()) - cluster_moy.get(best_cluster)));
          if(diff_distance_other < best_cluster_moy) {
            best_cluster = other_cluster;
            best_cluster_moy = diff_distance_other;
          }
        }
        if(cluster != best_cluster){
          double score_before = Evaluator.score(kmeans);
          kmeans.get(best_cluster).add(kmeans.get(cluster).remove(i));
          double score_after = Evaluator.score(kmeans);

          if(!(score_after < score_before)){
            kmeans.get(cluster).add(kmeans.get(best_cluster).remove(kmeans.get(best_cluster).size() - 1));
          }
        }
      }
    }
    return kmeans;
  }
}
