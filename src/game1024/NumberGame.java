package game1024;

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
