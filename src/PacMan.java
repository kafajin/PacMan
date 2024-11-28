import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {

    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; // up down left right
    Random random = new Random();

    private int score = 0;
    private int lives = 3; // Initial lives

    public PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight + 50)); // Extra space for score/lives display
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // Load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }

        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char tileMapChar = tileMap[r].charAt(c);

                int x = c * tileSize;
                int y = r * tileSize;

                switch (tileMapChar) {
                    case 'X' -> walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                    case 'b' -> ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
                    case 'o' -> ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize));
                    case 'p' -> ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
                    case 'r' -> ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
                    case 'P' -> pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                    case ' ' -> foods.add(new Block(null, x + 14, y + 14, 4, 4));
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.white);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        // Display score and lives
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, boardHeight + 30);
        g.drawString("Lives: " + lives, boardWidth - 100, boardHeight + 30);
    }

    public void move() {
        pacman.x += pacman.veloCityX;
        pacman.y += pacman.veloCityY;

        // Check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.veloCityX;
                pacman.y -= pacman.veloCityY;
                break;
            }
        }

        // Check food collisions
        foods.removeIf(food -> {
            if (collision(pacman, food)) {
                score += 10; // Increment score for eating food
                return true;
            }
            return false;
        });

        // Move ghosts
        for (Block ghost : ghosts) {
            ghost.x += ghost.veloCityX;
            ghost.y += ghost.veloCityY;

            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x < 0 || ghost.x + ghost.width > boardWidth
                        || ghost.y < 0 || ghost.y + ghost.height > boardHeight) {
                    ghost.x -= ghost.veloCityX;
                    ghost.y -= ghost.veloCityY;
                    ghost.updateDirection(directions[random.nextInt(4)]);
                }
            }

            // Check Pac-Man collision with ghosts
            if (collision(pacman, ghost)) {
                lives--;
                pacman.x = pacman.startX;
                pacman.y = pacman.startY;

                if (lives <= 0) {
                    JOptionPane.showMessageDialog(this, "Game Over! Final Score: " + score, "Pac-Man", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0); // End the game
                }
            }
        }
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x &&
                a.y < b.y + b.height && a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> pacman.updateDirection('U');
            case KeyEvent.VK_DOWN -> pacman.updateDirection('D');
            case KeyEvent.VK_LEFT -> pacman.updateDirection('L');
            case KeyEvent.VK_RIGHT -> pacman.updateDirection('R');
        }

        switch (pacman.direction) {
            case 'U' -> pacman.image = pacmanUpImage;
            case 'D' -> pacman.image = pacmanDownImage;
            case 'L' -> pacman.image = pacmanLeftImage;
            case 'R' -> pacman.image = pacmanRightImage;
        }
    }

    class Block {
        int x, y, width, height;
        Image image;
        int startX, startY;
        char direction = 'U'; // U D L R
        int veloCityX = 0, veloCityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            this.direction = direction;
            updateVelocity();
        }

        void updateVelocity() {
            switch (direction) {
                case 'U' -> { veloCityX = 0; veloCityY = -tileSize / 4; }
                case 'D' -> { veloCityX = 0; veloCityY = tileSize / 4; }
                case 'L' -> { veloCityX = -tileSize / 4; veloCityY = 0; }
                case 'R' -> { veloCityX = tileSize / 4; veloCityY = 0; }
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pac-Man");
        PacMan pacManPanel = new PacMan();
        frame.add(pacManPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
