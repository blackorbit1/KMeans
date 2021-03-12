package algorithms;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class KMresult implements Serializable {
    //private static final long serialVersionUID = 1L;

    public ArrayList<ArrayList<Point>> kmeans;
    public ArrayList<Point> barycentres;
    public double score;

    public KMresult(ArrayList<ArrayList<Point>> kmeans, ArrayList<Point> barycentres, double score){
        this.kmeans = kmeans;
        this.barycentres = barycentres;
        this.score = score;
    }
}
