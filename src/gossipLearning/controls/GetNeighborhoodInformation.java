package gossipLearning.controls;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.*;
import peersim.transport.Transport;

public class GetNeighborhoodInformation implements Control {
  private static final String PROT = "protocol";

  private final int pid;

  public GetNeighborhoodInformation(String prefix) {
    //System.err.println("HELLO "+PROT+" "+prefix);
    pid = Configuration.getPid(prefix + "." + PROT);
  }

  @Override
  public boolean execute() {
    System.out.println("{");
    String nextOutLine = "";
    for (int i = 0; i < Network.size(); i++) {
      Node node = Network.get(i);
      if (!nextOutLine.equals("")) {
        System.out.println(nextOutLine+",");
      }
      nextOutLine = "\"" +node.getID()+"\":[ ";
      Linkable overlay = getOverlay(node);
      String nextNeighbor = "";
      ///System.err.println(overlay.degree());
      for (int j = 0; j < overlay.degree(); j++ ){
        if(!nextNeighbor.equals("")){
          nextOutLine+="\"" +nextNeighbor+"\", ";
        }
        Node neighbor = overlay.getNeighbor(j);
        nextNeighbor = ""+neighbor.getID();
      }
      nextOutLine+="\"" +nextNeighbor+"\" ]";
    }
    System.out.println(nextOutLine);
    System.out.println("}");
    System.exit(0);
    return false;
  }
  /**
   * It is method which makes more easer of the accessing to the transport layer of the current node.
   *
   * @return The transform layer is returned.
   */
  protected Transport getTransport(Node node) {
    return ((Transport) node.getProtocol(FastConfig.getTransport(pid)));
  }

  /**
   * This method supports the accessing of the overlay of the current node.
   *
   * @return The overlay of the current node is returned.
   */
  protected Linkable getOverlay(Node node) {
    return (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
  }
}
