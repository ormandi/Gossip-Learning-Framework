package gossipLearning.overlays.init;

import gossipLearning.utils.Utils;

import java.util.TreeSet;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;

public class PublicPrivate extends WireGraph {

  private static final String PAR_DEGREE = "k";
  private final int k;
  
  private static final String PAR_PFRAC = "publicFraction";
  private final double pFrac;
  
  public PublicPrivate(String prefix) {
    super(prefix);
    k = Configuration.getInt(prefix + "." + PAR_DEGREE);
    pFrac = Configuration.getDouble(prefix + "." + PAR_PFRAC);
  }

  @Override
  public void wire(Graph g) {
    int n = g.size();
    TreeSet<Integer> pub = new TreeSet<Integer>();
    for (int i = 0; i < n; i++) {
      double rand = CommonState.r.nextDouble();
      if (rand < pFrac) {
        pub.add(i);
      }
    }
    System.out.println(pub);
    //System.exit(0);
    int[] nodes = new int[pub.size()];
    int tmp = 0;
    for (int i : pub) {
      nodes[tmp] = i;
      tmp ++;
    }
    for (int i = 0; i < n; i++) {
      if (!pub.contains(i)) {
        Utils.arrayShuffle(CommonState.r, nodes);
        int ns = Math.min(k, pub.size());
        for (int j = 0; j < ns; j++) {
          g.setEdge(i, nodes[j]);
          g.setEdge(nodes[j], i);
        }
      }
    }
  }

}
