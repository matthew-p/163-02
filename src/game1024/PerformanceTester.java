package game1024;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
     * Run a testing loop outputting to the terminal
     * @param args
     ******************************************************************/
    public static void terminalOut() {
        NumberGame g = new NumberGame(10);
        long startTime = 0;
        long endTime = 0;
        long d = 0;
        String msgS = "Beginning slide test for side length: ";
        String msgE = "Slide test complete: ";
        ArrayList<Info> outcomes = new ArrayList<Info>();
        
        for (int i = 5; i < 15000; i = i * 2) {
            System.out.println(msgS + i);
            g = new NumberGame(i);
            g.setValues(fullBoardGen(i, 2));
            startTime = System.currentTimeMillis();
            g.slide(SlideDirection.UP);
            endTime = System.currentTimeMillis();
            d = endTime - startTime;
            outcomes.add(new Info(d, i));
            System.out.println(msgE + i + " duration: " + d + " ms\n");
        }
        System.out.println("Summary: \n****************\n");
        
        for (int i = 0; i < outcomes.size(); i++) {
            Info cur = outcomes.get(i);

            System.out.println("Duration for side length " + 
                                cur.sideLength + ": " + 
                                cur.executionTime + " ms");
            if (i > 0) {
                double percent = cur.executionTime * 1.0 / 
                        outcomes.get(i - 1).executionTime;
                System.out.print("Difference from prior length: ");
                System.out.printf("%.1f", percent * 100);
                System.out.println(" %\n");
            }
        }
    }
    
    /*****************************************************************
     * Run a test loop, outputing to a .CSV file
     * @throws FileNotFoundException 
     */
    public static void fileOut(int step) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File("test.csv"));
        StringBuilder sb = new StringBuilder();
        NumberGame g;
        long startTime = 0;
        long endTime = 0;
        long d = 0;
        
        sb.append("side length,duration\n");
        
        System.out.println("fileOut beginning: ");
        
        for (int i = 200; i < 12000; i += step) {
            g = new NumberGame(i);
            g.setValues(fullBoardGen(i, 2));
            startTime = System.currentTimeMillis();
            g.slide(SlideDirection.RIGHT);
            endTime = System.currentTimeMillis();
            d = endTime - startTime; 
            sb.append(i + "," + d + "\n");
            
            System.out.println("finished for side: " + i + " dur: "+ d);
        }
        pw.write(sb.toString());
        pw.close();
        pw.flush();
        System.out.println("fileOutput test method finished");
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        PerformanceTester.fileOut(200);
    }
}
