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
    
    @Override
    public boolean slide(SlideDirection dir) {
        // TODO Auto-generated method stub
        /*
         * start at the 'wall', the direction-most array cell & work 
         * backward, so if eastward, begin in last cell of row array 
         * looping down to zero; but if westward, begin with zero to
         * max array length. 
         * Use three var pointers, holding index 
         * values:  
         * most recent un-merged cell (if any),
         * direction-most empty cell (if any),
         * current looping index
         * 
         * store first index as empty or unmerged, depending, walk one
         * over, if new cell is empty & most recent unmerged is null, 
         * set that var to position index & increment loop. If value,
         * if value is mergable with most recent un-merged, add to 
         * most recent unmerged's cell's value. set most recent unmerged
         * to empty & the current i cell's value to empty. If unmerged 
         * is unmergeable with i cell value (or empty / wall), 
         * update most empty to value, set i cell to empty. Increment loop.    
         * 
         */
        
        /*
         * feb 14th
         * make a new holder array with x variable to track 
         * its own pointer (uses same row as main board loop), 
         * 
         * iterate over each row of board,
         * main board variable holds most recent cell with a value,
         * beginning null, fill it with first value found,
         * when iterator hits new cell with a value, 
         * if values == the var value, place double this value in the cell 
         * of the reciever array's x pointer, if != place 
         * value in reciever array's pointer cell, and increment 
         * that pointer. Also update main board holder to current 
         * cell. 
         * 
         * reciever doesn't have to be the entire board, 
         * just the active row. Rebuilding the board one row by one
         * as you iterate over them. 1 dimensional holder array 
         * 
         * at end, assign new reciever board to main board var
         */
        
        /*
         * feb 15th do this as a queue instead of a holding array
         */
        
        // do everything as doublely linked list? empty cells don't exist???
        
        boolean cng = false;
        
        if (dir == SlideDirection.RIGHT) {
            for (int r = 0; r < board.length; r++) {
                Queue<Integer> q = new LinkedList<Integer>(); 
                for (int c = board[r].length - 1; c >= 0; c--) {
                    if (board[r][c] != 0) {
                        q.add(board[r][c]);
                    }
                }
                /*
                 * if the removed queue value and the queue.element() peek
                 * are equal, remove the peeked value, combine them, 
                 * assign them to the array at index counter value, 
                 * increment index counter.
                 * else if the removed value != peeked val,
                 * assign the removed val to the array[counter]
                 * and increment the counter.
                 * 
                 *
                int[] nRow = new int[board[r].length];
                int counter = board[r].length - 1;

                while (!q.isEmpty()) {
                    int cur = q.remove();
                    if (q.isEmpty()) {
                        nRow[counter] = cur;
                    } else if (cur == q.element()) {
                        int next = q.remove();
                        nRow[counter] = cur + next;
                        counter -= 1;
                    } else {
                        nRow[counter] = cur;
                        counter -= 1;
                    } 
                }
                
                board[r] = nRow;
                */
                board[r] = rowCompact(board[r].length, q, -1);
            }
        
        /*
        if (dir == SlideDirection.RIGHT) {
            
            for (int r = 0; r < board.length; r++) {
                int[] rRow = new int[board[r].length];
                int rRowPointer = board[r].length - 1;
                
                Cell lastCV = null;
                
                for (int c = board[r].length - 1; c >= 0; c--) {
                    if (c == 0) {
                        if (board[r][c] != 0) {
                            if (lastCV != null) {
                                if (board[r][c] == lastCV.value) {
                                    rRow[rRowPointer] = lastCV.value + board[r][c];
                                } else {
                                    rRow[rRowPointer] = lastCV.value;
                                    rRowPointer -= 1;
                                    rRow[rRowPointer] = board[r][c];
                                }
                                
                            } else {
                                rRow[rRowPointer] = board[r][c];
                            }

                        }
                        
                    } else if (board[r][c] != 0) {
                        if (lastCV == null) {
                            lastCV = new Cell(r,c, board[r][c]);
                        } else {
                            if (lastCV.value == board[r][c]) {
                                rRow[rRowPointer] = board[r][c] + lastCV.value;
                                lastCV = null;
                                rRowPointer -= 1;
                                cng = true;
                            } else {
                                rRow[rRowPointer] = lastCV.value;
                                rRowPointer -= 1;
                                lastCV = new Cell(r,c, board[r][c]);
                            }
                        }
                    }
                }
                board[r] = rRow;
            }
            */
        
            
            
            /* original, too complicated, mutating array in place
            for (int r = 0; r < board.length; r++) {
                Cell firstEmpty = null;
                Cell lastUnmerged = null;
                for (int c = board[0].length - 1; c >= 0; c--) {
                    // if no empty cell stored, & this cell is empty,
                    // store it to first empty cell 
                        
                    // if current has a value,  
                    if (board[r][c] != 0) {
                        if (lastUnmerged != null) {
                            if (lastUnmerged.value == board[r][c]){
                                board[lastUnmerged.row][lastUnmerged.column] = lastUnmerged.value + board[r][c];
                                lastUnmerged = null;
                                board[r][c] = 0;
                                cng = true;
                               // continue;
                            } else if (firstEmpty != null) {
                                board[firstEmpty.row][firstEmpty.column] = board[r][c];
                                lastUnmerged = new Cell(firstEmpty.row, firstEmpty.column, board[r][c]);
                                firstEmpty = new Cell(firstEmpty.row, firstEmpty.column - 1, 0);
                                board[r][c] = 0;
                                cng = true;
                            } else {
                                lastUnmerged = new Cell(r,c,board[r][c]);
                            }
                            
                        } else {
                            if (firstEmpty != null) {
                                board[firstEmpty.row][firstEmpty.column] = board[r][c];
                                lastUnmerged = new Cell(firstEmpty.row, firstEmpty.column, board[r][c]);
                                firstEmpty = new Cell(firstEmpty.row, firstEmpty.column - 1, 0);
                                board[r][c] = 0;  
                                cng = true;
                            } else {
                                lastUnmerged = new Cell(r,c, board[r][c]);
                            }
                        }
                    } else {
                        if (firstEmpty == null) {
                            firstEmpty = new Cell(r, c, board[r][c]);
                        }    
                        
                    }        
                    
                }
            }
            */
            
        }
        if (dir == SlideDirection.LEFT) {
            for (int r = 0; r < board.length; r++) {
                Queue<Integer> q = new LinkedList<Integer>(); 
                for (int c = 0; c < board[r].length; c++) {
                    if (board[r][c] != 0) {
                        q.add(board[r][c]);
                    }
                }
                board[r] = rowCompact(board[r].length, q, 1);
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
                colCompact(c, board[0].length, q, 1);
            }
        }
        
        //TODO back fill cells that should be empty 
        // after queue exhausted
        
        if (dir == SlideDirection.DOWN) {
            for (int c = board[0].length -1; c >= 0; c--) {
                Queue<Integer> q = new LinkedList<Integer>();
                for (int r = board.length - 1; r >= 0; r--) {
                    if (board[r][c] != 0) {
                        q.add(board[r][c]);
                        board[r][c] = 0;
                    }
                }
                colCompact(c, board[0].length, q, -1);
            }
        }
        
        placeRandomValue();
        return true;
    }
    
    private void colCompact(int col, int length, Queue<Integer> q, int iterator) {

        int counter = iterator < 0 ? length - 1 : 0;

        while (!q.isEmpty()) {
            int cur = q.remove();
            if (q.isEmpty()) {
                //nRow[counter] = cur;
                board[counter][col] = cur;
            } else if (cur == q.element()) {
                int next = q.remove();
                // nRow[counter] = cur + next;
                board[counter][col] = cur + next;
                counter = counter + iterator;
            } else {
                //nRow[counter] = cur;
                board[counter][col] = cur;
                counter = counter + iterator;
            } 
        }
        
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
