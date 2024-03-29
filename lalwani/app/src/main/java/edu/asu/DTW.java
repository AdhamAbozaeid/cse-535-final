package edu.asu;
public class DTW {
	protected float[] seq1;
	protected float[] seq2;
	protected int[][] warpingPath;
	protected int n;
	protected int m;
	protected int K;
	protected double warpingDistance;
	/* Constructor
 	         *
 	         * @param query
 	         * @param templete
	*/
	public DTW(float[] sample, float[] templete) {
		seq1 = sample;
		seq2 = templete;
		n = seq1.length;
		m = seq2.length;
		System.out.println(n);
		System.out.println(m);
		K = 1;
		warpingPath = new int[n + m][2];        // max(n, m) <= K < n + m
		warpingDistance = 0.0;
		this.compute();
	}

	public void compute() {
		double accumulatedDistance = 0.0;
		double[][] d = new double[n][m];        // local distances
		double[][] D = new double[n][m];        // global distances


		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {

				d[i][j] = distanceBetween(seq1[i], seq2[j]);
			}
		}
		D[0][0] = d[0][0];

		for (int i = 1; i < n; i++) {
			D[i][0] = d[i][0] + D[i - 1][0];
		}

		for (int j = 1; j < m; j++) {
			D[0][j] = d[0][j] + D[0][j - 1];
		}

		for (int i = 1; i < n; i++) {
			for (int j = 1; j < m; j++) {
				accumulatedDistance = Math.min(Math.min(D[i-1][j], D[i-1][j-1]), D[i][j-1]);
				accumulatedDistance += d[i][j];
				D[i][j] = accumulatedDistance;
			}
		}
		accumulatedDistance = D[n - 1][m - 1];
		int i = n - 1;
		int j = m - 1;
		int minIndex = 1;
		warpingPath[K - 1][0] = i;
		warpingPath[K - 1][1] = j;
		while ((i + j) != 0) {
			if (i == 0) {
				j -= 1;
			}
			else if (j == 0) {
				i -= 1;
			}// i != 0 && j != 0
			else {
				double[] array = { D[i - 1][j], D[i][j - 1], D[i - 1][j - 1] };
				minIndex = this.getIndexOfMinimum(array);
				if (minIndex == 0) {
					i -= 1;
				}
				else if (minIndex == 1) {
					j -= 1;
				}
				else if (minIndex == 2) {
					i -= 1;
					j -= 1;
				}
			} // end else
			K++;
			warpingPath[K - 1][0] = i;
			warpingPath[K - 1][1] = j;
		}
		warpingDistance = accumulatedDistance / K;
		this.reversePath(warpingPath);
	}
	//Changes the order of the warping path (increasing order)
	//@param path  the warping path in reverse order
	protected void reversePath(int[][] path) {
		int[][] newPath = new int[K][2];
		for (int i = 0; i < K; i++) {
			for (int j = 0; j < 2; j++) {
				newPath[i][j] = path[K - i - 1][j];
			}
		}
		warpingPath = newPath;
	}
	// Returns the warping distance
	public double getDistance() {
		return warpingDistance;
	}
	//Computes a distance between two points
	// @param p1    the point 1
	// @param p2    the point 2
	// @return              the distance between two points
	protected double distanceBetween(double p1, double p2) {
		return (p1 - p2) * (p1 - p2);
	}
	// Finds the index of the minimum element from the given array
	//@param array         the array containing numeric values
	//@return                              the min value among elements
	protected int getIndexOfMinimum(double[] array) {
		int index = 0;
		double val = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] < val) {
				val = array[i];
				index = i;
			}
		}
		return index;
	}
	//Returns a string that displays the warping distance and path
	public String toString() {
		String retVal = "Warping Distance: " + warpingDistance + "\n";
		retVal += "Warping Path: {";
		for (int i = 0; i < K; i++) {
			retVal += "(" + warpingPath[i][0] + ", " +warpingPath[i][1] + ")";
			retVal += (i == K - 1) ? "}" : ", ";
		}
		return retVal;
	}
	//Tests this class
	/*
	public static void main(String[] args) {
		float[] n2 = {3.1f, 5.45f, 4.673f, 10.32f};
		//float[] n2 = {2.1f, 2.45f, 3.673f, 4.32f, 2.05f, 1.93f, 5.67f, 6.01f};
		float[] n1 = {2.1f, 2.45f, 3.673f, 4.32f, 2.05f, 1.93f, 5.67f, 6.01f};
		DTW dtw = new DTW(n1, n2);
		System.out.println("Hi");
		System.out.println(dtw);
	}
	*/
}

