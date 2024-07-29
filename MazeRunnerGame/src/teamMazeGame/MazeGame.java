package teamMazeGame;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import edu.princeton.cs.algs4.BreadthFirstPaths;
import edu.princeton.cs.algs4.Graph;
import edu.princeton.cs.algs4.Queue;

/**
 * The MazeGame class represents a maze navigation game.
 * Players use the arrow keys to navigate from the start to the end point
 * (marked with a red circle) while avoiding walls and mountains.
 * Jumping over mountains is possible using the space bar and arrow keys.
 *
 * The class handles maze creation, player movement, game state, and completion.
 * It calculates the efficiency of the player's path compared to the shortest
 * path (found using Breadth-First Search) and provides visual feedback
 * through a graphical user interface.
 *
 * MazeGame extends JFrame and implements KeyListener for user input handling.
 *
 * @author Sahak I.
 * @author Terek Beckmann
 */

public class MazeGame extends JFrame implements KeyListener {
    
    private static final long serialVersionUID = 1L;
    
    // Constants for cell and window sizes
    private static int CELL_SIZE = 10;;
    private static final int DESIRED_WINDOW_WIDTH = 800;
    private static final int DESIRED_WINDOW_HEIGHT = 800;

    // Maze and player data
    private int mazeWidth;
    private int mazeHeight;
    private int[][] mazeGrid;
    private int endX, endY;
    private List<Integer> bfsPath;
    private int playerX, playerY;
    private BufferedImage playerImage;
    private BufferedImage mountainImage;
    private BufferedImage purpleImage;

    // Game state
    private boolean gameCompleted = false;
    private boolean spacePressed = false;
    private List<Integer> playerPath;
    private static final int PADDING = 0;


    /**
     * Constructor for MazeGame.
     *
     * @param difficultyLevel Difficulty level of the maze.
     */
    public MazeGame(int difficultyLevel) {
        initializeGame(difficultyLevel);
    }

    /**
     * Initializes the game with the specified difficulty level.
     *
     * @param difficultyLevel The difficulty level of the maze.
     */
    private void initializeGame(int difficultyLevel) {
        setTitle("Maze Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDifficultyLevel(difficultyLevel);

        adjustMazeDimensionsToFitWindow();
        setFrameSize();
        loadImages();
        generateMaze();
        
        // Add key listener and focus on the window
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
        
        // Initialize player path
        playerPath = new ArrayList<>();
        playerPath.add(playerY * mazeWidth + playerX);
        
        setVisible(true);
    }

    /**
     * Adjusts maze dimensions to fit the desired window size.
     */
    private void adjustMazeDimensionsToFitWindow() {
        int maxCellSizeWidth = DESIRED_WINDOW_WIDTH / mazeWidth;
        int maxCellSizeHeight = DESIRED_WINDOW_HEIGHT / mazeHeight;
        int newCellSize = Math.min(maxCellSizeWidth, maxCellSizeHeight);

        if (newCellSize != CELL_SIZE) {
            CELL_SIZE = newCellSize;
        }
    }

    /**
     * Sets the frame size based on maze dimensions and cell size.
     */
    private void setFrameSize() {
        Insets insets = getInsets();
        int frameWidth = mazeWidth * CELL_SIZE + insets.left + insets.right;
        int frameHeight = mazeHeight * CELL_SIZE + insets.top + insets.bottom;
        setSize(frameWidth, frameHeight);
        setLocationRelativeTo(null);
    }

    /**
     * Loads images for the player and mountain from files.
     */
    private void loadImages() {
    	try {
			// Load images from the classpath using getResourceAsStream
			InputStream playerImageStream = getClass().getResourceAsStream("/images/mario.png");
			InputStream mountainImageStream = getClass().getResourceAsStream("/images/mountain.png");
			InputStream purpleImageStream = getClass().getResourceAsStream("/images/Purple.png");

			// Check if streams are not null before creating the images
			if (playerImageStream != null && mountainImageStream != null && purpleImageStream != null) {
				playerImage = ImageIO.read(playerImageStream);
				mountainImage = ImageIO.read(mountainImageStream);
				purpleImage = ImageIO.read(purpleImageStream);
			} else {
				System.err.println("Failed to load image resources");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Sets the difficulty level and adjusts the maze size accordingly.
     *
     * @param difficultyLevel The difficulty level of the maze.
     */
    private void setDifficultyLevel(int difficultyLevel) {
        int baseWidth = 20;
        int baseHeight = 18;

        mazeWidth = baseWidth + (difficultyLevel - 1) * 5;
        mazeHeight = baseHeight + (difficultyLevel - 1) * 3;

        adjustMazeDimensionsToFitWindow();
    }

    /**
     * Generates a solvable maze with start and end points.
     */
    private void generateMaze() {
        Random random = new Random();
        boolean solvable = false;

        while (!solvable) {
            generateMazeGrid(random);
            setStartAndEndPoints(random);
            
            bfsPath = findPathWithBFS();

            if (bfsPath != null && !bfsPath.isEmpty()) {
                solvable = true;
                repaint();
            }
        }
    }

    /**
     * Generates a maze grid with random walls and paths.
     *
     * @param random Random instance for random generation.
     */
    private void generateMazeGrid(Random random) {
        mazeGrid = new int[mazeHeight][mazeWidth];
        for (int y = 0; y < mazeHeight; y++) {
            for (int x = 0; x < mazeWidth; x++) {
                mazeGrid[y][x] = random.nextBoolean() ? 1 : 0;
            }
        }
    }

    /**
     * Sets the start and end points in the maze and ensures they are open paths.
     *
     * @param random Random instance for random generation.
     */
    private void setStartAndEndPoints(Random random) {
        playerX = 1;
        playerY = random.nextInt(mazeHeight - 2) + 1;
        endX = mazeWidth - 2;
        endY = random.nextInt(mazeHeight - 2) + 1;

        mazeGrid[playerY][playerX] = 0;
        mazeGrid[endY][endX] = 0;
    }

    /**
     * Finds a path from the start to end point using breadth-first search.
     *
     * @return A list representing the BFS path.
     */
    private List<Integer> findPathWithBFS() {
        Graph graph = new Graph(mazeWidth * mazeHeight);
        int source = playerX + playerY * mazeWidth;
        int destination = endX + endY * mazeWidth;

        buildGraphFromMaze(graph);
        return getBFSPath(graph, source, destination);
    }

    /**
     * Builds a graph based on the maze grid.
     *
     * @param graph Graph to build from the maze grid.
     */
    private void buildGraphFromMaze(Graph graph) {
        for (int y = 0; y < mazeHeight; y++) {
            for (int x = 0; x < mazeWidth; x++) {
                int v = x + y * mazeWidth;
                if (mazeGrid[y][x] == 0) {
                    addAdjacentEdges(graph, v, x, y);
                }
            }
        }
    }

    /**
     * Adds edges to the graph for adjacent open cells in the maze.
     *
     * @param graph Graph to add edges to.
     * @param v Current cell index.
     * @param x Current x-coordinate.
     * @param y Current y-coordinate.
     */
    private void addAdjacentEdges(Graph graph, int v, int x, int y) {
        if (x > 0 && mazeGrid[y][x - 1] == 0) {
            graph.addEdge(v, v - 1);
        }
        if (x < mazeWidth - 1 && mazeGrid[y][x + 1] == 0) {
            graph.addEdge(v, v + 1);
        }
        if (y > 0 && mazeGrid[y - 1][x] == 0) {
            graph.addEdge(v, v - mazeWidth);
        }
        if (y < mazeHeight - 1 && mazeGrid[y + 1][x] == 0) {
            graph.addEdge(v, v + mazeWidth);
        }
    }

    /**
     * Retrieves a path using BFS from the source to the destination in the graph.
     *
     * @param graph Graph to search in.
     * @param source Source vertex index.
     * @param destination Destination vertex index.
     * @return A list representing the BFS path.
     */
    private List<Integer> getBFSPath(Graph graph, int source, int destination) {
        BreadthFirstPaths bfs = new BreadthFirstPaths(graph, source);
        if (bfs.hasPathTo(destination)) {
            Iterable<Integer> iterablePath = bfs.pathTo(destination);
            return convertIterableToList(iterablePath);
        } else {
            return null;
        }
    }

    /**
     * Converts an Iterable of integers to a list.
     *
     * @param iterablePath Iterable of integers.
     * @return A list of integers.
     */
    private List<Integer> convertIterableToList(Iterable<Integer> iterablePath) {
        Queue<Integer> pathQueue = new Queue<>();
        for (int vertex : iterablePath) {
            pathQueue.enqueue(vertex);
        }
        List<Integer> pathList = new ArrayList<>(pathQueue.size());
        while (!pathQueue.isEmpty()) {
            pathList.add(pathQueue.dequeue());
        }
        return pathList;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.translate(PADDING, PADDING);
        drawMaze(g);
        drawPlayer(g);
        drawEndPosition(g);
        if (gameCompleted) {
            drawPaths(g);
        }
    }

    /**
     * Draws the maze grid.
     *
     * @param g Graphics object to draw with.
     */
    private void drawMaze(Graphics g) {
        Set<Integer> mountainRows = new HashSet<>();
        Set<Integer> mountainColumns = new HashSet<>();

        for (int y = 0; y < mazeHeight; y++) {
            for (int x = 0; x < mazeWidth; x++) {
                if (mazeGrid[y][x] == 1) {
                    drawWall(g, x, y);
                } else {
                    drawPath(g, x, y);
                    if (shouldPlaceMountain(mountainRows, mountainColumns, x, y)) {
                        placeMountain(g, x, y, mountainRows, mountainColumns);
                    }
                }
            }
        }
    }

    /**
     * Draws a wall cell.
     *
     * @param g Graphics object to draw with.
     * @param x x-coordinate of the cell.
     * @param y y-coordinate of the cell.
     */
    private void drawWall(Graphics g, int x, int y) {
    	g.setColor(new Color(240, 245, 200));
        g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    /**
     * Draws a path cell.
     *
     * @param g Graphics object to draw with.
     * @param x x-coordinate of the cell.
     * @param y y-coordinate of the cell.
     */
    private void drawPath(Graphics g, int x, int y) {
        g.setColor(Color.WHITE);
        g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.BLACK);
        g.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    /**
     * Checks if a mountain should be placed at the given cell.
     *
     * @param mountainRows Set of rows containing mountains.
     * @param mountainColumns Set of columns containing mountains.
     * @param x x-coordinate of the cell.
     * @param y y-coordinate of the cell.
     * @return True if a mountain should be placed, false otherwise.
     */
    private boolean shouldPlaceMountain(Set<Integer> mountainRows, Set<Integer> mountainColumns, int x, int y) {
        return !mountainRows.contains(y) && !mountainColumns.contains(x);
    }

    /**
     * Places a mountain image at the given cell.
     *
     * @param g Graphics object to draw with.
     * @param x x-coordinate of the cell.
     * @param y y-coordinate of the cell.
     * @param mountainRows Set of rows containing mountains.
     * @param mountainColumns Set of columns containing mountains.
     */
    private void placeMountain(Graphics g, int x, int y, Set<Integer> mountainRows, Set<Integer> mountainColumns) {
        g.drawImage(mountainImage, x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE, null);
        mazeGrid[y][x] = 2;
        mountainRows.add(y);
        mountainColumns.add(x);
    }

    /**
     * Draws the player at their current position.
     *
     * @param g Graphics object to draw with.
     */
    private void drawPlayer(Graphics g) {
        if (playerImage != null) {
            g.drawImage(playerImage, playerX * CELL_SIZE, playerY * CELL_SIZE, CELL_SIZE, CELL_SIZE, null);
        }
    } 

    /**
     * Draws the end position as a red circle.
     *
     * @param g Graphics object to draw with.
     */
    private void drawEndPosition(Graphics g) {
    	if (purpleImage != null) {
            g.drawImage(purpleImage, endX * CELL_SIZE, endY * CELL_SIZE, CELL_SIZE, CELL_SIZE, null);
    	}
    }

    /**
     * Draws the paths if the game is completed.
     *
     * @param g Graphics object to draw with.
     */
    private void drawPaths(Graphics g) {
        Set<Integer> bfsPathCells = drawBFSPath(g);
        drawPlayerPath(g, bfsPathCells);
    }

    /**
     * Draws the BFS path in blue and returns a set of cells on the BFS path.
     *
     * @param g Graphics object to draw with.
     * @return Set of integers representing cells on the BFS path.
     */
    private Set<Integer> drawBFSPath(Graphics g) {
        Set<Integer> bfsPathCells = new HashSet<>();
        Color bfsPathColor = new Color(0, 0, 255, 100); // Blue with transparency
        g.setColor(bfsPathColor);
        for (int p : bfsPath) {
            int px = p % mazeWidth;
            int py = p / mazeWidth;
            g.fillRect(px * CELL_SIZE, py * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            bfsPathCells.add(p);
        }
        return bfsPathCells;
    }

    /**
     * Draws the player's path in green, excluding cells covered by the BFS path.
     *
     * @param g Graphics object to draw with.
     * @param bfsPathCells Set of cells on the BFS path.
     */
    private void drawPlayerPath(Graphics g, Set<Integer> bfsPathCells) {
        Color playerPathColor = new Color(0, 255, 0, 100); // Green with transparency
        g.setColor(playerPathColor);
        for (int p : playerPath) {
            if (!bfsPathCells.contains(p)) {
                int px = p % mazeWidth;
                int py = p / mazeWidth;
                g.fillRect(px * CELL_SIZE, py * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_SPACE) {
            handleSpacePress();
        } else if (spacePressed) {
            handleJump(keyCode);
        } else {
            handleMovement(keyCode);
        }
    }

    /**
     * Handles space bar press.
     */
    private void handleSpacePress() {
        spacePressed = true;
    }

    /**
     * Handles jump when space bar is pressed.
     *
     * @param keyCode Key code representing the arrow key pressed.
     */
    private void handleJump(int keyCode) {
        spacePressed = false;
        switch (keyCode) {
            case KeyEvent.VK_UP:
                jumpPlayer(0, -1);
                break;
            case KeyEvent.VK_DOWN:
                jumpPlayer(0, 1);
                break;
            case KeyEvent.VK_LEFT:
                jumpPlayer(-1, 0);
                break;
            case KeyEvent.VK_RIGHT:
                jumpPlayer(1, 0);
                break;
        }
    }

    /**
     * Handles player movement using arrow keys.
     *
     * @param keyCode Key code representing the arrow key pressed.
     */
    private void handleMovement(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                movePlayer(0, -1);
                break;
            case KeyEvent.VK_DOWN:
                movePlayer(0, 1);
                break;
            case KeyEvent.VK_LEFT:
                movePlayer(-1, 0);
                break;
            case KeyEvent.VK_RIGHT:
                movePlayer(1, 0);
                break;
        }
    }

    /**
     * Allows the player to jump in the specified direction if the jump is allowed.
     *
     * @param dx Horizontal jump distance.
     * @param dy Vertical jump distance.
     */
    private void jumpPlayer(int dx, int dy) {
        int jumpDistance = 1;
        int newX = playerX + dx * jumpDistance;
        int newY = playerY + dy * jumpDistance;
        if (isValidJump(newX, newY)) {
        	updatePlayerPosition(newX, newY);
            checkEndGame();
        }
    }
    

    /**
     * Checks if the jump is valid.
     *
     * @param newX New x-coordinate.
     * @param newY New y-coordinate.
     * @return True if the jump is valid, false otherwise.
     */
    private boolean isValidJump(int newX, int newY) {
        boolean jumpFromOrToMountain = checkMountainPosition(playerX, playerY) || checkMountainPosition(newX, newY);
        return jumpFromOrToMountain && isValidMove(newX, newY);
    }

    /**
     * Checks if the specified position contains a mountain.
     *
     * @param x x-coordinate.
     * @param y y-coordinate.
     * @return True if the position contains a mountain, false otherwise.
     */
    private boolean checkMountainPosition(int x, int y) {
        if (x < 0 || x >= mazeWidth || y < 0 || y >= mazeHeight) {
            return false;
        }
        return mazeGrid[y][x] == 2;
    }

    /**
     * Moves the player in the specified direction.
     *
     * @param dx Horizontal movement.
     * @param dy Vertical movement.
     */
    private void movePlayer(int dx, int dy) {
        int newX = playerX + dx;
        int newY = playerY + dy;

        if (checkMountainPosition(newX, newY)) {
            return; // Exit the method to prevent the move
        } else if (isValidMove(newX, newY)) {
            updatePlayerPosition(newX, newY);
            checkEndGame();
        }
    }

    /**
     * Updates the player's position.
     *
     * @param newX New x-coordinate.
     * @param newY New y-coordinate.
     */
    private void updatePlayerPosition(int newX, int newY) {
        playerX = newX;
        playerY = newY;
        playerPath.add(playerY * mazeWidth + playerX);
        repaint();
    }
    
    /**
     * Checks if the player has reached the end position.
     */
    private void checkEndGame() {
        if (playerX == endX && playerY == endY) {
            gameOver();
        }
    }

    /**
     * Handles the game over logic when the player completes the maze.
     * 
     * Calculates the player's efficiency score based on the ratio of the BFS path length to the player's path length, 
     * displays the  player's final score and a message indicating the game is over,
     * displays the comparison between the player's path and the BFS path, 
     * and transitions the player back to the main menu.
    **/
    private void gameOver() {
        int bfsPathLength = bfsPath.size();
        int playerPathLength = playerPath.size(); 

        double efficiencyPercentage = (playerPathLength != 0) ? ((double) bfsPathLength / playerPathLength) * 100 : 0;
        int finalScore = Math.round((float) efficiencyPercentage);
        
        String message = "Congratulations!\nYou completed the maze!\nScore: " + finalScore + "/100";
        JOptionPane.showMessageDialog(this, message);

        gameCompleted = true;
        
        repaint();
        adjustMazeDimensionsToFitWindow();
        setFrameSize();
        
        String message2 = "Here's the comparison between your path (green) and the BSF path. \nClick ok to go back to menu.";
        JOptionPane.showMessageDialog(this, message2);
        
        dispose();
        
        new GameMenu();
    }

    /**
     * Determines whether the move to the specified position is valid.
     *
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     * @return True if the move is valid (within maze bounds and not a wall), false otherwise.
     */
    private boolean isValidMove(int x, int y) {
        // Check if the position is within the maze bounds
        if (x < 0 || x >= mazeWidth || y < 0 || y >= mazeHeight) {
            return false;
        }
        return mazeGrid[y][x] != 1;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No need to implement
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // No need to implement
    }
}