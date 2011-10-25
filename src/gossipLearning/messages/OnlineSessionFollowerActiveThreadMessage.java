package gossipLearning.messages;

/**
 * This message class can be viewed as an extended version
 * of the {@link gossipLearning.messages.ActiveThreadMessage}.
 * It can be used when churn modeling is performed. It can
 * identify that the active thread alarm is set in the 
 * current session or not.
 * 
 * @author Róbert Ormándi
 */
public class OnlineSessionFollowerActiveThreadMessage implements Message {
  public final int sessionID;
  public OnlineSessionFollowerActiveThreadMessage(int sessionID) {
    this.sessionID = sessionID;
  }
}
