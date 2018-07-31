package gossipLearning.temp.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//import com.google.gson.Gson;

public final class FindSeries {
  public static void main(String[] args) throws Exception {
    //Gson gson = new Gson();
    String fName = "bin/distributions/5/res.out";
    List<int[]> series = loadSeries(new FileInputStream(fName));
    //List<int[]> series = loadSeries(System.in);
    //System.out.println(gson.toJson(series));
    
    int[][][] result = getSeries(series);
    for (int i = 0; i < result.length; i++) {
      //System.out.println("SERIE " + i + ": " + gson.toJson(result[i]));
    }
    
    // in LocalRun:
    //String batchSizeFile = Configuration.getString("BATCHSIZEFILE", null);
    //int minBatchSize = Configuration.getInt("MINBATCHSIZE", 1);
    //int[][] treeSizeSerie = FindSeries.getSeries(FindSeries.loadSeries(new FileInputStream(batchSizeFile)))[0];
    //int treeIdx = 0;
    //batchSize = treeSizeSerie[treeIdx][1];
    //evalTime = batchSize;
    //System.out.println(iter + "\t" + result);
    //} else {
    //System.out.println((treeSizeSerie[treeIdx][0] + treeSizeSerie[treeIdx][2]) + "\t" + result);
    // TODO: remove
    /*System.err.println(treeIdx + "\t" + Arrays.toString(treeSizeSerie[treeIdx]) + "\t" + batchSize + "\t" + evalTime);
    treeIdx ++;
    if (treeSizeSerie.length <= treeIdx) {
      break;
    }
    batchSize = treeSizeSerie[treeIdx][1];
    evalTime += batchSize;
    */
    //if (minBatchSize <= batchSize) {
    // update
  }
  public static List<int[]> loadSeries(InputStream is) throws Exception {
    List<int[]> result = new LinkedList<int[]>();
    String line;
    String[] split;
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    while ((line = br.readLine()) != null) {
      if (line.length() == 0) {
        continue;
      }
      split = line.split("\\s");
      int[] serie = new int[split.length];
      for (int i = 0; i < split.length; i++) {
        serie[i] = Integer.parseInt(split[i]);
      }
      result.add(serie);
    }
    br.close();
    Collections.sort(result, new IntArrayComparator());
    return result;
  }
  public static int[][][] getSeries(List<int[]> series) {
    List<int[][]> result = new LinkedList<int[][]>();
    //int serieIdx = 0;
    while (!series.isEmpty()) {
      LinkedList<int[]> serie = new LinkedList<int[]>();
      Iterator<int[]> it = series.iterator();
      while (it.hasNext()) {
        int[] element = it.next();
        if (serie.isEmpty() || serie.getLast()[0] + serie.getLast()[2] <= element[0] + 1) {
          serie.add(element);
          it.remove();
        }
      }
      result.add(serie.toArray(new int[][]{}));
      //System.out.println("SERIE " + serieIdx + ": " + gson.toJson(serie));
      //serieIdx ++;
    }
    return result.toArray(new int[][][]{});
  }
  private static final class IntArrayComparator implements Comparator<int[]>{
    @Override
    public int compare(int[] o1, int[] o2) {
      return o1[0] - o2[0];
    }
    
  }
}
