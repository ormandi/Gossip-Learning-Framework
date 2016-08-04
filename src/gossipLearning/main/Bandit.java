package gossipLearning.main;

import gossipLearning.controls.bandits.Machine;
import gossipLearning.utils.Utils;

import java.util.Random;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;

public class Bandit {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage: Bandit BanditConfig");
      System.exit(0);
    }
    
    // set up configuration parser
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    
    long seed = Configuration.getLong("SEED");
    int numIters = Configuration.getInt("ITERS");
    int N = Configuration.getInt("PEERS");
    int K = -1;
    Machine m;
    if (Configuration.contains("ARMS")) {
      String arms = Configuration.getString("ARMS");
      String[] split = arms.split(",");
      K = split.length;
      double[] mus = new double[K];
      for (int i = 0; i < K; i++) {
        mus[i] = Double.parseDouble(split[i]);
      }
      m = Machine.getInstance(seed, mus);
    } else {
      K = Configuration.getInt("K");
      m = Machine.getInstance(seed, K);
    }
    String alg = Configuration.getString("ALG");
    Random r = new Random(seed);
    
    System.err.println("SEED: " + seed);
    System.err.println("K: " + K);
    System.err.println("N: " + N);
    System.err.println("ALG: " + alg);
    System.err.print("ARMS:");
    for (int i = 0; i < K; i++) {
      System.err.print(" " + m.getArm(i).getMu());
    }
    System.err.println();
    
    if (alg.equals("sequential")) {
      playSequential(K, m, numIters, r);
    } else if (alg.equals("average")) {
      averaging(N, K, m, numIters, r);
    } else if (alg.equals("delayed")) {
      delayed(N, K, m, numIters, r);
    }
    
  }
  
  public static void playSequential(int K, Machine m, int numIters, Random r) {
    double[] plays = new double[K];
    double[] rewards = new double[K];
    
    System.out.println("#iter\tacc\tregret");
    for (int iter = 0; iter < numIters + 1; iter ++) {
      System.out.println(iter + "\t" + m.getPrecision() + "\t" + m.getRegret());
      int I = -1;
      double theta = 0.0;
      double max = 0.0;
      for (int k = 0; k < K; k++) {
        theta = Utils.nextBetaFast(rewards[k] + 1, plays[k] - rewards[k] + 1, r);
        if (theta > max) {
          max = theta;
          I = k;
        }
      }
      double xi = Machine.getInstance().play(I);
      rewards[I] += xi;
      plays[I]++;
    }
  }
  
  public static void averaging(int N, int K, Machine m, int numIters, Random r) {
    double[][] plays = new double[N + 1][K];
    double[][] rewards = new double[N + 1][K];
    
    System.out.println("#iter\tacc\tregret");
    for (int iter = 0; iter < numIters + 1; iter ++) {
      System.out.println(iter + "\t" + m.getPrecision() + "\t" + m.getRegret());
      for (int i = 0; i < N; i++) {
        int I = -1;
        double theta = 0.0;
        double max = 0.0;
        for (int k = 0; k < K; k++) {
          theta = Utils.nextBetaFast(rewards[i][k] + 1, plays[i][k] - rewards[i][k] + 1, r);
          if (theta > max) {
            max = theta;
            I = k;
          }
        }
        double xi = Machine.getInstance().play(I);
        rewards[i][I] += xi;
        plays[i][I]++;
        rewards[N][I] += xi;
        plays[N][I]++;
      }
      for (int i = 0; i < N; i++) {
        for (int k = 0; k < K; k++) {
          plays[i][k] = plays[N][k] / N;
          rewards[i][k] = rewards[N][k] / N;
        }
      }
    }
  }
  
  public static void delayed(int N, int K, Machine m, int numIters, Random r) {
    double[] playsIndependently = new double[K];
    double[] rewardsIndependently = new double[K];
    
    double[] playsForRound = new double[K];
    double[] rewardsForRound = new double[K];
    
    System.out.println("#iter\tacc\tregret");
    for (int iter = 0; iter < numIters + 1; iter ++) {
      System.out.println(iter + "\t" + m.getPrecision() + "\t" + m.getRegret());
      for (int i = 0; i < N; i++) {
        int I = -1;
        double theta = 0.0;
        double max = 0.0;
        for (int k = 0; k < K; k++) {
          theta = Utils.nextBetaFast(rewardsForRound[k] + 1, playsForRound[k] - rewardsForRound[k] + 1, r);
          if (theta > max) {
            max = theta;
            I = k;
          }
        }
        double xi = Machine.getInstance().play(I);
        rewardsIndependently[I] += xi;
        playsIndependently[I]++;
      }
      for (int k = 0; k < K; k++) {
        rewardsForRound[k] += rewardsIndependently[k];
        playsForRound[k] += playsIndependently[k];
        rewardsIndependently[k] = 0;
        playsIndependently[k] = 0;
      }
    }
  }

}
