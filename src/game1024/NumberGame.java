package game1024;

import java.util.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

public class NumberGame implements NumberSlider {

    private int[][] board;
    private int winValue;
    private GameStatus status;
    private Stack<int[][]> undos;
    private Stack<int[][]> redos;
    
    /**
     * Default constructor sets gameboard to 4x4 cells, 
     * and win value to 1024
     */
    public NumberGame() {
        board = new int[4][4];
        winValue = 1024;
        status = GameStatus.IN_PROGRESS;
        undos = new Stack<int[][]>();
        redos = new Stack<int[][]>();
    }
    
    /**
     * Takes a single value to set the game board's length and width,
     * win value defaults to 1024
     * @param length
     */
    public NumberGame(int length) {
        board = new int[length][length];
        winValue = 1024;
        status = GameStatus.IN_PROGRESS;
        undos = new Stack<int[][]>();
        redos = new Stack<int[][]>();
    }
    
    /**
     * Takes different row and column dimensions for the game board,
     * win value defaults to 1024
     * @param rowLength
     * @param colLength
     */
    public NumberGame(int rowLength, int colLength) {
        board = new int[rowLength][colLength];
        winValue = 1024;
        status = GameStatus.IN_PROGRESS;
        undos = new Stack<int[][]>();
        redos = new Stack<int[][]>();
    }
    
    /**
     * Set different board dimensions, and a custom win value
     * @param rowLength
     * @param colLength
     * @param winValue
     */
    public NumberGame(int rowLength, int colLength, int winValue) {
        board = new int[rowLength][colLength];
        this.winValue = winValue;
        status = GameStatus.IN_PROGRESS;
        undos = new Stack<int[][]>();
        redos = new Stack<int[][]>();
    }
    
    @Override
    public void resizeBoard(int height, int width, int winValue) {
        board = new int [height][width];
        this.winValue = winValue;
        undos = new Stack<int[][]>();
        status = GameStatus.IN_PROGRESS;
    }

    @Override
    public void reset() {
        board = new int[board.length][board[0].length];
        undos = new Stack<int[][]>();
        placeRandomValue();
        placeRandomValue();
        status = GameStatus.IN_PROGRESS;
    }

    @Override
    public void setValues(int[][] ref) {
        int rows = ref.length;
        int cols = ref[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c] = ref[r][c];
            }
        }
    }

    /**
     * Gets a randomized value for a new game cell, 
     * 90% chance of a 2, 10% of a 4
     * @return numeric cell value
     */
    private int newCellVal() {
        // NOTE: canonical implementation of this game uses 
        // a 90/10 value distribution of 2s & 4s for new random tiles
        // https://github.com/gabrielecirulli/2048/blob/master/js/game_manager.js#L71
        return (new Random()).nextInt(10) < 9 ? 2 : 4;
    }
    
    @Override
    public Cell placeRandomValue() {
        LinkedList<Cell> empty = getEmptyTiles();
        Cell c = empty.get((new Random()).nextInt(empty.size()));
        board[c.row][c.column] = newCellVal();
        return c;
    }
    
    /**
     * Prints a representation of the given gameboard to the terminal
     * @param b a game board as a 2D array of ints
     */
    private void printBoard(int[][] b) {
        System.out.println("Board: ");
        for (int r = 0; r < b.length; r++){
            for (int c = 0; c < b[r].length; c++) {
                System.out.print(b[r][c] + " ");
            }
            System.out.println("");
        }
    }
    
    /**
     * A class where each instance represents the 
     * empty cells in a given row, done to facilitate adding a 
     * new random cell into the available empty space on the gameboard
     * @author m
     *
     */
    private class Empties {
        private int max;
        private int min;
        private int rOrCIndex;
        private boolean isRow;
        
        /**
         * returns a new representation of the empty cells in a 
         * row or column of the gameboard
         * @param max the highest empty cell index in the row or column
         * @param min the first empty cell index in the row or column
         * @param rOrCIndex the column or row index for this instance
         * @param isRow whether the instance is a row or column
         */
        public Empties(int max, int min, int rOrCIndex, boolean isRow) {
            this.min = min;
            this.max = max;
            this.rOrCIndex = rOrCIndex;
            this.isRow = isRow;
        }
        /**
         * Creates a new Cell object in one of the available empty 
         * spaces in this row or column
         * @return a cell in a previously empty space in this row/column
         */
        public Cell randomCell() {
            int rnd = (new Random()).nextInt((max - min) + 1) + min;
            if (isRow) {
                return new Cell(rOrCIndex, rnd, newCellVal());
            } else {
                return new Cell(rnd, rOrCIndex, newCellVal());
            }
        }
    }
    
    /**
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
     */
    private boolean rowCompact(int length, 
                                Queue<Integer> q, 
                                int iterator, 
                                int r, 
                                boolean cng, 
                                int[][] nBoard, 
                                LinkedList<Empties> eR) {
        
        int counter = iterator < 0 ? length - 1 : 0;
        if (q.isEmpty()) {
         // add to the list of empty regions in columns
            eR.add(new Empties(length - 1, 0, r, true));
        }

        while (!q.isEmpty()) {
            int cur = q.remove();
            if (q.isEmpty()) {
                if (cur >= winValue) {
                    status = GameStatus.USER_WON;
                }
                nBoard[r][counter] = cur;

                if (nBoard[r][counter] != board[r][counter]) {
                    cng = true;
                }
                // add to the list of empty regions in columns
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
                // add to the list of empty regions in columns
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
    
    /**
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
     */
    private boolean colCompact(int length, 
                                Queue<Integer> q, 
                                int iterator, 
                                int col, 
                                boolean cng, 
                                int[][] nBoard, 
                                LinkedList<Empties> eR) {

        int counter = iterator < 0 ? length - 1 : 0;
        if (q.isEmpty()) {
           // add to the list of empty regions in columns
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
                
                // add to the list of empty regions in columns
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
                
             // add to the list of empty regions in columns
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
    
    @Override
    public boolean slide(SlideDirection dir) {
        // NOTE: the behavior here seems wrong to me, 
        // as it will not place a cell in an empty board, or adding
        // a new cell to empty space doesn't count as a 'change', but 
        // that appears to be what the given tests demanded
        
        // make a new holder board array, 
        // that you will push all the queue'd cell values onto to,
        // checking each one against the original board value for 
        // that cell to see if any change,
        // also, make a LList that will hold a item
        // holding its row or col value, its starting empty cell #,
        // it's ending empty cell # (0 or length of the board row/col),
        // and a method to return a random value from its range.
        // so you can easily pull a random value first from the LList
        // length, then from that item in the list's item to 
        // place the new cell. 
        // If any change, push original board onto the undo stack, 
        // & update return value to true, & add
        // main class board value to new board holder
        // if no empty cells, no board state change, & no winning move
        // terminate game 
        
        printBoard(board);
          
        boolean cng = false;
        
        LinkedList<Empties> emptyRegion = new LinkedList<Empties>();
        
        int[][] newBoard = new int[board.length][board[0].length];
        
        if (dir == SlideDirection.RIGHT || dir == SlideDirection.LEFT) {
            for (int r = 0; r < board.length; r++) {
                Queue<Integer> q = new LinkedList<Integer>();
                
                if (dir == SlideDirection.RIGHT) {
                    for (int c = board[r].length - 1; c >= 0; c--) {
                        if (board[r][c] != 0) {
                            q.add(board[r][c]);
                        }
                    }
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
        } else {
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
        System.out.println("emptyRegion.size(): " + emptyRegion.size());
        
        if (emptyRegion.size() > 1) {
            
            Cell n = emptyRegion.get(
                        (new Random()).nextInt(emptyRegion.size()))
                        .randomCell();
            System.out.println("empty cell row: " + n.row + " col: " 
                                + n.column + " val: " + n.value);
            
            newBoard[n.row][n.column] = n.value;
        } else if (emptyRegion.size() == 1) {
            
            Cell n = emptyRegion.get(0).randomCell();
            System.out.println("empty cell row: " + n.row + " col: " 
                                + n.column + " val: " + n.value);
            
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
        printBoard(board);
        System.out.println("did board swipe change board state? " 
                            + cng +  "\n");
        return cng;
    }
    
    /** 
     * determines if any legal move can be made by the player
     * @param b the board to evaluate as a 2D array of ints
     * @return whether there is any legal move
     */
    private boolean anyMoves(int[][] b) {
        for (int r = 0; r < b.length; r++) {
            for (int c = 0; c < b[r].length; c++) {
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
    
    /**
     * provides a list of empty game tiles independent of a 
     * given gamestate to implement the placeRandomValue() method
     * @return a linked list of empty cells
     */
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
 
    @Override
    public void undo() {
        board = undos.pop();
    }

}
