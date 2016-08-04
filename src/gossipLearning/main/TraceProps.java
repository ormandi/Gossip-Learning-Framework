package gossipLearning.main;

import gossipLearning.utils.UserTrace;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import peersim.core.Fallible;

public class TraceProps {
  public static void main(String[] args) throws Exception {
    String fName = "../res/trace/peersim_session_twoday_100.txt";
    Vector<UserTrace> userTraces = new Vector<UserTrace>();
    
    BufferedReader br = new BufferedReader(new FileReader(fName));
    String line;
    while ((line = br.readLine()) != null) {
      if (line != null) {
        Vector<Long> sessions = new Vector<Long>();
        StringTokenizer tokens = new StringTokenizer(line);
        String username = tokens.nextToken();
        String onlineToken = tokens.nextToken();
        Boolean online = ('1' == onlineToken.charAt(0));
        Long startDate = Long.parseLong(tokens.nextToken());
        int timeZone = Integer.parseInt(tokens.nextToken());
        sessions.add(Long.parseLong(tokens.nextToken()));    
        while (tokens.hasMoreTokens()) {
          sessions.add(Long.parseLong(tokens.nextToken()));
        }
        Long[] sessArr = sessions.toArray(new Long[sessions.size()]);
        userTraces.add(new UserTrace(sessArr, online, username, timeZone, startDate));
      }
    }
    br.close();
    
    long[] sessionLength = new long[userTraces.size()];
    int[] state = new int[userTraces.size()];
    int numOnline = 0;
    int numOk = 0;
    int numDown = 0;
    int numNew = 0;
    HashSet<Integer> set = new HashSet<Integer>();
    for (int i = 0; i < userTraces.size(); i++) {
      UserTrace ut = userTraces.get(i);
      if (ut.isFirstOnline()) {
        state[i] = Fallible.OK;
        numOnline ++;
        set.add(i);
      } else {
        state[i] = Fallible.DOWN;
      }
      sessionLength[i] = ut.nextSession();
    }
    
    int unitsInSteps = 60000;
    int steps = 3000;
    numNew = numOnline;
    System.out.println("#time\tonline\tup\tdown\tnew\tcum\tsize");
    for (long t = 0; t < steps + 1; t ++) {
      //System.out.println(t + "\t" + numOnline / userTraces.size() + "\t" + numOk / userTraces.size() + "\t" + numDown / userTraces.size());
      System.out.println(t + "\t" + numOnline + "\t" + numOk + "\t" + numDown + "\t" + numNew + "\t" + set.size() + "\t" + userTraces.size());
      numOk = 0;
      numDown = 0;
      numNew = 0;
      for (int i = 0; i < userTraces.size(); i ++) {
        int prevState = state[i];
        sessionLength[i] -= unitsInSteps;
        while (sessionLength[i] <= 0L) {
          if (state[i] == Fallible.OK) {
            state[i] = Fallible.DOWN;
          } else {
            state[i] = Fallible.OK;
          }
          UserTrace ut = userTraces.get(i);
          long sl = 0;
          if (ut.hasMoreSession()) {
            sl = ut.nextSession();
          } else {
            ut.resetPointer();
            if (ut.isFirstOnline()) {
              state[i] = Fallible.OK;
            } else {
              state[i] = Fallible.DOWN;
            }
            sl = ut.nextSession();
          }
          sessionLength[i] += sl;
        }
        
        if (prevState != state[i]) {
          if (prevState == Fallible.OK) {
            numOnline --;
            numDown ++;
          } else {
            numOnline ++;
            numOk ++;
            if (!set.contains(i)) {
              set.add(i);
              numNew ++;
            }
          }
        }
      }
    }
  }
}
