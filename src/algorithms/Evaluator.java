//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

public class Evaluator {
    public Evaluator() {
    }

    public static boolean isValid(ArrayList<Point> inpoints, ArrayList<ArrayList<Point>> inpts) {
        ArrayList<ArrayList<Point>> clusters = new ArrayList();

        for(int i = 0; i < inpts.size(); ++i) {
            clusters.add((ArrayList)((ArrayList)inpts.get(i)).clone());
        }

        ArrayList<Point> points = (ArrayList)inpoints.clone();
        boolean go = true;

        while(go) {
            Point p = (Point)points.remove(0);
            boolean foundIt = false;

            int size;
            for(size = 0; size < clusters.size(); ++size) {
                if (((ArrayList)clusters.get(size)).contains(p)) {
                    foundIt = true;
                    ((ArrayList)clusters.get(size)).remove(p);
                    break;
                }
            }

            go = !points.isEmpty();
            size = 0;

            for(int i = 0; i < clusters.size(); ++i) {
                size += ((ArrayList)clusters.get(i)).size();
            }

            if (size == 0) {
                go = false;
            }
        }

        int size = 0;

        for(int i = 0; i < clusters.size(); ++i) {
            size += ((ArrayList)clusters.get(i)).size();
        }

        return points.isEmpty() && size == 0;
    }

    public static double score(ArrayList<ArrayList<Point>> inpts) {
        ArrayList<ArrayList<Point>> clusters = new ArrayList();

        for(int i = 0; i < inpts.size(); ++i) {
            clusters.add((ArrayList)((ArrayList)inpts.get(i)).clone());
        }

        ArrayList<Attribut> attributs = computeAttributs(clusters);
        double result = sumDist(attributs);
        return result;
    }

    private static double sumDist(ArrayList<Attribut> as) {
        double d = 0.0D;

        Attribut a;
        for(Iterator var3 = as.iterator(); var3.hasNext(); d += a.distanceTotale) {
            a = (Attribut)var3.next();
        }

        return d;
    }

    private static double[] mean(ArrayList<Point> points) {
        double[] mean = new double[]{0.0D, 0.0D};

        Point p;
        for(Iterator var2 = points.iterator(); var2.hasNext(); mean[1] += (double)p.y / (double)points.size()) {
            p = (Point)var2.next();
            mean[0] += (double)p.x / (double)points.size();
        }

        return mean;
    }

    public static double distanceFromC(ArrayList<Point> points, double[] center) {
        double d = 0.0D;
        Point c = new Point();
        c.setLocation(center[0], center[1]);

        Point p;
        for(Iterator var5 = points.iterator(); var5.hasNext(); d += c.distance(p)) {
            p = (Point)var5.next();
        }

        return d;
    }

    private static ArrayList<Attribut> computeAttributs(ArrayList<ArrayList<Point>> cluster) {
        ArrayList<Attribut> attributs = new ArrayList();
        double[] center = new double[2];
        Iterator var6 = cluster.iterator();

        while(var6.hasNext()) {
            ArrayList<Point> c = (ArrayList)var6.next();
            center = mean(c);
            double d = distanceFromC(c, center);
            Attribut a = new Attribut(d, center[0], center[1], c.size());
            attributs.add(a);
        }

        return attributs;
    }
}
