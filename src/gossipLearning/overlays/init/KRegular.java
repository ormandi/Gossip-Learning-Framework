package gossipLearning.overlays.init;

import gossipLearning.utils.Utils;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;

public class KRegular extends WireGraph {
  
  private static final String PAR_DEGREE = "k";
  private final int k;
  
  public KRegular(String prefix) {
    super(prefix);
    k = Configuration.getInt(prefix + "." + PAR_DEGREE);
  }

  @Override
  public void wire(Graph g) {
    int n = g.size();
    if (k%2 == 1 && n%2 == 1) {
      throw new RuntimeException("Can not be create " + k + " regular graph with " + n + " nodes!");
    }
    int[] nodes = new int[n];
    for (int i = 0; i < n; i++) {
      nodes[i] = i;
    }
    Utils.arrayShuffle(CommonState.r, nodes);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < k>>>1; j++) {
        // set the next k/2 as a neighbor
        g.setEdge(nodes[i], nodes[(i+j+1)%n]);
        // set the previous k/2 as a neighbor
        g.setEdge(nodes[i], nodes[(n+i-j-1)%n]);
      }
      if (k%2 == 1) {
        // set the opposite as a neighbor
        g.setEdge(nodes[i], nodes[(i+(n/2))%n]);
      }
    }
  }

}
