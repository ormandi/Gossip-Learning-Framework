package gossipLearning.messages;

@Message
public class OnlineSessionFollowerActiveThreadMessage {
  public final int sessionID;
  public OnlineSessionFollowerActiveThreadMessage(int sessionID) {
    this.sessionID = sessionID;
  }
}
