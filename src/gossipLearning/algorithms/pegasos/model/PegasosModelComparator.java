package gossipLearning.algorithms.pegasos.model;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class PegasosModelComparator implements Comparator<PegasosModel> {
  public int compare(PegasosModel a, PegasosModel b) {
    Set<Integer> u = new TreeSet<Integer>();
    u.addAll(a.getW().keySet());
    u.addAll(b.getW().keySet());
    for (int i : u) {
      Double aV = a.getW().get(i);
      double aVd = (aV == null) ? 0.0 : aV.doubleValue();
      Double bV = b.getW().get(i);
      double bVd = (bV == null) ? 0.0 : bV.doubleValue();
      if (aVd < bVd) {
        return -1;
      } else if (aVd > bVd) {
        return 1;
      }
    }
    return 0;
  }

}
