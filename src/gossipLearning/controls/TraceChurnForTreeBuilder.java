package gossipLearning.controls;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import gossipLearning.protocols.TreeBuilderProtocol;
import gossipLearning.utils.Session;
import gossipLearning.utils.TracePointer;
import gossipLearning.utils.UserTraceLow;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.SchedulerI;
import peersim.util.RandPermutation;

public class TraceChurnForTreeBuilder implements Control, SchedulerI {
	
  private static final String PROT = "protocol";
  private final int pid;
  
	private static final String TRACE_FILE = "traceFile";
	
  private static final String STEP = "step";
  private final long step;

	TreeMap<Session, List<TracePointer> > heap = new TreeMap<Session, List<TracePointer> >();

	public TraceChurnForTreeBuilder(String prefix) {
		final String fName = Configuration.getString(prefix + "." + TRACE_FILE);
		pid = Configuration.getPid(prefix + "." + PROT);
		step = Configuration.getLong(prefix + "." + STEP);
		Vector<UserTraceLow> userTraces = new Vector<UserTraceLow>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fName));
			String line;
			while ((line = br.readLine()) != null) {
				Vector<Session> sessions = new Vector<Session>();
				StringTokenizer tokens = new StringTokenizer(line);
				tokens.nextToken(); //username
				Boolean online = '1' == tokens.nextToken().charAt(0);
				Long.parseLong(tokens.nextToken()); //startDate
				Integer.parseInt(tokens.nextToken()); //timeZone
				long sum = 0;
				boolean nextStatus = online;
				while (tokens.hasMoreTokens()) {
					sum += Long.parseLong(tokens.nextToken())*step;
					sessions.add(new Session((nextStatus?1:0),sum));
					nextStatus=!nextStatus;
				}
				userTraces.add(new UserTraceLow(sessions.toArray(new Session[sessions.size()])));
			}
			br.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		RandPermutation rp=new RandPermutation(CommonState.r);
		for (int i = 0; i < Network.size(); i ++) {
			Node node = Network.get(i);
			if (!rp.hasNext()) rp.reset(userTraces.size());
			UserTraceLow ut = userTraces.get(rp.next());
			node.setFailState(ut.isFirstOnline() ? Fallible.OK : Fallible.DOWN);
			TracePointer tp = new TracePointer(ut,node);
			if (tp.hasMoreSession())///
				insert(tp.nextSession(),tp);
		}
	}
	
	public boolean execute() {
		assert heap.firstKey().getLength() == CommonState.getTime();
		for (TracePointer tp : heap.pollFirstEntry().getValue()) {
			Node node = tp.getNode();
			if (node.getFailState() == Fallible.DEAD)
				continue;
	    Session nextSession = tp.nextSession();
			if(nextSession.getType() == 0) {
			  ((TreeBuilderProtocol)node.getProtocol(pid)).quitFromTree(node, pid);
			  node.setFailState(Fallible.DOWN);
			} else if (nextSession.getType() == 1) {
			  node.setFailState(Fallible.OK);
			  ((TreeBuilderProtocol)node.getProtocol(pid)).wakeUpAndForgetEverything(node,pid);
			}
			//System.out.println("ConnectionTIMEOUT -1269205417 81588 "+CommonState.getTime()+" "+node.isUp());
			//System.err.println("CONNECTIVTY_CHANGED "+node.getID()+" "+node.isUp()+" "+(nextSession.getType() == 1)+" "+nextSession.getLength()+" "+((TreeBuilderProtocol)node.getProtocol(pid)).isFreeNode() + " "+((TreeBuilderProtocol)node.getProtocol(pid)).getTreeID() );
			if (tp.hasMoreSession())
				insert(nextSession,tp);
		}
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
  void insert(Session key, TracePointer value) {
		if (!heap.containsKey(key))
			heap.put(key,new LinkedList());
		heap.get(key).add(value);
	}

	public long getNext() {
		if (heap.isEmpty())
			return -1;
		assert heap.firstKey().getLength() > CommonState.getTime();
		return heap.firstKey().getLength();
	}

	public boolean afterSimulation() { return false; }

	public boolean active(long time) { throw new UnsupportedOperationException("Not implemented for efficiency."); }

	public boolean active() { throw new UnsupportedOperationException("Not implemented for efficiency."); }

}