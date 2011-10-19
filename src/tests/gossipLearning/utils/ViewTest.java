package tests.gossipLearning.utils;

import gossipLearning.utils.View;

import java.io.Serializable;

import junit.framework.TestCase;

/**
 * This class is an extensive test case of our View implementation.
 * 
 * @author ormandi
 *
 */
public class ViewTest extends TestCase implements Serializable {
  private static final long serialVersionUID = 8155283223775025419L;

  /**
   * Sample content for the testcases. It simply stores an integer.
   */
  class SampleContent implements Serializable, Comparable<SampleContent> {
    private static final long serialVersionUID = -2560671308181906799L;
    private int c = 0;

    public SampleContent(int c) {
      this.c = c;
    }

    @Override
    public boolean equals(Object o) {
      return o != null && o instanceof SampleContent && c == ((SampleContent)o).c;
    }

    @Override
    public int compareTo(SampleContent o) {
      if (c > o.c) {
        return -1;
      }
      if (o == null || c < o.c) {
        return 1;
      }
      return 0;
    }

    @Override
    public String toString() {
      return "" + c;
    }
  }

  private View<SampleContent> testView = null;
  private View<SampleContent> cloneView = null;
  private static final int VIEW_SIZE = 6;

  /**
   * Initializes the test variable.
   */
  @SuppressWarnings("unchecked")
  @Override
  public void setUp() {
    testView = new View<SampleContent>(VIEW_SIZE);
    testView.insert(new SampleContent(1));
    testView.insert(new SampleContent(2));
    testView.insert(new SampleContent(5));
    testView.insert(new SampleContent(3));
    testView.insert(new SampleContent(0));
    testView.insert(new SampleContent(4));
    cloneView = (View<SampleContent>) testView.clone();
  }

  /**
   * Tests constructor of class View.
   */
  public void testCreate() {
    testView = null;
    testView = new View<SampleContent>(VIEW_SIZE);
    assertNotNull(testView);
  }

  /**
   * Tests clone method of class View by String comparation, since equals testing will be later.
   */
  @SuppressWarnings("unchecked")
  public void testClone() {
    cloneView = null;
    cloneView = (View<SampleContent>) testView.clone();
    assertNotNull(testView.toString(), cloneView.toString());
  }

  /**
   * Tests the deep copy property of clone method.
   */
  @SuppressWarnings("unchecked")
  public void testCloneSecondCase() {
    cloneView = null;
    cloneView = (View<SampleContent>) testView.clone();
    testView.clear();
    assertEquals(cloneView.size(), VIEW_SIZE);
  }

  /**
   * Tests the size method of class View.
   */
  public void testSize() {
    assertEquals(testView.size(), VIEW_SIZE);
  }

  /**
   * Tests the size method of class View (case 2).
   */
  public void testSizeSecondCase() {
    testView = null;
    testView = new View<SampleContent>(VIEW_SIZE);
    testView.insert(new SampleContent(1));
    testView.insert(new SampleContent(2));
    assertEquals(testView.size(), 2);
  }

  /**
   * Tests the getter of class View.
   */
  public void testGet() {
    assertEquals(testView.get(0), new SampleContent(5));
  }

  /**
   * Tests the clear method of class View.
   */
  public void testClear() {
    testView.clear();
    cloneView = new View<SampleContent>(VIEW_SIZE);
    assertEquals(testView.toString(), cloneView.toString());
  }

  /**
   * Tests insert (and so sorting property) of class View.
   */
  public void testInsert() {
    final String tExpected = "5, 4, 3, 2, 1, 0";
    assertEquals(tExpected, testView.toString());
  }

  /**
   * Tests insert (and so sorting property) of class View (case 2).
   */
  public void testInsertSecondCase() {
    testView.insert(new SampleContent(4));
    final String tExpected = "5, 4, 4, 3, 2, 1";
    assertEquals(tExpected, testView.toString());
  }

  /**
   * Tests insert (and so sorting property) of class View (case 3).
   */
  public void testInsertThirdCase() {
    testView.insert(new SampleContent(11));
    testView.insert(new SampleContent(10));
    final String tExpected = "11, 10, 5, 4, 3, 2";
    assertEquals(tExpected, testView.toString());
  }
}
