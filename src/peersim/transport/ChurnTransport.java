package peersim.transport;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;

/**
 * Implements a transport layer that simulates churn.
 * It delivers messages with the given delay if and only if both nodes stay online for the whole duration,
 * according to the churn sessions drawn from the configured source.
 *
 * @author Gabor Danner
 */
public class ChurnTransport implements Transport
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

/** Source of churn sessions. */
private ChurnProvider cp;

/** The point in time where the current churn session ends. */
private long end;

/** Type of the current churn session. */
private boolean online = true;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * Reads configuration parameters.
 */
public ChurnTransport(String prefix)
{
	delay = Configuration.getLong(prefix+"."+PAR_DELAY);
	if (delay<0) 
	   throw new IllegalParameterException(prefix+"."+PAR_DELAY,"The latency cannot be smaller than zero.");
	cp = (ChurnProvider)Configuration.getInstance(prefix+"."+PAR_CP);
}

@Override
public ChurnTransport clone()
{
	try {
		ChurnTransport ct = (ChurnTransport)super.clone();
		ct.cp = cp.clone();
		return ct;
	} catch (CloneNotSupportedException e) {
		throw new RuntimeException(e);
	}
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
	if (remainsOnline()&&((ChurnTransport)dest.getProtocol(FastConfig.getTransport(pid))).remainsOnline())
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
	while (CommonState.getTime()>=end) {
		end += cp.nextSession();
		online = !online;
	}
	return online;
}

private boolean remainsOnline()
{
	return isOnline()&&CommonState.getTime()+delay<end;
}

}
