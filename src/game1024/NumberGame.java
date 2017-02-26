package game1024;

import java.util.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

public class NumberGame implements NumberSlider {

    private int[][] board;
    private int wV;
    private GameStatus status;
    private Stack<int[][]> undos;
    private Stack<int[][]> redos;
    
    public NumberGame() {
        board = new int[4][4];
        wV = 1024;
        status = GameStatus.IN_PROGRESS;
    }
    
    @Override
    public void resizeBoard(int height, int width, int winningValue) {
        board = new int [height][width];
        wV = winningValue;
    }

    @Override
    public void reset() {
        board = new int[board.length][board[0].length];
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

    private int newCellVal() {
        // NOTE: canonical implementation of this game uses 
        // a 90/10 value distribution of 2s & 4s for new random tiles
        // https://github.com/gabrielecirulli/2048/blob/master/js/game_manager.js#L71
        return Math.random() < 0.9 ? 2 : 4;
    }
    
    @Override
    public Cell placeRandomValue() {
        // not clear to me why this needs to return a Cell
        LinkedList<Cell> empty = getEmptyTiles();
        Cell c = empty.get(new Random().nextInt(empty.size()));
        board[c.row][c.column] = newCellVal();
        return null;
    }

    private int[] rowCompact(int length, Queue<Integer> q, int iterator) {
        
        int[] nRow = new int[length];
        int counter = iterator < 0 ? length - 1 : 0;

        while (!q.isEmpty()) {
            int cur = q.remove();
            if (q.isEmpty()) {
                nRow[counter] = cur;
            } else if (cur == q.element()) {
                int next = q.remove();
                nRow[counter] = cur + next;
                counter = counter + iterator;
            } else {
                nRow[counter] = cur;
                counter = counter + iterator;
            } 
        }
        return nRow;
    }
    
    private boolean rowCompact(int length, Queue<Integer> q, int iterator, int r, boolean cng) {
        
        int counter = iterator < 0 ? length - 1 : 0;

        while (!q.isEmpty()) {
            int cur = q.remove();
            if (q.isEmpty()) {
                board[r][counter] = cur;
            } else if (cur == q.element()) {
                int next = q.remove();
                board[r][counter] = cur + next;
                counter = counter + iterator;
                cng = true;
            } else {
                board[r][counter] = cur;
                counter = counter + iterator;
            } 
        }
        return cng;
    }
    
    private boolean colCompact(int length, Queue<Integer> q, int iterator, int col,  boolean cng) {

        int counter = iterator < 0 ? length - 1 : 0;

        while (!q.isEmpty()) {
            int cur = q.remove();
            if (q.isEmpty()) {
                board[counter][col] = cur;
            } else if (cur == q.element()) {
                int next = q.remove();
                board[counter][col] = cur + next;
                counter = counter + iterator;
                cng = true;
            } else {
                board[counter][col] = cur;
                counter = counter + iterator;
            } 
        }
        return cng;
    }
    
    @Override
    public boolean slide(SlideDirection dir) {
        
        // TODO this is bad, must make a new holder board array, 
        // that you will push all the queue'd cell values onto to,
        // checking each one against the original board value for 
        // that cell to see if any change, until/unless change==true is 
        // set, then use || to short curcuit that evaluation maybe.
        // also, make a LList that will hold a struct item
        // holding its row or col value, its starting empty cell #,
        // it's ending empty cell # (zero or length of the board row/col),
        // and a method to return a random value from its range.
        // so you can easily pull a random value first from the LList
        // length, then from that item in the list's struct to 
        // place the new cell. 
        // If any change, push original board onto the undo stack, 
        // & update return value to true, & add
        // main class board value to new board holder
        // if no empty cells, no board state change, & no winning move
        // terminate game 
          
        boolean cng = false;
        
        int[][] newBoard = new int[board.length][board[0].length];
        
        if (dir == SlideDirection.RIGHT) {
            for (int r = 0; r < board.length; r++) {
                Queue<Integer> q = new LinkedList<Integer>(); 
                for (int c = board[r].length - 1; c >= 0; c--) {
                    if (board[r][c] != 0) {
                        q.add(board[r][c]);
                        board[r][c] = 0;
                    }
                }
                if (board[r].length > q.size())
                cng = rowCompact(board[r].length, q, -1, r, cng);
            }
        

        }
        if (dir == SlideDirection.LEFT) {
            for (int r = 0; r < board.length; r++) {
                Queue<Integer> q = new LinkedList<Integer>(); 
                for (int c = 0; c < board[r].length; c++) {
                    if (board[r][c] != 0) {
                        q.add(board[r][c]);
                        board[r][c] = 0;
                    }
                }
                cng = rowCompact(board[r].length, q, 1, r, cng);
            }
        }
        
        if (dir == SlideDirection.UP) {
            for (int c = 0; c < board[0].length; c++) {
                Queue<Integer> q = new LinkedList<Integer>();
                for (int r = 0; r < board.length; r++) {
                    if (board[r][c] != 0) {
                        q.add(board[r][c]);
                        board[r][c] = 0;
                    }
                }
                cng = colCompact(board[0].length, q, 1, c, cng);
            }
        }
        
        if (dir == SlideDirection.DOWN) {
            for (int c = 0; c < board[0].length; c++) {
                Queue<Integer> q = new LinkedList<Integer>();
                for (int r = board.length - 1; r >= 0; r--) {
                    if (board[r][c] != 0) {
                        q.add(board[r][c]);
                        board[r][c] = 0;
                    }
                }
                cng = colCompact(board[0].length, q, -1, c, cng);
            }
        }
        
        placeRandomValue();
        return true;
    }
    
    
    
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
    
    private boolean nn(Object obj) {
        if (obj == null)
            return false;
        else 
            return true;
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
        return status;
    }

    //TODO check if deep copy or just shallow copy of pointers 
    // you can use array lists in the undo methods 
    // to store away new arrayList of active cells via getNonEmptyTiles() 
    // improvement for deltas
    // save off just the changes
    @Override
    public void undo() {
        redos.push(board);
        board = undos.pop();
    }
    
    public void redo() {
        undos.push(board);
        board = redos.pop();
    }

}
