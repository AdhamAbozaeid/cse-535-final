package edu.asu;
public class DTW {

        protected float[] seq1;
	        protected float[] seq2;
	        protected int[][] warpingPath;

	        protected int n;
	        protected int m;
	        protected int K;

	        protected double warpingDistance;

	        /**
 30	         * Constructor
 31	         *
 32	         * @param query
 33	         * @param templete
 34	         */
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
            	                        } else if (j == 0) {
            	                                i -= 1;
            	                        } else {        // i != 0 && j != 0
            	                                double[] array = { D[i - 1][j], D[i][j - 1], D[i - 1][j - 1] };
            	                                minIndex = this.getIndexOfMinimum(array);

            	                                if (minIndex == 0) {
            	                                        i -= 1;
            	                                } else if (minIndex == 1) {
            	                                        j -= 1;
            	                                } else if (minIndex == 2) {
            	                                        i -= 1;
            	                                        j -= 1;
            	                                }
            	                        } // end else
            	                        K++;
            	                        warpingPath[K - 1][0] = i;
            	                        warpingPath[K - 1][1] = j;
            	                } // end while
            	                warpingDistance = accumulatedDistance / K;

            	                this.reversePath(warpingPath);
            	        }

        	        /**
 115	        * Changes the order of the warping path (increasing order)
 116	         *
 117	         * @param path  the warping path in reverse order
 118	         */
                protected void reversePath(int[][] path) {
            	                int[][] newPath = new int[K][2];
            	                for (int i = 0; i < K; i++) {
            	                        for (int j = 0; j < 2; j++) {
            	                                newPath[i][j] = path[K - i - 1][j];
            	                        }
            	                }
            	                warpingPath = newPath;
            	        }
	        /**
 129	         * Returns the warping distance
 130	         *
 131	         * @return
 132	         */
        	        public double getDistance() {
            	                return warpingDistance;
            	        }

        	        /**
 138	         * Computes a distance between two points
 139	         *
 140	         * @param p1    the point 1
 141	         * @param p2    the point 2
 142	         * @return              the distance between two points
 143	         */
        	        protected double distanceBetween(double p1, double p2) {
            	                return (p1 - p2) * (p1 - p2);
            	        }

        	        /**
 149	         * Finds the index of the minimum element from the given array
 150	         *
 151	         * @param array         the array containing numeric values
 152	         * @return                              the min value among elements
 153	         */
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

        	        /**
 168	         *      Returns a string that displays the warping distance and path
 169	        */
        	        public String toString() {
            	                String retVal = "Warping Distance: " + warpingDistance + "\n";
            	                retVal += "Warping Path: {";
            	                for (int i = 0; i < K; i++) {
                	                        retVal += "(" + warpingPath[i][0] + ", " +warpingPath[i][1] + ")";
                	                        retVal += (i == K - 1) ? "}" : ", ";
                                	                }
            	                return retVal;
            	        }

        	        /**
 182	         * Tests this class
 183	         *
 184	         * @param args  ignored
 185	         *//*
        	        public static void main(String[] args) {
            	                //float[] n2 = {1.5f, 3.9f, 4.1f, 3.3f};
								float[] n2 = {3.1f, 5.45f, 4.673f, 10.32f};
								//float[] n2 = {2.1f, 2.45f, 3.673f, 4.32f, 2.05f, 1.93f, 5.67f, 6.01f};
            	                float[] n1 = {2.1f, 2.45f, 3.673f, 4.32f, 2.05f, 1.93f, 5.67f, 6.01f};
            	                DTW dtw = new DTW(n1, n2);
            	                System.out.println("Hi");
                           System.out.println(dtw);
                    }
                    */
	}

