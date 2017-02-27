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
    
    public NumberGame() {
        board = new int[4][4];
        winValue = 1024;
        status = GameStatus.IN_PROGRESS;
        undos = new Stack<int[][]>();
        redos = new Stack<int[][]>();
    }
    
    public NumberGame(int length) {
        board = new int[length][length];
        this.winValue = 1024;
        status = GameStatus.IN_PROGRESS;
        undos = new Stack<int[][]>();
        redos = new Stack<int[][]>();
    }
    
    public NumberGame(int rowLength, int colLength) {
        board = new int[rowLength][colLength];
        this.winValue = 1024;
        status = GameStatus.IN_PROGRESS;
        undos = new Stack<int[][]>();
        redos = new Stack<int[][]>();
    }
    
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

    private int newCellVal() {
        // NOTE: canonical implementation of this game uses 
        // a 90/10 value distribution of 2s & 4s for new random tiles
        // https://github.com/gabrielecirulli/2048/blob/master/js/game_manager.js#L71
        return Math.random() < 0.9 ? 2 : 4;
    }
    
    @Override
    public Cell placeRandomValue() {
        LinkedList<Cell> empty = getEmptyTiles();
        Cell c = empty.get(new Random().nextInt(empty.size()));
        board[c.row][c.column] = newCellVal();
        return c;
    }
    
    private void printBoard(int[][] b) {
        System.out.println("Board: ");
        for (int r = 0; r < b.length; r++){
            for (int c = 0; c < b[r].length; c++) {
                System.out.print(b[r][c] + " ");
            }
            System.out.println("");
        }
    }
    
    private class Empties {
        private int max;
        private int min;
        private int rOrCIndex;
        private boolean isRow;
        
        
        public Empties(int max, int min, int rOrCIndex, boolean isRow) {
            this.min = min;
            this.max = max;
            this.rOrCIndex = rOrCIndex;
            this.isRow = isRow;
        }
        
        public Cell randomCell() {
            int rnd = (new Random()).nextInt((max - min) + 1) + min;
            if (isRow) {
                return new Cell(rOrCIndex, rnd, newCellVal());
            } else {
                return new Cell(rnd, rOrCIndex, newCellVal());
            }
        }
    }
    
    private boolean rowCompact(int length, Queue<Integer> q, int iterator, int r, boolean cng, int[][] nBoard, LinkedList<Empties> eR) {
        
        int counter = iterator < 0 ? length - 1 : 0;
        if (q.isEmpty()) {
         // add to the list of empty regions in columns
            eR.add(new Empties(length - 1, 0, r, true));
        }

        while (!q.isEmpty()) {
            int cur = q.remove();
            if (q.isEmpty()) {
                if (cur == winValue) {
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
                    eR.add(new Empties(length - 1, counter + 1, r, true));
                }
                
            } else if (cur == q.element()) {
                int next = q.remove();
                if (cur + next == winValue) {
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
                        eR.add(new Empties(length - 1, counter + 1, r, true));
                    }
                }
                
                counter = counter + iterator;
                
            } else {
                if (cur == winValue) {
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
    
    private boolean colCompact(int length, Queue<Integer> q, int iterator, int col,  boolean cng, int[][] nBoard, LinkedList<Empties> eR) {

        int counter = iterator < 0 ? length - 1 : 0;
        if (q.isEmpty()) {
           // add to the list of empty regions in columns
           eR.add(new Empties(length - 1, 0, col, false));
       }

        while (!q.isEmpty()) {
            int cur = q.remove();
            if (q.isEmpty()) {
                if (cur == winValue) {
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
                    eR.add(new Empties(length - 1, counter + 1, col, false));
                }
            } else if (cur == q.element()) {
                int next = q.remove();
                
                if (cur + next == winValue) {
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
                        eR.add(new Empties(length - 1, counter + 1, col, false));
                    }
                }
                
                counter = counter + iterator;
                
            } else {
                if (cur == winValue) {
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
        
        // make a new holder board array, 
        // that you will push all the queue'd cell values onto to,
        // checking each one against the original board value for 
        // that cell to see if any change,
        // also, make a LList that will hold a item
        // holding its row or col value, its starting empty cell #,
        // it's ending empty cell # (zero or length of the board row/col),
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
                    cng = rowCompact(board[r].length, q, -1, r, cng, newBoard, emptyRegion);
                }
                else { // Left
                    for (int c = 0; c < board[r].length; c++) {
                        if (board[r][c] != 0) {
                            q.add(board[r][c]);
                        }
                    }
                    cng = rowCompact(board[r].length, q, 1, r, cng, newBoard, emptyRegion);
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
                    cng = colCompact(board.length, q, 1, c, cng, newBoard, emptyRegion);
                } else { // Down
                    for (int r = board.length - 1; r >= 0; r--) {
                        if (board[r][c] != 0) {
                            q.add(board[r][c]);
                        }
                    }
                    cng = colCompact(board.length, q, -1, c, cng, newBoard, emptyRegion);
                }
            }
        }
        System.out.println("emptyRegion.size(): " + emptyRegion.size());
        
        if (emptyRegion.size() > 1) {
            
            Cell n = emptyRegion.get(
                        (new Random()).nextInt(emptyRegion.size()))
                        .randomCell();
            System.out.println("empty cell row: " + n.row + " col: " + n.column + " val: " + n.value);
            
            newBoard[n.row][n.column] = n.value;
        } else if (emptyRegion.size() == 1) {
            
            Cell n = emptyRegion.get(0).randomCell();
            System.out.println("empty cell row: " + n.row + " col: " + n.column + " val: " + n.value);
            
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
        System.out.println("did board swipe change board state? " + cng +  "\n");
        return cng;
    }
    
    
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
        // the way this is tested makes it necessary
        // to call a check across the game board
        // here, outside of any actual gameplay 
        if (!anyMoves(board)) {
            status = GameStatus.USER_LOST;
        }
        return status;
    }
 
    // you can use array lists in the undo methods 
    // to store away new arrayList of active cells via getNonEmptyTiles() 
    @Override
    public void undo() {
        board = undos.pop();
    }

}
