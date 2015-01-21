package gossipLearning.utils;

import java.util.Arrays;

public class UserTrace {
  
  private String userName = "NA"; 
  private int timeZone = 0;
  private Long startDate = 0L;
  private Boolean isTimeZoned = false;
  private int pointer = 0;
  private Long[] sessions;
  private Boolean firstOnline;
  
  public UserTrace(Long[] sessions, Boolean firstOnline) {
    this.sessions = sessions;
    this.firstOnline = firstOnline;
  }
  
  public UserTrace(Long[] sessions, Boolean firstOnline, String userName, int timeZone, Long startDate) {
    this.userName = userName;
    this.timeZone = timeZone;
    this.startDate = startDate;
    this.sessions = sessions;
    this.firstOnline = firstOnline;
  }
  /**
   * copy constructor
   * @param ut
   */
  public UserTrace(UserTrace ut) {
    this.userName = ut.getUserName();
    this.timeZone = ut.getTimeZone();
    this.startDate = ut.getStartDate();
    this.isTimeZoned = ut.isTimeZoned();
    this.pointer = ut.getPointer(); 
    this.sessions = ut.getSessions();
    this.firstOnline = ut.isFirstOnline();
  }
  
  public void useTimeZone() {
    if(!isTimeZoned) {
      sessions[0] += timeZone*60*60*1000;
      isTimeZoned = true;
    }
  }
  
  public void switchOffTimeZone() {
    if(isTimeZoned) {
      sessions[0] -= timeZone*60*60*1000;
      isTimeZoned = false;
    }
  }
  
  public Boolean isTimeZoned() {
    return isTimeZoned;
  }
  
  public String getUserName() {
    return userName;
  }
  
  public Long getStartDate() {
    return startDate;
  }
  
  public void resetPointer() {
    pointer = 0;
  }
  public int getPointer() {
    return pointer;
  }
  public Long nextSession() {
    return sessions[pointer++];
  }
  public Long[] getSessions() {
    return sessions;
  }
  public int getTimeZone() {
    return timeZone;
  }
  public Boolean isFirstOnline() {
    return firstOnline;
  }
  public Boolean hasMoreSession() {
    return pointer < sessions.length;
  }
  
  public int getDayName() {
    int[] dayName = {3,4,5,6,0,1,2};
    int days = (int)(startDate/(24.0*60.0*60.0*1000.0));
    return dayName[days%7];
  }

  @Override
  public String toString() {
    return "UserTrace [userName=" + userName + ", startDate=" + startDate
        + ", timeZone=" + timeZone + ", isTimeZoned=" + isTimeZoned
        + ", pointer=" + pointer + ", sessions=" + Arrays.toString(sessions)
        + ", firstOnline=" + firstOnline + "]";
  }
  
}
