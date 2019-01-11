package peersim.transport;

import peersim.config.*;
import peersim.core.CommonState;

/**
 * Generates exponentially distributed churn sessions.
 *
 * @author Gabor Danner
 */
public class ExponentialChurn implements ChurnProvider
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/** 
 * Average length of online sessions (1/lambda).
 * @config
 */	
private static final String PAR_ONLINE = "online";

/** 
 * Average length of offline sessions (1/lambda).
 * @config
 */	
private static final String PAR_OFFLINE = "offline";

//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** Beta of the online session distribution. */
private final long beta_on;

/** Beta of the offline session distribution. */
private final long beta_off;

/** 1, if the last returned session was online, -1, if offline, and 0, if no session was returned yet. */
private int online;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * Reads configuration parameters.
 */
public ExponentialChurn(String prefix)
{
	beta_on = Configuration.getLong(prefix+"."+PAR_ONLINE);
	if (beta_on<=0) 
	   throw new IllegalParameterException(prefix+"."+PAR_ONLINE,"Average online session length must be positive.");
	beta_off = Configuration.getLong(prefix+"."+PAR_OFFLINE);
	if (beta_off<0) 
	   throw new IllegalParameterException(prefix+"."+PAR_OFFLINE,"Average offline session length must be non-negative.");
}

@Override
public ExponentialChurn clone()
{
	try {
		return (ExponentialChurn)super.clone();
	} catch (CloneNotSupportedException e) {
		throw new RuntimeException(e);
	}
}

//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

@Override
public long nextSession()
{
	if (online==0) {
		online = -1;
		if (CommonState.r.nextDouble()*(beta_on+beta_off)<beta_on)
			return 0;
	} else {
		online *= -1;
	}
	return (long)(-Math.log(CommonState.r.nextDouble())*(online>0?beta_on:beta_off));
}

}
