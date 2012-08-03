package gossipLearning.controls.observers;

import gossipLearning.models.recSys.TopKFreqCollector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import peersim.core.Control;
import peersim.core.Network;

public class TopKObserver implements Control {
  private final Set<Integer> expectedTopkSet = new TreeSet<Integer>();
  
  
  public TopKObserver(String prefix) {
    // BoockCrossing
    expectedTopkSet.addAll(Arrays.asList(819,374,206,643,629,3120,1567,1163,1200,567,587,938,824,1374,1087,4071,3456,1497,2693,633,1210,1509,3320,505,280,5601,1685,1490,33,1146,3789,3638,429,1539,1036,1782,617,2005,5543,2090,1037,2220,5733,6086,2805,438,1274,4619,3300,2504,6822,4647,1127,8675,678,6053,296,8243,4746,4747,991,2671,1216,2532,1164,7972,3395,2542,1711,3760,701,5648,9211,2739,2666,7091,667,1783,1230,7013,2159,1339,687,2737,2113,2541,1526,1282,1223,4219,7105,2109,7710,5573,2540,2025,1577,1114,644,1452));
    // MovieLens
    //expectedTopkSet.addAll(Arrays.asList(592,479,355,456,588,591,295,779,589,379,1209,317,2570,587,376,2857,526,1195,607,1197,1269,647,2761,594,343,363,596,2027,499,735,1579,366,732,259,857,1239,348,586,1096,315,1264,538,2958,1720,1290,4992,1196,2627,1135,1213,2715,3577,1072,356,453,1035,585,433,1616,328,4305,5951,291,2395,1199,2996,230,1703,1526,1192,252,338,149,1922,540,2682,3792,3995,1088,1212,4225,164,1220,1392,207,1960,287,7152,923,918,2986,1306,2705,1386,1609,1783,299,2917,1516,439));
  }

  @Override
  public boolean execute() {
    for (int i = 0; i < Network.size(); i++) {
      // get top-k set of node i
      int[] topkArray = TopKFreqCollector.getTopK(i, 100);
      Set<Integer> topkSet = new TreeSet<Integer>();
      for (int j = 0; topkArray != null && j < topkArray.length; j ++) {
        topkSet.add(topkArray[j]);
      }
      
      // measure similarity between sets
      Set<Integer> intersection = new HashSet<Integer>(expectedTopkSet);
      intersection.retainAll(topkSet);
      double sim = intersection.size(); 
      
      Set<Integer> union = new HashSet<Integer>(expectedTopkSet);
      union.addAll(topkSet);
      sim /= union.size();

      if (i == 0) {
        System.out.println(topkSet);
        System.out.println(sim);
      }
    }
    return false;
  }

}
