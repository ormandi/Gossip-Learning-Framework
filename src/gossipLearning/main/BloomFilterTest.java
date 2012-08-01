package gossipLearning.main;

import gossipLearning.DataBaseReader;
import gossipLearning.interfaces.VectorEntry;
import gossipLearning.utils.MultiBloomFilter;
import gossipLearning.utils.SparseVector;

import java.io.File;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class BloomFilterTest {

  public static void main(String[] args) throws Exception {
    if (args.length != 6) {
      System.out.println("Usage: java BloomFilterTest iter seed m k trainName evalName");
      System.exit(0);
    }
    int iter = Integer.parseInt(args[0]);
    long seed = Integer.parseInt(args[1]);
    int m = Integer.parseInt(args[2]);
    int k = Integer.parseInt(args[3]);
    String trainName = args[4];
    String testName = args[5];
    
    Random r = new Random(seed);
    Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
    MultiBloomFilter filter = new MultiBloomFilter(m, k);
    DataBaseReader dbReader = DataBaseReader.createDataBaseReader("gossipLearning.RecSysDataBaseReader", new File(trainName), new File(testName));
    
    System.out.println("#iter\tuniqCount\tmultiCount\tmultiError\t" + dbReader.getTrainingSet().getNumberOfFeatures());
    for (int i = 1; i <= iter; i++) {
      int index = r.nextInt(dbReader.getTrainingSet().size());
      SparseVector ratings = dbReader.getTrainingSet().getInstance(index);
      for (VectorEntry e : ratings) {
        if (!map.containsKey(e.index)) {
          map.put(e.index, 0);
        }
        map.put(e.index, map.get(e.index) + 1);
        filter.add(e.index);
      }
      
      // evaluate
      double multiError = 0.0;
      double numOfMultiElements = 0.0;
      for (int key : map.keySet()) {
        numOfMultiElements += map.get(key);
        multiError += filter.contains(key);
      }
      System.out.println(i + "\t" + map.size() + "\t" + numOfMultiElements + "\t" + ((multiError/numOfMultiElements)-1.0));
    }
  }

}
