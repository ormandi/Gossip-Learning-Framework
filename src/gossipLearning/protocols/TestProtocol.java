package gossipLearning.protocols;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.protocols.AbstractProtocol;
import gossipLearning.messages.ModelMessage;
import gossipLearning.models.TestModel;
import gossipLearning.utils.BQModelHolder;
import gossipLearning.utils.Utils;

import java.util.PriorityQueue;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;

public class TestProtocol extends AbstractProtocol {
  private static final String PAR_STEP = "step";
  
  private ModelHolder models;
  private double budget;
  private double maxBudget;
  
  private long lastBudgetTime;
  private final long step;
  
  private PriorityQueue<NB> neighbors;
  private NB[] mins;
  
  public TestProtocol(String prefix) {
    super(prefix);
    models = new BQModelHolder(10);
    maxBudget = 10000;
    //budget = CommonState.r.nextDouble() * (maxBudget / 2);
    budget = 10;
    lastBudgetTime = CommonState.getTime();
    step = Configuration.getLong(prefix + "." + PAR_STEP); 
  }
  
  protected TestProtocol(TestProtocol a) {
    super(a);
    models = (ModelHolder)a.models.clone();
    maxBudget = a.maxBudget;
    //budget = CommonState.r.nextDouble() * (maxBudget / 2);
    budget = 10;
    lastBudgetTime = a.lastBudgetTime;
    step = a.step;
  }
  
  @Override
  public Object clone() {
    return new TestProtocol(this);
  }

  @Override
  public void activeThread() {
    // update budget
    long time = CommonState.getTime();
    while (time - lastBudgetTime >= step) {
      lastBudgetTime += step;
      budget ++;
    }
    if (budget > maxBudget) {
      // send model
      TestModel m;
      if (models.size() == 0) {
        m = new TestModel("");
      } else {
        m = (TestModel)models.getModel(0);
        for (int i = 1; i < models.size(); i++) {
          if (m.getAge() < ((TestModel)models.getModel(i)).getAge()) {
            m = (TestModel)models.getModel(i);
          }
        }
        //m = ((TestModel)models.getModel(models.size() - 1)).setId();
      }
      sendModel(m);
      budget --;
    }
  }

  @Override
  public void passiveThread(ModelMessage message) {
    TestModel model;
    if (message == null) {
      // init
      model = new TestModel("");
    } else {
      // update and store
      model = (TestModel)message.getModel(0);
      Node src = message.getSource();
      //System.out.println(currentNode.getID() + " " + neighbors + " <- " + src.getID());
      onRecv(src);
      
      for (int i = 0; i < getOverlay().degree(); i++) {
        ((TestProtocol)getOverlay().getNeighbor(i).getProtocol(currentProtocolID)).onRecv(currentNode);
      }
    }
    model.update((int)currentNode.getID(), CommonState.getTime());
    models.add(model);
    // send
    if (budget >= 1.0) {
      sendModel(model);
      budget --;
    }
  }
  
  public void sendModel(Model model) {
    BQModelHolder mh = new BQModelHolder(1);
    mh.add(model);
    ModelMessage message = new ModelMessage(currentNode, mh, currentProtocolID, true);
    sendToRandomNeighbor(message);
    //sendToNextNeighbor(message);
  }
  
  public void initNeighbors(Linkable overlay) {
    if (neighbors == null) {
      mins = new NB[overlay.degree()];
      neighbors = new PriorityQueue<TestProtocol.NB>();
      for (int i = 0; i < overlay.degree(); i++) {
        NB nb = new NB();
        nb.node = overlay.getNeighbor(i);
        nb.time = CommonState.getTime();
        neighbors.add(nb);
      }
    }
  }
  
  public void sendToNextNeighbor(ModelMessage message) {
    // TODO: works with static overlay only!!!
    //System.out.println(currentNode.getID() + " " + neighbors);
    long time = neighbors.peek().time;
    int idx = 0;
    while (neighbors.size() > 0 && neighbors.peek().time == time) {
      mins[idx] = neighbors.poll();
      idx++;
    }
    Utils.arrayShuffle(CommonState.r, mins, 0, idx);
    NB nb = mins[0];
    //System.out.println(nb);
    Node randomNode = nb.node;
    getTransport().send(currentNode, randomNode, message, currentProtocolID);
    nb.time = CommonState.getTime() + 1;
    neighbors.add(nb);
    for (int i = 1; i < idx; i++) {
      neighbors.add(mins[i]);
    }
    //System.out.println(neighbors + " " + nb + "\n");
  }
  
  private void onRecv(Node node) {
    NB nb = new NB();
    nb.node = node;
    nb.time = CommonState.getTime();
    neighbors.remove(nb);
    neighbors.add(nb);
  }
  
  public Model getModel() {
    if (models.size() == 0) {
      //models.add(new TestModel(""));
      return null;
    }
    return models.getModel(models.size() - 1);
  }
  
  private class NB implements Comparable<NB> {
    public Node node;
    public long time;
    
    @Override
    public int compareTo(NB o) {
      if (time < o.time) {
        return -1;
      } 
      if (time > o.time) {
        return 1;
      }
      return 0;
    }
    
    @Override
    public boolean equals(Object o) {
      if (o instanceof NB) {
        if (node.getID() == ((NB)o).node.getID()) {
          return true;
        }
      }
      return false;
    }
    
    public String toString() {
      return node.getID() + "-" + time;
    }
  }

}
