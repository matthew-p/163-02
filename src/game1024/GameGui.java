package game1024;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.*;

/**********************************************************************
 * The GUI class for 2017 Winter GVSU CIS163 project 3 2048 game 
 * @author Matthew Pische
 *
 *********************************************************************/
@SuppressWarnings("serial")
public class GameGui extends JFrame implements ActionListener {

    private int rows; 
    private int cols; 
    private int winVal;
    private final static int SIZE = 800; 
    private int ttlGames;
    private int highScr;
    private NumberSlider game;
    private JPanel chromeTopPanel, chromeBottomPanel;
    private JButton undoButton, resizeButton, resetButton;
    private JTextField widthField, heightField, winValField;
    private JLabel widthLabel, 
                   heightLabel, 
                   winValLabel, 
                   totalGamesLabel,
                   totalGamesValLabel,
                   highScoreLabel,
                   highScoreValLabel;
    private Board mainPanel;
    
    /******************************************************************
     * Constructor for the primary GUI class, sets up everything
     * @param rws starting number of rows on the game board
     * @param cls starting number of columns on the game board
     *****************************************************************/
    public GameGui(int rws, int cls) { 
        super("Gameboard in Java Swing"); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        rows = rws;
        cols = cls;
        winVal = 1024;
        ttlGames = 1;
        
        game = new NumberGame();
        game.resizeBoard(rows, cols, winVal);
        /* Place the first two random tiles */
        game.placeRandomValue();
        game.placeRandomValue();
        // starting values might not be 2
        highScr = findHigh();
        
        setSize(SIZE, SIZE);
        setLocationRelativeTo(null);
        Container pane = getContentPane();
        chromeTopPanel = new JPanel();
        chromeBottomPanel = new JPanel();
        
        undoButton = new JButton("Undo");
        undoButton.addActionListener(this);
        chromeBottomPanel.add(undoButton);
        
        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        chromeBottomPanel.add(resetButton);
        
        widthLabel = new JLabel("Columns: ");
        chromeTopPanel.add(widthLabel);
        widthField = new JTextField();
        widthField.setColumns(8);
        chromeTopPanel.add(widthField);
        
        heightLabel = new JLabel("Rows: ");
        heightField = new JTextField();
        heightField.setColumns(8);
        chromeTopPanel.add(heightLabel);
        chromeTopPanel.add(heightField);
        
        winValLabel = new JLabel("Winning Value: ");
        winValField = new JTextField();
        winValField.setColumns(8);
        chromeTopPanel.add(winValLabel);
        chromeTopPanel.add(winValField);
        
        resizeButton = new JButton("Resize Board");
        resizeButton.addActionListener(this);
        chromeTopPanel.add(resizeButton);
        
        totalGamesLabel = new JLabel("Games Played: ");
        totalGamesValLabel = new JLabel(Integer.toString(ttlGames));
        chromeBottomPanel.add(totalGamesLabel);
        chromeBottomPanel.add(totalGamesValLabel);
        
        highScoreLabel = new JLabel("High Score: ");
        highScoreValLabel = new JLabel(Integer.toString(highScr));
        chromeBottomPanel.add(highScoreLabel);
        chromeBottomPanel.add(highScoreValLabel);
        
        mainPanel = new Board(rows, cols, 
                            game.getNonEmptyTiles(), winVal);
        buildBoard(mainPanel);
        
        pane.add(mainPanel, BorderLayout.CENTER);
        pane.add(chromeTopPanel, BorderLayout.NORTH);
        pane.add(chromeBottomPanel, BorderLayout.SOUTH);
    }
    
    public static void main(String[] args) { 
        GameGui gameGui = new GameGui(8, 8); 
        
        gameGui.setVisible(true);   
        gameGui.mainPanel.requestFocusInWindow();
        gameGui.mainPanel.grabFocus();
    } 
    
    /******************************************************************
     * Function to build the game board in the GUI constructor
     * @param mainPanel the GUI board representation
     *****************************************************************/
    private void buildBoard(Board mainPanel) {
        mainPanel.setFocusable(true);
        mainPanel.requestFocusInWindow();
        mainPanel.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent k) {
                int code = k.getKeyCode();
                if (code == KeyEvent.VK_LEFT) {
                    game.slide(SlideDirection.LEFT);
                    mainPanel.gameBoard = game.getNonEmptyTiles();
                    mainPanel.repaint();
                } else if (code == KeyEvent.VK_RIGHT) {
                    game.slide(SlideDirection.RIGHT);
                    mainPanel.gameBoard = game.getNonEmptyTiles();
                    mainPanel.repaint();
                } else if (code == KeyEvent.VK_UP) {
                    game.slide(SlideDirection.UP);
                    mainPanel.gameBoard = game.getNonEmptyTiles();
                    mainPanel.repaint();
                } else if (code == KeyEvent.VK_DOWN) {
                    game.slide(SlideDirection.DOWN);
                    mainPanel.gameBoard = game.getNonEmptyTiles();
                    mainPanel.repaint();
                }
                int high = findHigh();
                if (high > highScr) {
                    highScr = high;
                    highScoreValLabel.setText(
                            Integer.toString(highScr));
                }
                if (high >= winVal) {
                    ttlGames += 1;
                    totalGamesValLabel.setText(
                            Integer.toString(ttlGames));
                    JOptionPane.showMessageDialog(null, "You Won!"); 
                }
            }
        });      
    }
    
    /******************************************************************
     * Handler for GUI events
     * @param e the source event
     ******************************************************************/
    public void actionPerformed(ActionEvent e) {
        Object btn = e.getSource();
        if (btn == undoButton) {
            try {
                game.undo();
                mainPanel.repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Undo Failed");
            } finally {
                mainPanel.requestFocusInWindow();
            } 
        } else if (btn == resetButton) {
            try {
                game.resizeBoard(rows, cols, winVal);
                game.placeRandomValue();
                game.placeRandomValue();
                int high = findHigh();
                if (high > highScr) {
                    highScr = high;
                    highScoreValLabel.setText(
                            Integer.toString(highScr));
                }
                ttlGames += 1;
                totalGamesValLabel.setText(Integer.toString(ttlGames));
                mainPanel.repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Reset Failure: " 
                                                    + ex.getMessage());
            } finally {
                mainPanel.requestFocusInWindow(); 
            }     
        } else if (btn == resizeButton) {
            String w = widthField.getText();
            String h = heightField.getText();
            String v = winValField.getText();
            if (!tryParseInt(w) || !tryParseInt(h) || !tryParseInt(v)) {
                JOptionPane.showMessageDialog(null, 
                            "Columns, Rows, & Winning Value must " + 
                            "be whole, positive nummbers");
            } else if (Integer.parseInt(w) <= 0 || 
                       Integer.parseInt(h) <= 0 || 
                       Integer.parseInt(v) <= 0) {
                JOptionPane.showMessageDialog(null, 
                            "All values must be positive numbers");
            } else if (!((Integer.parseInt(v) 
                         & -(Integer.parseInt(v))) 
                         == Integer.parseInt(v))) {
                JOptionPane.showMessageDialog(null, 
                        "Win value must be power of 2");
            } else {
                rows = Integer.parseInt(h);
                cols = Integer.parseInt(w);
                winVal = Integer.parseInt(v);
                try {
                    game.resizeBoard(rows, cols, winVal);
                    game.placeRandomValue();
                    game.placeRandomValue();
                    int high = findHigh();
                    if (high > highScr) {
                        highScr = high;
                        highScoreValLabel.setText(
                                Integer.toString(highScr));
                    }
                    ttlGames += 1;
                    totalGamesValLabel.setText(Integer
                                                .toString(ttlGames));
                    mainPanel.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, 
                                            "Resize Failure: " 
                                            + ex.getMessage());
                } finally {
                    mainPanel.requestFocusInWindow();
                }
            }
        }
    }
    
    /******************************************************************
     * Helper method to return the highest cell value on the board
     * @return the highest value on the board
     ******************************************************************/
    private int findHigh() {
        int high = 0;
        for (Cell c : game.getNonEmptyTiles()) {
            if (high < c.value) {
                high = c.value;
            }
        }
        return high;
    }
    
    /******************************************************************
     * Helper method to deal with non-numeric strings
     * @param value the string to see if parsible into an int
     * @return if string is parsable 
     ******************************************************************/
    boolean tryParseInt(String value) {  
        try {  
            Integer.parseInt(value);  
            return true;  
         } catch (Exception e) {  
            return false;  
         }  
   }
    
    /******************************************************************
     * A class to build and render the game board as a JPanel
     * @author Matthew Pische
     *
     *****************************************************************/
    private class Board extends JPanel {
        int cellSize;
        int width;
        int height;
        int bRows;
        int bCols;
        ArrayList<Cell> gameBoard;
        Font FONT = new Font("Courier New", Font.BOLD, 12);
        
        public Board(int r, int c, ArrayList<Cell> b, int winV) {
            bRows = r;
            bCols = c;
            gameBoard = b;
            width = getWidth();
            height = getHeight();
        }
        
        /**************************************************************
         * Generates centered text in a given rectangle 
         * by finding the bounds of the text when rendered as a
         * rectangle, taking the difference of these bounds from 
         * the holding rectangle, and splitting that difference
         * @param text the text to render
         * @param rect the rectangle bounds to hold the text
         * @param font the font for the text
         * @param g the current graphics context
         **************************************************************/
        public void centeredString(String text, 
                                   Rectangle rect, 
                                   Font font, 
                                   Graphics g) {
            FontMetrics metrics = g.getFontMetrics(font);
            // text x coordinate
            int x = (rect.width - metrics.stringWidth(text)) / 2 
                                                + (int)(rect.getX());
            // text y coordinate with ascent
            int y = ((rect.height - metrics.getHeight()) / 2) 
                                               + metrics.getAscent() 
                                               + (int)(rect.getY());
            g.drawString(text, x, y);
        }
        
        /**************************************************************
         * render the game board on refreshing events
         * @param g the graphics context for the board
         *************************************************************/
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            gameBoard = game.getNonEmptyTiles();
            bRows = rows;
            bCols = cols;
            width = getWidth();
            height = getHeight();
            
            // get square cell size that will fit the side with 
            // the least space
            if (bCols > bRows) {
                cellSize = width / bCols;
            } else if (bRows > bCols) {
                cellSize = height / bRows;
            } else {
                cellSize = height / bRows;
            }
            
            // make offsets to center the game board
            int horGap = width - bCols * cellSize;
            int vertGap = height - bRows * cellSize;
            
            int xOrg = horGap / 2;
            int yOrg = vertGap / 2;
            
            // make a representation of the full game state
            int[][] b = new int[bRows][bCols];
            for (Cell c : gameBoard) {
                b[c.row][c.column] = c.value;
            }
            
            // black void surrounding game board
            g.setColor(Color.BLACK);
            g.fillRect(0,0, width, height);
            g.setColor(Color.BLUE);
            g.setFont(FONT);
            for (int y = 0; y < bRows; y += 1) {
                int yStart = y * cellSize + yOrg;
                for (int x = 0; x < bCols; x += 1) {
                    int xStart = x * cellSize + xOrg;
                    
                    // highlight winning cell
                    if (b[y][x] >= winVal) {
                        g.setColor(Color.ORANGE);
                        g.fillRect(xStart, yStart, cellSize, cellSize);
                        g.setColor(Color.BLACK);
                        centeredString(Integer.toString(b[y][x]), 
                                       new Rectangle(xStart, 
                                              yStart, 
                                              cellSize, 
                                              cellSize), 
                                       FONT, 
                                       g);
                        
                    // color normal cells with values
                    } else if (b[y][x] > 0) {
                        g.setColor(Color.BLUE);
                        g.fillRect(xStart, yStart, cellSize, cellSize);
                        g.setColor(Color.YELLOW);
                        centeredString(Integer.toString(b[y][x]), 
                                       new Rectangle(xStart, 
                                                     yStart, 
                                                     cellSize, 
                                                     cellSize), 
                                       FONT, 
                                       g);
                    // make empty cells white
                    } else {
                        g.setColor(Color.GRAY);
                        g.fillRect(xStart, yStart, cellSize, cellSize);
                        g.setColor(Color.WHITE);
                        g.fillRect(xStart + 1, 
                                   yStart + 1, 
                                   cellSize - 1, 
                                   cellSize -1);
                    }
                }
            }
        }
    }
} 


