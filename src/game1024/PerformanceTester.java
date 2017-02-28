package game1024;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class to test the performance of the game slide method
 * @author Matthew Pische
 *
 */
public class PerformanceTester {
    
    /*******************************************************************
     * Generate a gameboard of an arbitrary size and value
     * @param length size of the sides of the gameboard
     * @param value number to which every board cell will be initialized
     * @return the new gameboard
     ******************************************************************/
    public static int[][] fullBoardGen(int length, int value) {
        int[][] output = new int[length][length];
        for (int r = 0; r < length; r++) {
            Arrays.fill(output[r], value);
        }
        return output;
    }
    
    /*******************************************************************
     * Run a testing loop
     * @param args
     ******************************************************************/
    public static void main(String[] args) {
        NumberGame g = new NumberGame(10);
        long startTime = 0;
        long endTime = 0;
        long d = 0;
        String msgS = "Beginning slide test for side length: ";
        String msgE = "Slide test complete for side length: ";
        ArrayList<Info> outcomes = new ArrayList<Info>();
        
        for (int i = 5; i < 15000; i = i * 2) {
            System.out.println(msgS + i);
            g = new NumberGame(i);
            g.setValues(fullBoardGen(i, 2));
            startTime = System.currentTimeMillis();
            g.slide(SlideDirection.LEFT);
            endTime = System.currentTimeMillis();
            d = endTime - startTime;
            outcomes.add(new Info(d, i));
            System.out.println(msgE + i + " duration: " + d + " ms\n");
        }
        System.out.println("Summary: \n****************\n");
        
        for (int i = 0; i < outcomes.size(); i++) {
            Info cur = outcomes.get(i);

            System.out.println("Duration for side length " + 
                                cur.sideLength + ": " + cur.executionTime);
            if (i > 0) {
                double percent = cur.executionTime * 1.0 / 
                        outcomes.get(i - 1).executionTime;
                System.out.println("Difference from prior length: " + 
                                    cur.sideLength + " = " + percent);
            }
        }
    }
}
