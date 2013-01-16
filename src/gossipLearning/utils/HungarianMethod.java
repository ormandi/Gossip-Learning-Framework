package gossipLearning.utils;

/**
* This class implements the Hungarian method which solves the assignment problem.
* @author Arpad Berta
*/
public class HungarianMethod {
	
	private double[][] diffTable;
	private int[] permutationArray;
	
	/***
	 * Construct and execute the Hungarian method. You can get the
	 * result with getPermutationArray()
	 * @param x assignment matrix
	 */
	public HungarianMethod(double[][] x) {
		diffTable = initDiffTable(x);
		permutationArray = hungarianMethod();
	}
	
	/***
	 * It constructs a double[][] array with euclidean distance of 
	 * the two SparceVector and then it does the same as the
	 * HungarianMethod(double[][] x) constructor
	 * @param x1 vector
	 * @param x2 vector
	 */
	public HungarianMethod(SparseVector[] x1, SparseVector[] x2) {
		diffTable = initDiffTable(x1,x2);
		permutationArray = hungarianMethod();
	}
	
	/***
	 * Permutation array gives the best permutation for the two input vector 
	 * (the input was set into the constructors)  
	 * @return result of the Hungarian method
	 */
	public int[] getPermutationArray() {
		return permutationArray;
	}

	private int[] hungarianMethod() {
	
		//Preparation Part 2
		int[] starArray = initStarArray();
		int[] commaArray = initCommaArray();
		int nullSystemCount = computeNullSystemCount(starArray);

		int r = 0;
		int nextIteration = 0;
		boolean[] rowBlock = new boolean[diffTable.length];
		boolean[] coloumnBlock = new boolean[diffTable.length];

		while (nullSystemCount < diffTable.length) {

			if (r == nextIteration){
				// Step1
				for (int i = 0; i < starArray.length; i++) {
					if (starArray[i] != -1) {
						coloumnBlock[starArray[i]] = true;
					}
				}
				nextIteration ++;
				
			}
			// Step2
			boolean goToStep5 = true;
			boolean getBackToStep2 = false;
			for (int i = 0; i < diffTable.length; i++) {
				for (int j = 0; j < diffTable.length; j++) {
					if (diffTable[i][j] == 0 &&  coloumnBlock[j] == false && rowBlock[i] == false) {
						goToStep5 = false;
						
						boolean notRowBlockingNul = (starArray[i] == -1);
						if (notRowBlockingNul) { 
							// Step4
							
							starArray[i] = j;
							boolean isNextRing = true;
							while(isNextRing) {
								boolean stopIt = true;
								for (int k = 0; k < starArray.length; k++) {
									if (starArray[k] == j && k!=i) {
										starArray[k] = -1;
										i = k;
										if (commaArray[i] != -1) {
											j = commaArray[i];
											starArray[i] = j;
											commaArray[i] = -1;
											stopIt = false;
											break;
										}
										break;
									}
								}
								if (stopIt) {
									isNextRing = false;
								}
							}
							
							// Restore the default markings and compute the nullsystem for the next iteration
							for (int k = 0; k < coloumnBlock.length; k++) {
								coloumnBlock[k] = false;
							}
							for (int k = 0; k < rowBlock.length; k++) {
								rowBlock[k] = false;
							}
							for (int k = 0; k < commaArray.length; k++) {
								commaArray[k] = -1;
							}
							
							r++; // step to the next iteration
							nullSystemCount = computeNullSystemCount(starArray);
							getBackToStep2 = true; // break out
						} else { 
							// Step3
							commaArray[i] = j;
							rowBlock[i] = true;
							coloumnBlock[starArray[i]] = false;
							getBackToStep2 = true;

						}
					}
					if(getBackToStep2) {
						break;
					}
				}
				if(getBackToStep2) {
					break;
				}
			}
			if(goToStep5) { 
				// Step5
				double minFreeDiffTable = Double.MAX_VALUE;
				for (int i = 0; i < diffTable.length; i++) {
					for (int j = 0; j < diffTable.length; j++) {
						if( !rowBlock[i] && !coloumnBlock[j] ) {
							minFreeDiffTable = Math.min(minFreeDiffTable, diffTable[i][j]);
						}
					}
				}
				for (int i = 0; i < diffTable.length; i++) {
					for (int j = 0; j < diffTable.length; j++) {
						if(rowBlock[i] && coloumnBlock[j]) {
							diffTable[i][j] += minFreeDiffTable;
						}
						if( !rowBlock[i] && !coloumnBlock[j] ) {
							diffTable[i][j] -= minFreeDiffTable;
						}
					}
				}

			}
		}
		return starArray;
	}
	/***
	 * Hungarian Method - Preparation Part 1
	 * 
	 * @param differTable matrix of the assignment problem
	 * @return
	 */
	private double[][] initDiffTable(double[][] differTable){
		
		double[][] differenceTable = differTable.clone();
		double[] rowMin = new double[differenceTable.length];
		double[] coloumnMin = new double[differenceTable.length];

		for (int i = 0; i < differenceTable.length; i++) {
			rowMin[i] = Integer.MAX_VALUE;
			for (int j = 0; j < differenceTable[i].length; j++) {
				rowMin[i] = Math.min(rowMin[i], differenceTable[i][j]);
				coloumnMin[j] = Integer.MAX_VALUE;
			}
		}

		for (int i = 0; i < differenceTable.length; i++) {
			for (int j = 0; j < differenceTable[i].length; j++) {
				differenceTable[i][j] -= rowMin[i];
				coloumnMin[j] = Math.min(coloumnMin[j], differenceTable[i][j]);
			}
		}

		for (int i = 0; i <differenceTable.length; i++) {
			for (int j = 0; j < differenceTable[i].length; j++) {
				differenceTable[i][j] -= coloumnMin[j];
			}
		}
		
		return differenceTable;
	}
	
	private double[][] initDiffTable(SparseVector[] x1, SparseVector[] x2) {
		
		double[][] differenceTable = new double[x1.length][x2.length];

		for (int i = 0; i < differenceTable.length; i++) {	
			for (int j = 0; j < differenceTable[i].length; j++) {
				differenceTable[i][j] = x1[i].euclideanDistance(x2[j]);
			}
		}
		
		return initDiffTable(differenceTable);
	}
	
	private int[] initStarArray(){
		int[] permArray = initCommaArray();
		
		for (int j = 0; j < diffTable[0].length; j++) {	
			for (int i = 0; i < diffTable.length; i++) {
				if (diffTable[i][j] == 0) {
					boolean notRowBlockingNul = (permArray[i] == -1);
					if (notRowBlockingNul) {
						permArray[i] = j;
						break;
					}
				}
			}
		}
		
		return permArray;
	}
	
	private int[] initCommaArray(){
		int[] commaArray = new int[diffTable.length];
		for (int i = 0; i < commaArray.length; i++) {
			commaArray[i] = -1;
		}
		return commaArray;
	}

	
	private int computeNullSystemCount(int[] permArray) {
		int nullSystemCount = 0;
		for (int i = 0; i < permArray.length; i++) {
			if (permArray[i] != -1) {
				nullSystemCount++;
			}
		}
		return nullSystemCount;
	}
	
	
}
