package algorithms;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IO {
    private static final String SOLUTIONS_FOLDER = "solutions/";
    private static final String SOLUTION_FILE_REGEX = "solution_([a-z0-9]+?)_([0-9.]+?)\\.kmres";

    /*
    //FILE PRINTER
    public void saveToFile(String filename,ArrayList<ArrayList<Point>> result){
        try {
            while(true){
                BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(filename+".points")));
                try {
                    input.close();
                    System.out.println("Une solution avec le meme score est deja presente dans les fichiers pour ce graphe");
                    return;
                } catch (IOException e) {
                    System.err.println("I/O exception: unable to close "+filename+".points");
                }
            }
        } catch (FileNotFoundException e) {
            printToFile(filename+".points",result);
        }
    }
    private void printToFile(String filename,ArrayList<Point> points){
        try {
            PrintStream output = new PrintStream(new FileOutputStream(filename));
            int x,y;
            for (Point p:points) output.println(Integer.toString((int)p.getX())+" "+Integer.toString((int)p.getY()));
            output.close();
        } catch (FileNotFoundException e) {
            System.err.println("I/O exception: unable to create "+filename);
        }
    }

    //FILE LOADER
    public ArrayList<Point> readFromFile(String filename) {
        String line;
        String[] coordinates;
        ArrayList<Point> points=new ArrayList<Point>();
        try {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename))
            );
            try {
                while ((line=input.readLine())!=null) {
                    coordinates=line.split("\\s+");
                    points.add(new Point(Integer.parseInt(coordinates[0]),
                            Integer.parseInt(coordinates[1])));
                }
            } catch (IOException e) {
                System.err.println("Exception: interrupted I/O.");
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    System.err.println("I/O exception: unable to close "+filename);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Input file not found.");
        }
        return points;
    }
    */

    public static boolean save(KMresult kmeans, ArrayList<Point> points){
        ObjectOutputStream oos = null;
        FileOutputStream fout = null;
        String fileName = SOLUTIONS_FOLDER + "solution_" + hashListOfPoints(points) + "_" + kmeans.score + ".kmres";
        try{
            File file = new File(fileName);
            file.createNewFile();
            fout = new FileOutputStream(file, true);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(kmeans);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if(oos != null){
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // pas besoin de retourner false, on a déjà enregistré le fichier
                }
            }
        }
        return true;
    }

    public static KMresult getBestKM(ArrayList<Point> points){
        KMresult result = null;
        String pointsHash = hashListOfPoints(points);




        File folder = new File(SOLUTIONS_FOLDER);

        File[] listOfFiles = folder.listFiles();
        ArrayList<Double> listOfSolutionsScore = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName();

                Pattern pattern = Pattern.compile(SOLUTION_FILE_REGEX);
                Matcher matcher = pattern.matcher(fileName);

                if(matcher.find()){
                    if(matcher.group(1).equals(pointsHash)){
                        listOfSolutionsScore.add(Double.parseDouble(matcher.group(2)));
                    }
                }
            }
        }

        Collections.sort(listOfSolutionsScore, new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                // ordre decroissant
                return o1.compareTo(o2);
            }
        });

        if(listOfSolutionsScore.size() == 0) return null;

        System.out.println("Scores : " + listOfSolutionsScore);


        String fileName = SOLUTIONS_FOLDER + "solution_" + pointsHash + "_" + listOfSolutionsScore.get(0) + ".kmres";

        System.out.println("FileName : " + fileName);


        ObjectInputStream objectinputstream = null;
        try {
            FileInputStream streamIn = new FileInputStream(fileName);
            objectinputstream = new ObjectInputStream(streamIn);
            result = (KMresult) objectinputstream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(objectinputstream != null){
                try {
                    objectinputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String hashListOfPoints(ArrayList<Point> points){
        String result = "";
        for(Point point : points){
            result += "" + point.x + "" + point.y;
        }
        return md5(result);
    }

    public static String md5(String str) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(str.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
