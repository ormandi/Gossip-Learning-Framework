package peersim.transport;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
import java.util.*;

/**
 * The multipeersim-compatible version of ChurnTransport.
 *
 * @author Gabor Danner
 */
public class ChurnTransportM implements Transport
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/** 
 * The latency.
 * @config
 */	
private static final String PAR_DELAY = "delay";

/** 
 * Source of churn sessions.
 * @config
 */	
private static final String PAR_CP = "churnProvider";
	
//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** Delay for message sending. */
private final long delay;

/** Prototype source of churn sessions. */
private final ChurnProvider cp;

/** The churn session endpoints. */
private long[] ends;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * Reads configuration parameters.
 */
public ChurnTransportM(String prefix)
{
	delay = Configuration.getLong(prefix+"."+PAR_DELAY);
	if (delay<0) 
	   throw new IllegalParameterException(prefix+"."+PAR_DELAY,"The latency cannot be smaller than zero.");
	cp = (ChurnProvider)Configuration.getInstance(prefix+"."+PAR_CP);
	initEnds();
}

@Override
public ChurnTransportM clone()
{
	try {
		ChurnTransportM ct = (ChurnTransportM)super.clone();
		ct.initEnds();
		return ct;
	} catch (CloneNotSupportedException e) {
		throw new RuntimeException(e);
	}
}

/**
 * Initialization of ends.
 */
private void initEnds()
{
	ChurnProvider cp2 = cp.clone();
	List<Long> sessions = new ArrayList<Long>();
	long sum = 0;
	while (sum<CommonState.getEndTime()) {
		sum += cp2.nextSession();
		sessions.add(sum);
	}
	ends = new long[sessions.size()];
	int i = 0;
	for (Long s : sessions)
		ends[i++] = s;
}

//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

/**
 * Delivers the message with the configured delay, or drops it due to churn.
 */
@Override
public void send(Node src, Node dest, Object msg, int pid)
{
	if (search()==2&&((ChurnTransportM)dest.getProtocol(FastConfig.getTransport(pid))).search()==2)
		EDSimulator.add(delay,msg,dest,pid);
}

/**
 * Returns the configured delay.
 */
@Override
public long getLatency(Node src, Node dest)
{
	return delay;
}

/**
 * Returns wether the node is currently online, based on the churn provider.
 * Note: peersim node status is irrelevant.
 */
public boolean isOnline()
{
	return search()>0;
}

/**
 * 0: offline
 * 1: online, but won't remain online
 * 2: online, and will remain online
 */
private int search()
{
	long time = CommonState.getTime();
	int i = Arrays.binarySearch(ends,time);
	if (i<0)
		i = -i-1;
	while (ends[i]==time)
		i++;
	if (i%2==0)
		return 0;
	if (time+delay<ends[i])
		return 2;
	return 1;
}

}
