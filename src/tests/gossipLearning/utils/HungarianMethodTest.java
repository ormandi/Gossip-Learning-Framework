package tests.gossipLearning.utils;

import static org.junit.Assert.*;
import gossipLearning.utils.HungarianMethod;
import gossipLearning.utils.SparseVector;

import org.junit.Test;

public class HungarianMethodTest {

	private HungarianMethod hum;

	/***
	 * test the constructor with SparesVectors
	 */
	@Test
	public void testHungarianMethodWithNullSparseVector() {
		SparseVector[] s1 = new SparseVector[3];
		SparseVector[] s2 = new SparseVector[3];
		for (int i = 0; i < s1.length; i++) {
			s1[i] = new SparseVector(new double[40]);
			s2[i] = new SparseVector(new double[40]);		
		}
		
		
		hum = new HungarianMethod(s1,s2);
		int[] actuals = hum.getPermutationArray();
		int[] expecteds1 = new int[3];
		
		for (int i = 0; i < expecteds1.length; i++) {
			expecteds1[i] = i; 
		}
		String message = "Result";
		assertArrayEquals(message, expecteds1, actuals);

	}
	
	/***
	 * test the Hungarian method implementation with known example
	 */
	@Test
	public void testHungarianMethodWithSampleArrayArray1() {
		double[][] x2 = {{4,5,3,2,3},
				{3,2,4,3,4},
				{3,3,4,4,3},
				{2,4,3,2,4},
				{2,1,3,4,3}};
		hum = new HungarianMethod(x2);
		int[] actuals = hum.getPermutationArray();
		int[] expecteds2 = {2,1,4,3,0};
		String message = "Result";
		assertArrayEquals(message, expecteds2, actuals);
	}
	
	/***
	 * test the Hungarian method implementation with known example
	 */
	@Test
	public void testHungarianMethodWithSampleArrayArray2(){
		double[][] x4 = {{1,2,4,8},
				{8,4,2,1},
				{3,4,4,3},
				{5,6,6,5}};
		hum = new HungarianMethod(x4);
		int[] actuals = hum.getPermutationArray();
		int[] expecteds4 = {0,2,1,3};
		String message = "Result";
		assertArrayEquals(message, expecteds4, actuals);
	}
	
	/***
	 * test the Hungarian method implementation with known example
	 */
	@Test
	public void testHungarianMethodWithSampleArrayArray3(){

		double[][] x5 = {{4,5,4,5},
				{2,3,6,1},
				{4,5,7,1},
				{3,8,6,2}};
		hum = new HungarianMethod(x5);
		int[] actuals = hum.getPermutationArray();
		int[] expecteds5 = {2,1,3,0};
		String message = "Result";
		assertArrayEquals(message, expecteds5, actuals);
	}
	
	/***
	 * test the Hungarian method implementation with known example
	 */
	@Test
	public void testHungarianMethodWithSampleArrayArray4(){
		double[][] x6 = {{4,92,92,5},
				{92,7,8,92},
				{7,7,9,6},
				{6,6,9,92}};
		hum = new HungarianMethod(x6);
		int[] actuals = hum.getPermutationArray();
		int[] expecteds6 = {0,2,3,1};
		String message = "Result";
		assertArrayEquals(message, expecteds6, actuals);

	}
	
	/***
	 * test the Hungarian method implementation with known example 
	 * which include negative numbers. 
	 */
	@Test
	public void testHungarianMethodWithNegativeSampleArrayArray1(){
		double[][] x3 = {{-2,5,1,3,-1},
				{4,2,0,1,-1},
				{3,-2,1,1,4},
				{-2,1,0,3,3},
				{1,2,-4,1,2}};
		hum = new HungarianMethod(x3);
		int[] actuals = hum.getPermutationArray();
		int[] expecteds3 = {4,3,1,0,2};
		String message = "Result";
		assertArrayEquals(message, expecteds3, actuals);
	}

}
