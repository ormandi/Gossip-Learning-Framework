package peersim.transport;

/**
 * Provides infinite online churn sessions.
 *
 * @author Gabor Danner
 */
public class NoChurn implements ChurnProvider
{

//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** True, if the next session to be returned is online. */
private boolean online;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

public NoChurn(String prefix)
{
}

@Override
public NoChurn clone()
{
	try {
		return (NoChurn)super.clone();
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
	if (online)
		return Long.MAX_VALUE;
	online = true;
	return 0;
}

}
