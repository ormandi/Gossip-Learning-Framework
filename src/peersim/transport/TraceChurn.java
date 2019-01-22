package peersim.transport;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.util.RandPermutation;
import java.io.*;
import java.util.*;

/**
 * Reads churn sessions from a file.
 *
 * @author Gabor Danner
 */
public class TraceChurn implements ChurnProvider
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/** 
 * The location of the file containing the session sequences.
 * @config
 */	
private static final String PAR_FILE = "traceFile";

//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** List of session sequences. */
private final List<String> userTraces;

/** Used to select a sequence. */
private final RandPermutation rp;

/** The sequence used by the current instance. */
private StringTokenizer tokens;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * Reads configuration parameters and the trace file.
 */
public TraceChurn(String prefix)
{
	final String fName = Configuration.getString(prefix + "." + PAR_FILE);
	userTraces = new ArrayList<String>();
	try {
		BufferedReader br = new BufferedReader(new FileReader(fName));
		String line;
		while ((line = br.readLine()) != null)
			userTraces.add(line);
		br.close();
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
	rp = new RandPermutation(CommonState.r);
}

@Override
public TraceChurn clone()
{
	try {
		return (TraceChurn)super.clone();
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
	if (tokens==null) {
		if (!rp.hasNext())
			rp.reset(userTraces.size());
		tokens = new StringTokenizer(userTraces.get(rp.next()));
		tokens.nextToken(); //username
		boolean online = '1' == tokens.nextToken().charAt(0);
		Long.parseLong(tokens.nextToken()); //startDate
		Integer.parseInt(tokens.nextToken()); //timeZone
		if (online)
			return 0;
	}
	if (!tokens.hasMoreTokens())
		throw new RuntimeException("EndTime ("+CommonState.getEndTime()+") is greater than the length of the trace!");
	return Long.parseLong(tokens.nextToken());
}

}
