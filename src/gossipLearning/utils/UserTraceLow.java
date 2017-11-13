package gossipLearning.utils;

public class UserTraceLow {
  final Session[] sessions;
  
  public UserTraceLow(Session[] sessions) {
    this.sessions = sessions;
  }
  
  public boolean isFirstOnline(){
    return sessions[0].getType()==1;
  }
}
