package gossipLearning.models.recSys;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.VectorEntry;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.View;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import peersim.core.CommonState;
import peersim.core.Network;

public class TopKFreqCollector implements Model {
  private static final long serialVersionUID = -7435788126371487109L;
  private Set<Integer> itemSet;
  
  public TopKFreqCollector() {
    itemSet = new TreeSet<Integer>();
  }
  
  public TopKFreqCollector(TopKFreqCollector o) {
    itemSet = (o != null && o.itemSet != null) ? new TreeSet<Integer>() : null;
    if (itemSet != null) {
      // deep copy elements
      for (int e : o.itemSet) {
        itemSet.add(e);
      }
    }
  }
  
  public Object clone() {
    return new TopKFreqCollector(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void init(String prefix) {
    if (estimations == null) {
      estimations = new TreeMap[Network.size()];
    }
  }

  @Override
  public void update(SparseVector instance, double label) {
    //System.out.println("Node: " + CommonState.getNode().getID() + ", topK update!!! " + itemSet);
    
    // get the ID of the current node
    int nodeID = (int) label;
    
    // update with the frequency estimation of the current node with itemSet
    for (int itemID : itemSet) {
      increment(nodeID, itemID);
    }
    
    
    // store itemSet based on the current instance
    itemSet = new TreeSet<Integer>();
    for (VectorEntry v : instance) {
      itemSet.add(v.index);
    }
    
    if (nodeID == 0 && CommonState.getTime() % 100 == 0) {
      // perform normalization
      for (int i = 0; i < estimations.length; i ++) {
        for (int itemID : estimations[i].keySet()) {
          estimations[i].put(itemID, estimations[i].get(itemID)/2);
        }
      }
    }
  }

  @Override
  public double predict(SparseVector instance) {
    return 0;
  }

  @Override
  public int getNumberOfClasses() {
    return 0;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
  }
  
  public static TreeMap<Integer,Integer>[] estimations;
  
  private static void increment(int nodeID, int itemID) {
    if (estimations != null && 0 <= nodeID && nodeID < estimations.length) {
      if (estimations[nodeID] == null) {
        estimations[nodeID] = new TreeMap<Integer,Integer>();
      }
      Integer incrementedO = estimations[nodeID].get(new Integer(itemID));
      int incremented = (incrementedO != null) ? incrementedO.intValue() + 1 : 1 ;
      estimations[nodeID].put(itemID, incremented);
    }
  }
  
  public static int[] getTopK(int nodeID, int k) {
    if (estimations != null && 0 <= nodeID && nodeID < estimations.length) {
      final View<ItemFreq> q = new View<ItemFreq>(k);
      
      if (estimations[nodeID] == null) {
        estimations[nodeID] = new TreeMap<Integer,Integer>();
      }
      
      // get top-k element in O(n*k^2) time
      for (int itemID : estimations[nodeID].keySet()) {
        q.insert(new ItemFreq(itemID, estimations[nodeID].get(itemID)));
      }
      
      // receive top-k
      int[] topk = new int[k];
      int idx = 0;
      for (ItemFreq i : q) {
        topk[idx ++] = i.itemID;
      }
      
      return topk;
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    estimations = new TreeMap[1];
    TopKFreqCollector.increment(0, 0);
    TopKFreqCollector.increment(0, 0);
    TopKFreqCollector.increment(0, 0);
    
    TopKFreqCollector.increment(0, 1);
    
    TopKFreqCollector.increment(0, 2);
    TopKFreqCollector.increment(0, 2);
    
    
    
    int[] topk = TopKFreqCollector.getTopK(0, 2);
    printTopK(topk);  // 0,2
    
    topk = TopKFreqCollector.getTopK(0, 3);
    printTopK(topk);  // 0,2,1
    
    TopKFreqCollector.increment(0, 3);
    TopKFreqCollector.increment(0, 3);
    TopKFreqCollector.increment(0, 3);
    TopKFreqCollector.increment(0, 3);
    
    topk = TopKFreqCollector.getTopK(0, 3);
    printTopK(topk);  // 3,0,2
  }
  
  private static void printTopK(int[] topk) {
    for (int i = 0; i < topk.length - 1; i ++) {
      System.out.print(topk[i] + ", ");
    }
    System.out.println(topk[topk.length - 1]);
  }
}

class ItemFreq implements Serializable, Comparable<ItemFreq> {
  private static final long serialVersionUID = -25606713084339L;
  public final int itemID;
  public final int f;

  public ItemFreq(int itemID, int f) {
    this.itemID = itemID;
    this.f = f;
  }

  @Override
  public boolean equals(Object o) {
    return o != null && o instanceof ItemFreq && itemID == ((ItemFreq)o).itemID && f == ((ItemFreq)o).f;
  }

  @Override
  public int compareTo(ItemFreq o) {
    if (o == null || f < o.f) {
      return 1;
    }
    if (f > o.f) {
      return -1;
    }
    return 0;
  }

  @Override
  public String toString() {
    return "(" + itemID + "," + f + ")";
  }
}
