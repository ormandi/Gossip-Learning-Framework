package gossipLearning.main.rw;

import gossipLearning.utils.Utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class RandomWalkTest {

  public static void main(String[] args) throws Exception {
    int N = 1000;
    int numIter = 1000;
    long seed = 1234567890;
    Random r = new Random(seed);
    String rwClass = "gossipLearning.main.rw.Uniform";
    rwClass = "gossipLearning.main.rw.Permutation";
    rwClass = "gossipLearning.main.rw.BipartiteGraph";
    rwClass = "gossipLearning.main.rw.KRegular";
    
    int lastSeenInIter[] = new int[N];
    int numHits[] = new int[N];
    for (int i = 0; i < N; i++) {
      lastSeenInIter[i] = -1;
      numHits[i] = 0;
    }
    TreeMap<Integer, Integer> hist = new TreeMap<Integer, Integer>();
    
    RW randomWalk = (RW)Class.forName(rwClass).newInstance();
    for (int iter = 0; iter < N*numIter; iter++) {
      int idx = randomWalk.nextId(N, r);
      if (idx == -1) {
        continue;
      }
      numHits[idx] ++;
      if (lastSeenInIter[idx] == -1) {
        lastSeenInIter[idx] = iter;
        continue;
      }
      int dist = iter - lastSeenInIter[idx];
      lastSeenInIter[idx] = iter;
      
      Integer count = hist.get(dist);
      if (count == null) {
        hist.put(dist, 1);
      } else {
        hist.put(dist, count + 1);
      }
    }
    for (int i = 1; i <= hist.lastKey(); i++) {
      if (hist.containsKey(i)) {
        System.out.println(i + "\t" + hist.get(i));
      } else {
        System.out.println(i + "\t0");
      }
    }
    System.out.println();
    
    /*for (int i = 0; i < N; i++) {
      System.out.println(i + "\t" + numHits[i]);
    }*/
  }

}

interface RW {
  public int nextId(int numNodes, Random r);
}

class Permutation implements RW {
  private int[] permutation;
  private int idx = 0;
  @Override
  public int nextId(int numNodes, Random r) {
    if (permutation == null) {
      permutation = new int[numNodes];
      for (int i = 0; i < numNodes; i++) {
        permutation[i] = i;
      }
    }
    if (idx == 0) {
      Utils.arrayShuffle(r, permutation);
    }
    int result = permutation[idx];
    idx = (idx + 1) % numNodes;
    return result;
  }
  
}

class Uniform implements RW {
  @Override
  public int nextId(int numNodes, Random r) {
    return r.nextInt(numNodes);
  }
}

class BipartiteGraph implements RW {
  protected int k = 10;
  private double publicRatio = 0.1;
  protected int[] publicNodes;
  
  protected int currentNode;
  protected int[][] neighbors;
  protected int[] lastSeenAt;
  
  private int time = 0;
  private int maxDeg = k;
  private int minDeg = k;
  
  protected void init(int numNodes, Random r) {
    TreeMap<Integer, Set<Integer>> pub = new TreeMap<Integer, Set<Integer>>();
    lastSeenAt = new int[numNodes];
    neighbors = new int[numNodes][];
    for (int i = 0; i < numNodes; i++) {
      lastSeenAt[i] = -1;
      double rand = r.nextDouble();
      if (rand < publicRatio) {
        pub.put(i, new TreeSet<Integer>());
      }
    }
    publicNodes = new int[pub.size()];
    int tmp = 0;
    for (int i : pub.keySet()) {
      publicNodes[tmp] = i;
      tmp ++;
    }
    for (int i = 0; i < numNodes; i++) {
      if (!pub.containsKey(i)) {
        Utils.arrayShuffle(r, publicNodes);
        int n = Math.min(k, pub.size());
        // TODO: min(k, pub.size())
        neighbors[i] = new int[n];
        for (int j = 0; j < n; j++) {
          neighbors[i][j] = publicNodes[j];
          Set<Integer> set = pub.get(publicNodes[j]);
          set.add(i);
        }
      }
    }
    for (int i : pub.keySet()) {
      Set<Integer> set = pub.get(i);
      neighbors[i] = new int[set.size()];
      if (maxDeg < set.size()) {
        maxDeg = set.size();
      }
      if (set.size() < minDeg) {
        minDeg = set.size();
      }
      tmp = 0;
      for (int j : set) {
        neighbors[i][tmp] = j;
        tmp ++;
      }
    }
    currentNode = r.nextInt(numNodes);
  }
  
  @Override
  public int nextId(int numNodes, Random r) {
    if (neighbors == null) {
      init(numNodes, r);
    }
    int idx = 1;
    // random neighbor
    idx = neighbors[currentNode][r.nextInt(neighbors[currentNode].length)];
    
    // earliest seen
    int min = time;
    HashSet<Integer> mins = new HashSet<Integer>();
    for (int i = 0; i < neighbors[currentNode].length; i++) {
      if (lastSeenAt[neighbors[currentNode][i]] < min) {
        min = lastSeenAt[neighbors[currentNode][i]];
        // clear
        mins.clear();
        mins.add(neighbors[currentNode][i]);
      } else if (lastSeenAt[neighbors[currentNode][i]] == min) {
        // collect min idxs
        mins.add(neighbors[currentNode][i]);
      }
    }
    int ridx = r.nextInt(mins.size());
    for (int i : mins) {
      if (ridx == 0) {
        idx = i;
        break;
      }
      ridx --;
    }
    
    // probability of send to myself
    double p = (maxDeg - neighbors[currentNode].length) / (double)maxDeg;
    if (r.nextDouble() < p) {
      idx = currentNode;
    }
    
    lastSeenAt[idx] = time;
    time ++;
    currentNode = idx;
    
    // probability of skip update
    /*double ps = 1.0 - (minDeg / (double)neighbors[currentNode].length);
    if (r.nextDouble() < ps) {
      return -1;
    }*/
    
    return currentNode;
  }
}

class KRegular extends BipartiteGraph {
  protected void init(int numNodes, Random r) {
    if (k%2 == 1 && numNodes%2 == 1) {
      throw new RuntimeException("Can not be create " + k + " regular graph with " + numNodes + " nodes!");
    }
    lastSeenAt = new int[numNodes];
    neighbors = new int[numNodes][k];
    publicNodes = new int[numNodes];
    for (int i = 0; i < numNodes; i++) {
      lastSeenAt[i] = -1;
      publicNodes[i] = i;
    }
    Utils.arrayShuffle(r, publicNodes);
    for (int i = 0; i < numNodes; i++) {
      for (int j = 0; j < k>>>1; j++) {
        neighbors[publicNodes[i]][j<<1] = publicNodes[(i+j+1)%numNodes];
        neighbors[publicNodes[i]][(j<<1) + 1] = publicNodes[(numNodes+i-j-1)%numNodes];
      }
      if (k%2 == 1) {
        neighbors[publicNodes[i]][k-1] = publicNodes[(i+(numNodes/2))%numNodes];
      }
    }
    /*for (int i = 0; i < numNodes; i++) {
      System.out.print(i + ":");
      for (int j = 0; j < neighbors[i].length; j++) {
        System.out.print(" " + neighbors[i][j]);
      }
      System.out.println();
    }
    System.exit(0);*/
    currentNode = r.nextInt(numNodes);
  }
}
