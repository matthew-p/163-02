package game1024;

import java.util.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

/**
 * Core of the 2048 (1024) game, built for CS163, February 2017
 * Built to satisfy a provided spec and suite of tests
 * that require handling arbitrarily sized boards, 
 * with random cell values   
 * @author Matthew Pische
 */
public class NumberGame implements NumberSlider {

    /** The main Game Board  */
    private int[][] board;

    /** the winning value */
    private int winValue;
 
    /** current status */
    private GameStatus status;
 
    /** game history */
    private Stack<int[][]> undos;
    
    /******************************************************************
     * Default constructor sets gameboard to 4x4 cells, 
     * and win value to 1024
     *****************************************************************/
    public NumberGame() {
        board = new int[4][4];
        winValue = 1024;
        status = GameStatus.IN_PROGRESS;
        undos = new Stack<int[][]>();
    }
    
    /******************************************************************
     * Takes a single value to set the game board's length and width,
     * win value defaults to 1024
     * @param length length of both gameboard sides
     *****************************************************************/
    public NumberGame(int length) {
        board = new int[length][length];
        winValue = 1024;
        status = GameStatus.IN_PROGRESS;
        undos = new Stack<int[][]>();
    }
    
    /******************************************************************
     * Takes different row and column dimensions for the game board,
     * win value defaults to 1024
     * @param rowLength height of the gameboard
     * @param colLength width of the gameboard
     *****************************************************************/
    public NumberGame(int rowLength, int colLength) {
        board = new int[rowLength][colLength];
        winValue = 1024;
        status = GameStatus.IN_PROGRESS;
        undos = new Stack<int[][]>();
    }
    
    /******************************************************************
     * Set different board dimensions, and a custom win value
     * @param rowLength height of the gameboard
     * @param colLength width of the gameboard
     * @param winValue winning score
     *****************************************************************/
    public NumberGame(int rowLength, int colLength, int winValue) {
        board = new int[rowLength][colLength];
        this.winValue = winValue;
        status = GameStatus.IN_PROGRESS;
        undos = new Stack<int[][]>();
    }
    
    /******************************************************************
     * A class where each instance represents the 
     * empty cells in a given row, done to facilitate adding a 
     * new random cell into the available empty space on the gameboard
     * @author Matthew Pische
     *
     *****************************************************************/
    private class Empties {
        /** highest empty cell index */ 
        private int max; 
        /** lowest empty cell index */
        private int min;
        /** the index of the current row or column this represents */
        private int rOrCIndex;
        /** whether this represents a row or column */
        private boolean isRow;
        
        /***************************************************************
         * returns a new representation of the empty cells in a 
         * row or column of the gameboard
         * @param max the highest empty cell index in the row or column
         * @param min the first empty cell index in the row or column
         * @param rOrCIndex the column or row index for this instance
         * @param isRow whether the instance is a row or column
         **************************************************************/
        public Empties(int max, int min, int rOrCIndex, boolean isRow) {
            this.min = min;
            this.max = max;
            this.rOrCIndex = rOrCIndex;
            this.isRow = isRow;
        }
        
        /***************************************************************
         * Creates a new Cell object in one of the available empty 
         * spaces in this object's represented row or column
         * @return a cell in a previously empty space in this row/column
         **************************************************************/
        public Cell randomCell() {
            int rnd = (new Random()).nextInt((max - min) + 1) + min;
            if (isRow) {
                return new Cell(rOrCIndex, rnd, newCellVal());
            } else {
                return new Cell(rnd, rOrCIndex, newCellVal());
            }
        }
        public boolean isMultiCell() {
            return max - min > 0 ? true : false;
        }
    }
    
    /******************************************************************/
    @Override
    public void resizeBoard(int height, int width, int winValue) 
            throws IllegalArgumentException {
        if ((winValue & -winValue) == winValue) {
            board = new int [height][width];
            this.winValue = winValue;
            undos = new Stack<int[][]>();
            status = GameStatus.IN_PROGRESS;
        } else {
            throw new IllegalArgumentException("Invalid winning value");
        }
    }

    /******************************************************************/
    @Override
    public void reset() {
        board = new int[board.length][board[0].length];
        undos = new Stack<int[][]>();
        placeRandomValue();
        placeRandomValue();
        status = GameStatus.IN_PROGRESS;
    }

    /******************************************************************/
    @Override
    public void setValues(int[][] ref) {
        if (ref.length != board.length || 
                ref[0].length != board[0].length) {
            resizeBoard(ref.length, ref[0].length, 0);
        }
        int rows = ref.length;
        int cols = ref[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c] = ref[r][c];
            }
        }
    }
    
    /******************************************************************/
    @Override
    public Cell placeRandomValue() throws IllegalStateException {
        // build a list of empty cells, to place one within that zone
        // avoids potentially long run time with random checks in a 
        // loop when the board is mostly full 
        LinkedList<Cell> empty = getEmptyTiles();
        if (empty.isEmpty()) {
            throw new IllegalStateException();
        } else {
            Cell c = empty.get((new Random()).nextInt(empty.size()));
            board[c.row][c.column] = newCellVal();
            return c;
        }
    }
    
    /******************************************************************
     * Gets a randomized value for a new game cell, 
     * 90% chance of a 2, 10% of a 4
     * @return numeric cell value
     *****************************************************************/
    private int newCellVal() {
        // NOTE: canonical implementation of this game uses 
        // a 90/10 value distribution of 2s & 4s for new random tiles
        // https://github.com/gabrielecirulli/2048/blob/master/js/game_manager.js#L71
        return (new Random()).nextInt(10) < 9 ? 2 : 4;
    }
    
    /******************************************************************/
    @Override
    public ArrayList<Cell> getNonEmptyTiles() {
        ArrayList<Cell> rtn = new ArrayList<Cell>();
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                if (board[r][c] > 0)
                    rtn.add(new Cell( r, c, board[r][c]));
            }
        }
        return rtn;
    }

    /******************************************************************/
    @Override
    public GameStatus getStatus() {
        // the way this class is tested makes it necessary
        // to call a check across the game board
        // here, outside of any actual gameplay 
        if (!anyMoves(board)) {
            status = GameStatus.USER_LOST;
        }
        return status;
    }
    /******************************************************************/
    @Override
    public void undo() throws IllegalStateException {
        if (undos.empty()) {
            throw new IllegalStateException();
        } else {
            board = undos.pop();
        }
        
    }
    
    /******************************************************************/
    @Override
    public boolean slide(SlideDirection dir) {        
        // Excessively complicated to avoid re-traversing the board 
        // when placing a new cell in the empty portion of the board
        // at the end of the slide
        boolean cng = false;
        
        LinkedList<Empties> emptyRegion = new LinkedList<Empties>();
        
        // recieves new values, eventually replaces existing board
        int[][] newBoard = new int[board.length][board[0].length];
        
        if (dir == SlideDirection.RIGHT || dir == SlideDirection.LEFT) {
            for (int r = 0; r < board.length; r++) {
                // holds all values found in this row
                Queue<Integer> q = new LinkedList<Integer>();
                
                if (dir == SlideDirection.RIGHT) {
                    // walk backwards, adding values to the queue 
                    for (int c = board[r].length - 1; c >= 0; c--) {
                        if (board[r][c] != 0) {
                            q.add(board[r][c]);
                        }
                    }
                    // helper method to empty the queue & fill new board
                    cng = rowCompact(board[r].length, q, -1, r, cng, 
                                    newBoard, emptyRegion);
                }
                else { // Left
                    for (int c = 0; c < board[r].length; c++) {
                        if (board[r][c] != 0) {
                            q.add(board[r][c]);
                        }
                    }
                    cng = rowCompact(board[r].length, q, 1, r, cng, 
                                    newBoard, emptyRegion);
                }
            }
        } else { // not horizontal, must be a column
            for (int c = 0; c < board[0].length; c++) {
                Queue<Integer> q = new LinkedList<Integer>();
                if (dir == SlideDirection.UP) {
                    for (int r = 0; r < board.length; r++) {
                        if (board[r][c] != 0) {
                            q.add(board[r][c]);
                        }
                    }
                    cng = colCompact(board.length, q, 1, c, cng, 
                                    newBoard, emptyRegion);
                } else { // Down
                    for (int r = board.length - 1; r >= 0; r--) {
                        if (board[r][c] != 0) {
                            q.add(board[r][c]);
                        }
                    }
                    cng = colCompact(board.length, q, -1, c, cng, 
                                    newBoard, emptyRegion);
                }
            }
        }
        
        if (emptyRegion.size() > 1 || (
                emptyRegion.size() == 1 && 
                emptyRegion.get(0).isMultiCell())) {
            
            // if there are multiple empty cells, 
            // get one & place it
            Cell n = emptyRegion.get(
                        (new Random()).nextInt(emptyRegion.size()))
                        .randomCell();
            
            newBoard[n.row][n.column] = n.value;
        } else if (emptyRegion.size() == 1 && 
                    !emptyRegion.get(0).isMultiCell()) {
            
            // if there is only one empty cell, use it
            Cell n = emptyRegion.get(0).randomCell();            
            newBoard[n.row][n.column] = n.value;
            
            if (status != GameStatus.USER_WON && !anyMoves(newBoard)) {
                status = GameStatus.USER_LOST;
            }
        } else {
            if (status != GameStatus.USER_WON && !anyMoves(newBoard)) {
                status = GameStatus.USER_LOST;
            }
        }
        
        if (cng) {
            undos.push(board);
            board = newBoard;
        }
        return cng;
    }
    
    /******************************************************************
     * Compacts a row on the game board, evaluating whether any 
     * change occurs from the board's prior state, whether the 
     * winning value is reached, and tracks whatever empty space 
     * is available in this row for inserting a new cell
     * @param length number of cells in the board's rows
     * @param q a queue of all the cells to compact
     * @param iterator whether the row counts L2R or R2L
     * @param r index value from the gameboard for the current row
     * @param cng whether any change has yet been recorded 
     * compared to the prior board state
     * @param nBoard the new state of the gameboard that is being built
     * @param eR a list of all empty cell regions in the new gameboard 
     * @return whether this row has changed vs the prior game state
     *****************************************************************/
    private boolean rowCompact(int length, 
                                Queue<Integer> q, 
                                int iterator, 
                                int r, 
                                boolean cng, 
                                int[][] nBoard, 
                                LinkedList<Empties> eR) {
        // yes I know that many parameters is a code smell
        
        int counter = iterator < 0 ? length - 1 : 0;
        // if row is empty, add to empty region representation
        if (q.isEmpty()) {
            eR.add(new Empties(length - 1, 0, r, true));
        }

        // while there are values in the queue, add them to new board
        while (!q.isEmpty()) {
            int cur = q.remove();
            if (q.isEmpty()) {
                if (cur >= winValue) {
                    status = GameStatus.USER_WON;
                }
                nBoard[r][counter] = cur;

                // if new and old board differ, trip changed flag
                if (nBoard[r][counter] != board[r][counter]) {
                    cng = true;
                }
                if (iterator < 0 && counter > 0) {
                    eR.add(new Empties(counter -1, 0, r, true));
                } else if (counter + 1 <= length - 1) {
                    eR.add(new Empties(length - 1, 
                                        counter + 1, r, true));
                }
                
            } else if (cur == q.element()) {
                int next = q.remove();
                if (cur + next >= winValue) {
                    status = GameStatus.USER_WON;
                }
                nBoard[r][counter] = cur + next;
                if (nBoard[r][counter] != board[r][counter]) {
                    cng = true;
                }
                if (q.isEmpty()) {
                    if (iterator < 0 && counter > 0) {
                        eR.add(new Empties(counter - 1, 0, r, true));
                    } else if (counter + 1 <= length - 1) {
                        eR.add(new Empties(length - 1, 
                                            counter + 1, r, true));
                    }
                }
                
                counter = counter + iterator;
                
            } else {
                if (cur >= winValue) {
                    status = GameStatus.USER_WON;
                }
                nBoard[r][counter] = cur;
                if (nBoard[r][counter] != board[r][counter]) {
                    cng = true;
                }
                counter = counter + iterator;
            } 
        }
        return cng;
    }
    
    /******************************************************************
     * Compacts a column on the game board, evaluating whether any 
     * change occurs from the board's prior state, whether the 
     * winning value is reached, and tracks whatever empty space 
     * is available in this column for inserting a new cell
     * @param length number of cells in the board's columns
     * @param q a queue of all the cells to compact
     * @param iterator whether the column counts top2bottom or reverse
     * @param r index value from the gameboard for the current column
     * @param cng whether any change has yet been recorded 
     * compared to the prior board state
     * @param nBoard the new state of the gameboard that is being built
     * @param eR a list of all empty cell regions in the new gameboard 
     * @return whether this column has changed vs the prior game state
     *****************************************************************/
    private boolean colCompact(int length, 
                                Queue<Integer> q, 
                                int iterator, 
                                int col, 
                                boolean cng, 
                                int[][] nBoard, 
                                LinkedList<Empties> eR) {

        int counter = iterator < 0 ? length - 1 : 0;
        if (q.isEmpty()) {
           eR.add(new Empties(length - 1, 0, col, false));
       }

        while (!q.isEmpty()) {
            int cur = q.remove();
            if (q.isEmpty()) {
                if (cur >= winValue) {
                    status = GameStatus.USER_WON;
                }
                nBoard[counter][col] = cur;
                
                if (nBoard[counter][col] != board[counter][col]) {
                    cng = true;
                }
                
                if (iterator < 0 && counter > 0) {
                    eR.add(new Empties(counter -1, 0, col, false));
                } else if (counter + 1 <= length - 1) {
                    eR.add(new Empties(length - 1, 
                            counter + 1, col, false));
                }
            } else if (cur == q.element()) {
                int next = q.remove();
                
                if (cur + next >= winValue) {
                    status = GameStatus.USER_WON;
                }
                nBoard[counter][col] = cur + next;
                
                if (nBoard[counter][col] != board[counter][col]) {
                    cng = true;
                }
                
                if (q.isEmpty()) {
                    if (iterator < 0 && counter > 0) {
                        eR.add(new Empties(counter - 1, 0, col, false));
                    } else if (counter + 1 <= length - 1) {
                        eR.add(new Empties(length - 1, 
                                counter + 1, col, false));
                    }
                }
                
                counter = counter + iterator;
                
            } else {
                if (cur >= winValue) {
                    status = GameStatus.USER_WON;
                }
                nBoard[counter][col] = cur;
                
                if (nBoard[counter][col] != board[counter][col]) {
                    cng = true;
                }
                
                counter = counter + iterator;
            } 
        }
        return cng;
    }
    
    /****************************************************************** 
     * determines if any legal move can be made by the player
     * @param b the board to evaluate as a 2D array of ints
     * @return whether there is any legal move
     *****************************************************************/
    private boolean anyMoves(int[][] b) {
        for (int r = 0; r < b.length; r++) {
            for (int c = 0; c < b[r].length; c++) {
                
                // always have a move if empty space left
                if (b[r][c] == 0) {
                    return true;
                }
                if (r < b.length - 1) {
                    if (b[r][c] == b[r + 1][c]){
                        return true;
                    }
                }
                if (c < b[r].length -1) {
                    if (b[r][c] == b[r][c + 1]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /******************************************************************
     * provides a list of empty game tiles independent of a 
     * given gamestate to implement the placeRandomValue() method
     * @return a linked list of empty cells
     *****************************************************************/
    public LinkedList<Cell> getEmptyTiles() {
        LinkedList<Cell> rtn = new LinkedList<Cell>();
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                if (board[r][c] == 0)
                    rtn.add(new Cell(r, c, 0));
            }
        }
        return rtn;
    }
    
    /******************************************************************
     * Prints a representation of the given gameboard to the terminal
     * @param b game board as a 2D array of ints
     *****************************************************************/
    private void printBoard(int[][] b) {
        System.out.println("Board: ");
        for (int r = 0; r < b.length; r++){
            for (int c = 0; c < b[r].length; c++) {
                System.out.print(b[r][c] + " ");
            }
            System.out.println("");
        }
    }
        
}
