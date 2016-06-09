package uebung_parallelisierung.parallel;

import java.util.Arrays;

public class LabyrinthTester {

	private static long testLabyrinthSolving(Labyrinth labyrinth) throws Exception {
		long solvingTime = labyrinth.solveAndMeasure();

		labyrinth.showSolutionDetails();

		if (!labyrinth.checkSolution()) {
			System.out.println("Solution incorrect :-(");

			throw new Exception("Solution incorrect");
		}
		
		/*/
		int mb = 1024*1024;
        
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
         
        System.out.println("##### Heap utilization statistics [MB] #####");
         
        //Print used memory
        System.out.println("Used Memory: "
            + (runtime.totalMemory() - runtime.freeMemory()) / mb + " MB");
 
        //Print free memory
        System.out.println("Free Memory: "
            + runtime.freeMemory() / mb + " MB");
         
        //Print total available memory
        System.out.println("Total Memory: " + runtime.totalMemory() / mb + " MB");
 
        //Print Maximum available memory
        System.out.println("Max Memory: " + runtime.maxMemory() / mb + " MB");
		/**/
		
		return solvingTime;
	}

	private static long getMedian(long[] list) {
		Arrays.sort(list);
		if (list.length % 2 == 0)
			return (list[list.length / 2] + list[list.length / 2 - 1]) / 2;
		return list[list.length / 2];
	}

	private static long getAverage(long[] list) {
		long sum = 0;
		for (int i = 0; i < list.length; i++) {
			sum += list[i];
		}
		return sum / list.length;
	}

	public static void main(String[] args) throws Exception {

		float total1M = 0, total1A = 0, total2M = 0, total2A = 0, total3M = 0, total3A = 0, total4M = 0, total4A = 0;
		final int NUM_RUNS = 10;
		final int NUM_TRIES = 25;
		for (int num = 1; num <= NUM_RUNS; num++) {

			/**/
			int width, height;
			width = height = 5000;
			Grid grid = LabyrinthCreator.createGrid(width, height);
			/*/
			Grid grid = LabyrinthCreator.loadGrid("grid");
			/**/

			/*/
			long[] list1 = new long[NUM_TRIES];
			for (int i = 0; i < NUM_TRIES; i++) {
				Labyrinth labyrinth = new LabyrinthSeq(grid);
				list1[i] = testLabyrinthSolving(labyrinth);
			}
			long median1 = getMedian(list1); 
			total1M += median1;
			long average1 = getAverage(list1); 
			total1A += average1;
			System.out.println("Median = " + median1 + ", average = " + average1);
			/**/
			
			/*/
			long[] list2 = new long[NUM_TRIES];
			for (int i = 0; i < NUM_TRIES; i++) {
				Labyrinth labyrinth = new LabyrinthSeqBetter(grid);
				list2[i] = testLabyrinthSolving(labyrinth);
			}
			long median2 = getMedian(list2); 
			total2M += median2;
			long average2 = getAverage(list2); 
			total2A += average2;
			System.out.println("Median = " + median2 + ", average = " + average2);
			/**/
			
			/**/
			long[] list3 = new long[NUM_TRIES];
			for (int i = 0; i < NUM_TRIES; i++) {
				Labyrinth labyrinth = new LabyrinthPar(grid);
				list3[i] = testLabyrinthSolving(labyrinth);
			}
			long median3 = getMedian(list3); 
			total3M += median3;
			long average3 = getAverage(list3);
			total3A += average3;
			System.out.println("Median = " + median3 + ", average = " + average3);
			/**/
			
			/**/
			long[] list4 = new long[NUM_TRIES];
			for (int i = 0; i < NUM_TRIES; i++) {
				Labyrinth labyrinth = new LabyrinthParSplit(grid);
				list4[i] = testLabyrinthSolving(labyrinth);
			}
			long median4 = getMedian(list4); 
			total4M += median4;
			long average4 = getAverage(list4);
			total4A += average4;
			System.out.println("Median = " + median4 + ", average = " + average4);
			/**/
			
			System.out.println("Labyrinth " + num + " done");
		}

		long res1M = Math.round(total1M / NUM_RUNS);
		long res1A = Math.round(total1A / NUM_RUNS);
		long res2M = Math.round(total2M / NUM_RUNS);
		long res2A = Math.round(total2A / NUM_RUNS);
		long res3M = Math.round(total3M / NUM_RUNS);
		long res3A = Math.round(total3A / NUM_RUNS);
		long res4M = Math.round(total4M / NUM_RUNS);
		long res4A = Math.round(total4A / NUM_RUNS);
		
		//System.out.println("Sequentiell original - Median: " + res1M + "ms, Durchschnitt: " + res1A + " ms");
		//System.out.println("Sequentiell verbessert - Median: " + res2M + "ms, Durchschnitt: " + res2A + " ms");
		System.out.println("Parallel - Median: " + res3M + "ms, Durchschnitt: " + res3A + " ms");
		System.out.println("Parallel, splitted - Median: " + res4M + "ms, Durchschnitt: " + res4A + " ms");
	}
}