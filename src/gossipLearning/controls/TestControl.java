package gossipLearning.controls;

import java.util.TreeSet;

import gossipLearning.models.TestModel;
import gossipLearning.protocols.TestProtocol;
import gossipLearning.utils.Utils;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;

public class TestControl implements Control {
  private static final String PAR_PID = "protocol";
  protected final int pid;
  private static final String PAR_OID = "overlay";
  protected final int oid;
  
  private final int numModels = 1;
  
  private int exec;
  private int[] indices;
  
  public TestControl(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PID);
    oid = Configuration.getPid(prefix + "." + PAR_OID);
    exec = 0;
  }

  @Override
  public boolean execute() {
    if (exec == 0) {
      for (int i = 0; i < Network.size(); i++) {
        /*System.out.print(Network.get(i).getID() + ":");
        Linkable l = (Linkable)Network.get(i).getProtocol(oid);
        if (l.degree() > 10) {
          //System.out.println(Network.get(i).getID() + ":");
        }
        for (int j = 0; j < l.degree(); j++) {
          System.out.print(" " + l.getNeighbor(j).getID());
        }
        System.out.println();*/
        
        // neighbor init
        ((TestProtocol)Network.get(i).getProtocol(pid)).initNeighbors((Linkable)Network.get(i).getProtocol(oid));
      }
      
      exec ++;
      return false;
    }
    if (exec == 1) {
      // init
      indices = new int[Network.size()];
      for (int i = 0; i < indices.length; i ++) {
        indices[i] = i;
      }
      Utils.arrayShuffle(CommonState.r, indices);
      for (int i = 0; i < numModels && i < indices.length; i++) {
        ((TestProtocol)Network.get(indices[i]).getProtocol(pid)).passiveThread(null);
      }
    }
    // eval
    TreeSet<Long> idSet = new TreeSet<Long>();
    //TestModel model = (TestModel)((TestProtocol)Network.get(0).getProtocol(pid)).getModel();
    //idSet.add(model.getId());
    TestModel model = null;
    double minAge = 0.0;
    double maxAge = 0.0;
    double avgAge = 0.0;
    long minId = 0;
    long maxId = 0;
    for (int i = 0; i < Network.size(); i++) {
      TestModel m = (TestModel)((TestProtocol)Network.get(i).getProtocol(pid)).getModel();
      if (m == null) {
        continue;
      } else if (idSet.size() == 0) {
        minAge = m.getAge();
        maxAge = m.getAge();
        avgAge = m.getAge();
        idSet.add(m.getId());
        minId = i;
        maxId = i;
        model = m;
      } else {
        idSet.add(m.getId());
        if (m.getAge() > model.getAge()) {
          model = m;
          maxAge = model.getAge();
          maxId = i;
        }
        if (m.getAge() < minAge) {
          minAge = m.getAge();
          minId = i;
        }
        avgAge += m.getAge();
      }
    }
    avgAge /= Network.size();
    System.out.println(exec + "\t" + minAge + "\t" + minId + "\t" + maxAge + "\t" + maxId + "\t" + avgAge + "\t" + idSet.size() + "\t" + model);
    exec ++;
    if (exec > 3000) {
      return true;
    }
    return false;
  }

}
