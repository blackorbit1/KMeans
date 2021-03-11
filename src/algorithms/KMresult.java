package algorithms;

public class KMresult<A, B> {
    public A kmeans;
    public B barycentres;
    public double score;

    public KMresult(A kmeans, B barycentres, double score){
        this.kmeans = kmeans;
        this.barycentres = barycentres;
        this.score = score;
    }
}
