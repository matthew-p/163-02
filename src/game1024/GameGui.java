package game1024;
import java.awt.*;
import java.util.ArrayList;

import javax.swing.*; 

@SuppressWarnings("serial")
public class GameGui extends JFrame {

    private int rows; 
    private int cols; 
    private int winVal;
    private final static int SIZE = 800;
    private final int GAP = 2; 
    private int num = rows * cols; 
    private int x; 
    private JPanel mainBoardPanel; 
    private JPanel[] cellPanels = new JPanel[num]; 
    private Color clr = Color.RED; 
    private Color clr2 = Color.BLUE; 
    private Color tColor; 
      
    //here 
    private NumberSlider game;
    private int[][] guiBoard;
      
    public GameGui(int rws, int cls) { 
        super("Gameboard in Java Swing"); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        rows = rws;
        cols = cls;
        winVal = 1024;
        game = new NumberGame();
        game.resizeBoard(rows, cols, winVal);
        /* Place the first two random tiles */
        game.placeRandomValue();
        game.placeRandomValue();
        mainBoardPanel = new JPanel();
        this.add(mainBoardPanel); 
    }
    
    public static void main(String[] args) { 
        GameGui gameGui = new GameGui(15, 30); 
     // add it to the JFrame
        Board mainPanel = gameGui.new Board(gameGui.rows, gameGui.cols, gameGui.game.getNonEmptyTiles());
        gameGui.setSize(SIZE, SIZE); 
        gameGui.getContentPane().add(mainPanel);
      //  gameGui.pack();
        gameGui.setLocationRelativeTo(null);
        gameGui.setVisible(true);
        
        
    } 
    
    private class Board extends JPanel {
        int cellSize;
        int width;
        int height;
        int rows;
        int cols;
        ArrayList<Cell> gameBoard;
        Font FONT = new Font("Courier New", Font.BOLD, 12);
        
        public Board(int r, int c, ArrayList<Cell> b) {
            rows = r;
            cols = c;
            gameBoard = b;
            width = getWidth();
            height = getHeight();
        }
        
        public void centeredString(String text, Rectangle rect, Font font, Graphics g) {
            FontMetrics metrics = g.getFontMetrics(font);
            // the X coordinate for the text
            int x = (rect.width - metrics.stringWidth(text)) / 2 + (int)(rect.getX());
            // the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
            int y = ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent() + (int)(rect.getY());
            g.drawString(text, x, y);
        }
        
        public void paintComponent(Graphics g) {
            width = getWidth();
            height = getHeight();
            if (rows <= cols) {
                cellSize = width / cols;
            } else {
                cellSize = height / rows;
            }
            int[][] b = new int[rows][cols];
            for (Cell c : gameBoard) {
                b[c.row][c.column] = c.value;
            }
            
            g.setColor(Color.BLACK);
            g.fillRect(0,0, width, height);
            g.setColor(Color.BLUE);
            g.setFont(FONT);
            int cIndex = 0;
            int rIndex = 0;
            for (int y = 0; y < rows; y += 1) {
                int yStart = y * cellSize;
                for (int x = 0; x < cols; x += 1) {
                    int xStart = x * cellSize;
                    if (b[y][x] > 0) {
                        g.setColor(Color.BLUE);
                        g.fillRect(xStart, yStart, cellSize, cellSize);
                        g.setColor(Color.YELLOW);
                        centeredString(Integer.toString(b[y][x]), new Rectangle(xStart, yStart, cellSize, cellSize), FONT, g);
                       // g.drawString(Integer.toString(b[rIndex][cIndex]), x * cellSize, y * cellSize + cellSize / 2);
                    } else {
                        g.setColor(Color.WHITE);
                        g.fillRect(xStart, yStart, cellSize, cellSize);
                        g.setColor(Color.GREEN);
                        centeredString(x + ":" + y, new Rectangle(xStart, yStart, cellSize, cellSize), FONT, g);
                        //g.drawString((Integer.toString(x) + ":" + y), x * cellSize, y * cellSize + cellSize / 2);
                    }
                    cIndex++;
                }
                cIndex = 0;
                rIndex++;
            }
        }
    }
} 


