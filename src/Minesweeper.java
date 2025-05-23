import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class Minesweeper extends JFrame {
    private static final int ROWS = 16;
    private static final int COLS = 30;
    private static final int MINES = 99;

    private final Tile[][] board = new Tile[ROWS][COLS];
    private boolean firstClick = true;
    private boolean gameOver = false;
    private int tilesRevealed = 0;
    private int flagsPlaced = 0;
    private final JLabel statusLabel;
    private final JPanel boardPanel;

    public Minesweeper() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("Muselmänner: " + MINES);
        add(statusLabel, BorderLayout.NORTH);

        boardPanel = new JPanel(new GridLayout(ROWS, COLS));
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Tile tile = new Tile(r, c);
                board[r][c] = tile;
                boardPanel.add(tile);
            }
        }
        add(boardPanel, BorderLayout.CENTER);

        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void placeMines(int safeRow, int safeCol) {
        List<Point> positions = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1) continue;
                positions.add(new Point(r, c));
            }
        }
        Collections.shuffle(positions);
        for (int i = 0; i < MINES; i++) {
            Point p = positions.get(i);
            board[p.x][p.y].isMine = true;
        }
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c].adjacentMines = countAdjacentMines(r, c);
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr, nc = col + dc;
                if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) continue;
                if (board[nr][nc].isMine) count++;
            }
        }
        return count;
    }

    private void revealTile(int row, int col) {
        Tile tile = board[row][col];
        if (tile.isRevealed || tile.isFlagged) return;
        tile.reveal();
        tilesRevealed++;
        if (tile.isMine) {
            gameOver(false);
            return;
        }
        if (tile.adjacentMines == 0) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = row + dr, nc = col + dc;
                    if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) continue;
                    if (!(dr == 0 && dc == 0)) {
                        revealTile(nr, nc);
                    }
                }
            }
        }
        if (tilesRevealed == ROWS * COLS - MINES) {
            gameOver(true);
        }
    }

    private void gameOver(boolean win) {
        gameOver = true;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Tile t = board[r][c];
                if (t.isMine) t.setText("💣");
                t.setEnabled(false);
            }
        }
        if (win) {
            statusLabel.setText("yayy! :)");
        } else {
            statusLabel.setText("womp womp :(");
        }
    }

    private class Tile extends JButton {
        int row, col;
        boolean isMine = false;
        boolean isRevealed = false;
        boolean isFlagged = false;
        int adjacentMines = 0;

        public Tile(int row, int col) {
            this.row = row;
            this.col = col;
            setPreferredSize(new Dimension(25, 25));
            setMargin(new Insets(0,0,0,0));
            setFont(new Font("Monospaced", Font.BOLD, 14));
            setFocusPainted(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (gameOver) return;
                    if (SwingUtilities.isRightMouseButton(e)) {
                        toggleFlag();
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (isFlagged) return;
                        if (firstClick) {
                            placeMines(row, col);
                            firstClick = false;
                        }
                        if (!isRevealed) {
                            revealTile(row, col); // Correctly uses the outer method
                        }
                    }
                }
            });
        }

        public void reveal() {
            if (isRevealed) return;
            isRevealed = true;
            setEnabled(false);
            if (isMine) {
                setText("💣");
                setBackground(Color.RED);
            } else if (adjacentMines > 0) {
                setText(Integer.toString(adjacentMines));
                setForeground(getColorForNumber(adjacentMines));
            } else {
                setText("");
                setBackground(Color.LIGHT_GRAY);
            }
        }

        public void toggleFlag() {
            if (isRevealed) return;
            if (!isFlagged && flagsPlaced >= MINES) return;
            isFlagged = !isFlagged;
            setText(isFlagged ? "🚩" : "");
            flagsPlaced += isFlagged ? 1 : -1;
            statusLabel.setText("Muselmänner: " + (MINES - flagsPlaced));
        }

        private Color getColorForNumber(int n) {
            switch (n) {
                case 1: return Color.BLUE;
                case 2: return new Color(0,128,0);
                case 3: return Color.RED;
                case 4: return new Color(0,0,128);
                case 5: return new Color(128,0,0);
                case 6: return new Color(64,224,208);
                case 7: return Color.BLACK;
                case 8: return Color.GRAY;
                default: return Color.BLACK;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Minesweeper::new);
    }
}
