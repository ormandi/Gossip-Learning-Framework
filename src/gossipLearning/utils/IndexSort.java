package gossipLearning.utils;

import java.util.Arrays;
import java.util.Comparator;

public class IndexSort <T extends Comparable<T>> implements Comparator<Integer> {
  private final T[] array;
  private IndexSort(T[] array) {
    this.array = array;
  }
  
  private Integer[] getIndices() {
    Integer[] indices = new Integer[array.length];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = i;
    }
    return indices;
  }

  @Override
  public int compare(Integer arg0, Integer arg1) {
    return array[arg0].compareTo(array[arg1]);
  }
  
  public static<T extends Comparable<T>> int[] sort(T[] array) {
    IndexSort<T> comparator = new IndexSort<T>(array);
    Integer[] indices = comparator.getIndices();
    Arrays.sort(indices, comparator);
    int[] result = new int[indices.length];
    for (int i = 0; i < indices.length; i++) {
      result[i] = indices[i];
    }
    return result;
  }
  
  public static int[] sort(int[] array) {
    Integer[] tmp = new Integer[array.length];
    for (int i = 0; i < array.length; i++) {
      tmp[i] = array[i];
    }
    
    return sort(tmp);
  }
  
  public static int[] sort(long[] array) {
    Long[] tmp = new Long[array.length];
    for (int i = 0; i < array.length; i++) {
      tmp[i] = array[i];
    }
    
    return sort(tmp);
  }
  
  public static int[] sort(float[] array) {
    Float[] tmp = new Float[array.length];
    for (int i = 0; i < array.length; i++) {
      tmp[i] = array[i];
    }
    
    return sort(tmp);
  }
  
  public static int[] sort(double[] array) {
    Double[] tmp = new Double[array.length];
    for (int i = 0; i < array.length; i++) {
      tmp[i] = array[i];
    }
    
    return sort(tmp);
  }
  
}
