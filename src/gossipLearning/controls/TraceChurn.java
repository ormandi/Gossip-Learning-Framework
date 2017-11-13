package gossipLearning.controls;

import java.io.*;
import java.util.*;
import peersim.util.*;

import peersim.config.Configuration;
import peersim.core.*;

public class TraceChurn implements Control, SchedulerI {
	
	private static final String TRACE_FILE = "traceFile";

	TreeMap<Long, List<TracePointer> > heap = new TreeMap<Long, List<TracePointer> >();

	public TraceChurn(String prefix) {
		final String fName = Configuration.getString(prefix + "." + TRACE_FILE);
		Vector<UserTrace> userTraces = new Vector<UserTrace>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fName));
			String line;
			while ((line = br.readLine()) != null) {
				Vector<Long> sessions = new Vector<Long>();
				StringTokenizer tokens = new StringTokenizer(line);
				tokens.nextToken(); //username
				Boolean online = '1' == tokens.nextToken().charAt(0);
				Long.parseLong(tokens.nextToken()); //startDate
				Integer.parseInt(tokens.nextToken()); //timeZone
				long sum = 0;
				while (tokens.hasMoreTokens()) {
					sum += Long.parseLong(tokens.nextToken());
					sessions.add(sum);
				}
				userTraces.add(new UserTrace(sessions.toArray(new Long[sessions.size()]), online));
			}
			br.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		RandPermutation rp=new RandPermutation(CommonState.r);
		for (int i = 0; i < Network.size(); i ++) {
			Node node = Network.get(i);
			if (!rp.hasNext()) rp.reset(userTraces.size());
			UserTrace ut = userTraces.get(rp.next());
			node.setFailState(ut.firstOnline ? Fallible.OK : Fallible.DOWN);
			TracePointer tp = new TracePointer(ut,node);
			if (tp.hasMoreSession())///
				insert(tp.nextSession(),tp);
		}
	}
	
	public boolean execute() {
		assert heap.firstKey() == CommonState.getTime();
		for (TracePointer tp : heap.pollFirstEntry().getValue()) {
			Node node = tp.node;
			if (node.getFailState() == Fallible.DEAD)
				continue;
			node.setFailState(node.isUp() ? Fallible.DOWN : Fallible.OK);
			if (tp.hasMoreSession())
				insert(tp.nextSession(),tp);
		}
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
  void insert(Long key, TracePointer value) {
		if (!heap.containsKey(key))
			heap.put(key,new LinkedList());
		heap.get(key).add(value);
	}

	public long getNext() {
		if (heap.isEmpty())
			return -1;
		assert heap.firstKey() > CommonState.getTime();
		return heap.firstKey();
	}

	public boolean afterSimulation() { return false; }

	public boolean active(long time) { throw new UnsupportedOperationException("Not implemented for efficiency."); }

	public boolean active() { throw new UnsupportedOperationException("Not implemented for efficiency."); }

}

class TracePointer {

	final UserTrace ut;
	final Node node;
	int pointer = 0;
	
	public TracePointer(UserTrace ut, Node node) {
		this.ut = ut;
		this.node = node;
	}
	
	public Long nextSession() {
		return ut.sessions[pointer++];
	}
	
	public Boolean hasMoreSession() {
		return pointer < ut.sessions.length;
	}
	
}

class UserTrace {
	
	final Long[] sessions;
	final Boolean firstOnline;
	
	public UserTrace(Long[] sessions, Boolean firstOnline) {
		this.sessions = sessions;
		this.firstOnline = firstOnline;
	}
	
}