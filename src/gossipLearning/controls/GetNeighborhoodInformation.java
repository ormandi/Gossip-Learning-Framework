package gossipLearning.controls;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.*;
import peersim.transport.Transport;

public class GetNeighborhoodInformation implements Control {
  private static final String PROT = "protocol";
  private static final String TRACE_FILE = "traceFile";
  private final String fName;
  protected LinkedList<String> userTracesSampler;
  protected final LinkedList<String> userTraces;
  protected Map<Long, String> assignedUserTrace;
  private final int pid;

  public GetNeighborhoodInformation(String prefix) {
    //System.err.println("HELLO "+PROT+" "+prefix);
    pid = Configuration.getPid(prefix + "." + PROT);
    fName = Configuration.getString(prefix + "." + TRACE_FILE);
    BufferedReader br;
    userTraces = new LinkedList<String>();
    userTracesSampler = new LinkedList<String>();
    assignedUserTrace = new HashMap<Long, String>();
	try {
		br = new BufferedReader(new FileReader(fName));
		String line;
	    while ((line = br.readLine()) != null) {
	      if (line != null) {
	    	  userTraces.addLast(line);
	    	  userTracesSampler.addLast(line);
	      }
	    }
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	if(userTraces.size()>0) {
		Collections.shuffle(userTracesSampler, CommonState.r);
		for (int i = 0; i < Network.size(); i ++) {
	        Node node = Network.get(i);
	        assignedUserTrace.put(node.getID(), userTracesSampler.poll());
	        if (userTracesSampler.size() < 1) {
	        	Collections.copy(userTracesSampler, userTraces);
	        	Collections.shuffle(userTracesSampler, CommonState.r);
	        }
		}
	}
    
  }

  @Override
  public boolean execute() {
    String outLine = "{";
    long recentNodeId = Long.MIN_VALUE;
	String recentAssignedUserTrace = "";
	for (int i = 0; i < Network.size(); i++) {
	  if (recentNodeId > Long.MIN_VALUE) {
	    outLine += "\""+recentNodeId+"\": \""+recentAssignedUserTrace+"\", ";
	  }
      Node node = Network.get(i);
      recentNodeId = node.getID();
      recentAssignedUserTrace = assignedUserTrace.get(node.getID());
    }
	outLine += "\""+recentNodeId+"\": \""+recentAssignedUserTrace+"\"}";
    System.out.println(outLine);    
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
