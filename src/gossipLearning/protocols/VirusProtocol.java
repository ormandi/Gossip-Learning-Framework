package gossipLearning.protocols;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.protocols.AbstractProtocol;
import gossipLearning.messages.ModelMessage;
import gossipLearning.models.Virus;
import gossipLearning.utils.BQModelHolder;
import peersim.core.CommonState;
import peersim.core.Fallible;
import peersim.core.Linkable;
import peersim.core.Node;

public class VirusProtocol extends AbstractProtocol {
  
  protected ModelHolder holder;

  public VirusProtocol(String prefix) {
    super(prefix);
    holder = new BQModelHolder(1);
    Virus model = new Virus(prefix);
    holder.add(model);
  }
  
  public VirusProtocol(VirusProtocol a) {
    super(a.prefix);
    holder = (ModelHolder)a.holder.clone();
  }
  
  public Object clone() {
    return new VirusProtocol(this);
  }

  @Override
  public void activeThread() {
    sendToRandomNeighbor(new ModelMessage(currentNode, holder, currentProtocolID));
  }

  @Override
  public void passiveThread(ModelMessage message) {
    Virus model = (Virus)message.getModel(0);
    model.update((int)currentNode.getID());
    if (((Virus)holder.getModel(0)).isInfecter()) {
      model.setInfected();
    }
    holder.add(model);
  }
  
  @Override
  protected void sendToRandomNeighbor(ModelMessage message) {
    Linkable overlay = getOverlay();
    Node[] online = new Node[overlay.degree()];
    int counter = 0;
    for (int i = 0; i < overlay.degree(); i++) {
      if (overlay.getNeighbor(i).getFailState() == Fallible.OK) {
        online[counter] = overlay.getNeighbor(i);
        counter ++;
      }
    }
    if (counter == 0) {
      return;
    }
    Node randomNode = online[CommonState.r.nextInt(counter)];
    getTransport().send(currentNode, randomNode, message, currentProtocolID);
  }
  
  public Model getModel() {
    return holder.getModel(holder.size() - 1);
  }

}
