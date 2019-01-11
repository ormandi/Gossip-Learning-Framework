package peersim.transport;

/**
 * Source of churn sessions.
 *
 * @author Gabor Danner
 */
public interface ChurnProvider extends Cloneable {

	/**
	 * Returns the length of the next session.
	 * Sessions alternate between online and offline sessions, starting with *offline*.
	 * Sessions may have a length of zero.
	 */
	long nextSession();
	
	ChurnProvider clone();

}
