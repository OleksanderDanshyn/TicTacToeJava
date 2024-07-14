import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class Main {
    static final int BOARD_SIZE = 3;
    static final int SMALL_BOARD_SIZE = 3;
    static JButton[][][] buttons = new JButton[BOARD_SIZE][BOARD_SIZE][SMALL_BOARD_SIZE * SMALL_BOARD_SIZE];
    static boolean isPlayerXTurn = true;
    static ImageIcon xIcon;
    static ImageIcon oIcon;
    static JFrame frame;
    static int[][] smallBoardWinners = new int[BOARD_SIZE][BOARD_SIZE]; // 0: no winner, 1: X wins, 2: O wins
    static JPanel[][] smallBoards = new JPanel[BOARD_SIZE][BOARD_SIZE];
    static boolean unrestrictedMove = true;
    static int nextBigRow, nextBigCol;
    static JLabel turnLabel;
    static JPanel boardStatePanel;

    public static void main(String[] args) {
        frame = new JFrame("Ultimate Tic Tac Toe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setLayout(new BorderLayout());

        // who makes a move
        JPanel topPanel = new JPanel(new BorderLayout());
        turnLabel = new JLabel("Turn: X");
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(turnLabel, BorderLayout.WEST);

        //last turn
        boardStatePanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        topPanel.add(boardStatePanel, BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        frame.add(mainPanel, BorderLayout.CENTER);

        // Load images
        try {
            Image xImage = ImageIO.read(new File("Red_X.png"));
            Image oImage = ImageIO.read(new File("Blue_O.png"));

            if (xImage == null || oImage == null) {
                throw new IOException("One or both images could not be loaded.");
            }

            // Don't appear without it
            xIcon = new ImageIcon(xImage.getScaledInstance(30, 30, Image.SCALE_SMOOTH));
            oIcon = new ImageIcon(oImage.getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Create panels for each small board by iterating over size
        for (int bigRow = 0; bigRow < BOARD_SIZE; bigRow++) {
            for (int bigCol = 0; bigCol < BOARD_SIZE; bigCol++) {
                JPanel smallBoard = new JPanel();
                smallBoard.setLayout(new GridLayout(SMALL_BOARD_SIZE, SMALL_BOARD_SIZE));
                smallBoard.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                smallBoards[bigRow][bigCol] = smallBoard;
                for (int i = 0; i < SMALL_BOARD_SIZE * SMALL_BOARD_SIZE; i++) {
                    buttons[bigRow][bigCol][i] = new JButton();
                    buttons[bigRow][bigCol][i].setFocusPainted(false);
                    buttons[bigRow][bigCol][i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    final int index = i;
                    final int finalBigRow = bigRow;
                    final int finalBigCol = bigCol;

                    // mouse interaction
                    buttons[bigRow][bigCol][i].addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (unrestrictedMove || (finalBigRow == nextBigRow && finalBigCol == nextBigCol)) {
                                JButton buttonClicked = (JButton) e.getSource();
                                if (buttonClicked.getIcon() == null) {
                                    updateBoardStatePanel();
                                    if (isPlayerXTurn) {
                                        buttonClicked.setIcon(xIcon);
                                    } else {
                                        buttonClicked.setIcon(oIcon);
                                    }
                                    buttonClicked.setEnabled(false);
                                    buttonClicked.revalidate();
                                    buttonClicked.repaint();
                                    checkForSmallBoardWinner(finalBigRow, finalBigCol);
                                    checkForOverallWinner();
                                    isPlayerXTurn = !isPlayerXTurn;
                                    updateTurnLabel();
                                    setNextMove(index);
                                }
                            }
                        }
                    });
                    smallBoard.add(buttons[bigRow][bigCol][i]);
                }
                mainPanel.add(smallBoard);
            }
        }

        frame.setVisible(true);
    }

    //got idea from here, https://codewithcurious.com/projects/tic-tac-toe-game-using-java-swing/, changed for ultimate
    static void checkForSmallBoardWinner(int bigRow, int bigCol) {
        for (int i = 0; i < SMALL_BOARD_SIZE * SMALL_BOARD_SIZE; i += SMALL_BOARD_SIZE) {
            if      (buttons[bigRow][bigCol][i].getIcon() != null &&
                    buttons[bigRow][bigCol][i].getIcon().equals(buttons[bigRow][bigCol][i + 1].getIcon()) &&
                    buttons[bigRow][bigCol][i].getIcon().equals(buttons[bigRow][bigCol][i + 2].getIcon())) {
                declareSmallBoardWinner(bigRow, bigCol, buttons[bigRow][bigCol][i].getIcon());
            }
        }

        for (int i = 0; i < SMALL_BOARD_SIZE; i++) {
            if      (buttons[bigRow][bigCol][i].getIcon() != null &&
                    buttons[bigRow][bigCol][i].getIcon().equals(buttons[bigRow][bigCol][i + 3].getIcon()) &&
                    buttons[bigRow][bigCol][i].getIcon().equals(buttons[bigRow][bigCol][i + 6].getIcon())) {
                declareSmallBoardWinner(bigRow, bigCol, buttons[bigRow][bigCol][i].getIcon());
            }
        }

        if      (buttons[bigRow][bigCol][0].getIcon() != null &&
                buttons[bigRow][bigCol][0].getIcon().equals(buttons[bigRow][bigCol][4].getIcon()) &&
                buttons[bigRow][bigCol][0].getIcon().equals(buttons[bigRow][bigCol][8].getIcon())) {
            declareSmallBoardWinner(bigRow, bigCol, buttons[bigRow][bigCol][0].getIcon());
        }

        if      (buttons[bigRow][bigCol][2].getIcon() != null &&
                buttons[bigRow][bigCol][2].getIcon().equals(buttons[bigRow][bigCol][4].getIcon()) &&
                buttons[bigRow][bigCol][2].getIcon().equals(buttons[bigRow][bigCol][6].getIcon())) {
            declareSmallBoardWinner(bigRow, bigCol, buttons[bigRow][bigCol][2].getIcon());
        }
    }

    static void declareSmallBoardWinner(int bigRow, int bigCol, Icon icon) {
        for (int i = 0; i < SMALL_BOARD_SIZE * SMALL_BOARD_SIZE; i++) {
            buttons[bigRow][bigCol][i].setEnabled(false);
        }
        smallBoardWinners[bigRow][bigCol] = (icon.equals(xIcon) ? 1 : 2);

        //cover the entire small board with the winner's icon
        JLabel winnerLabel = new JLabel(icon);
        winnerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        winnerLabel.setVerticalAlignment(SwingConstants.CENTER);
        smallBoards[bigRow][bigCol].removeAll();
        smallBoards[bigRow][bigCol].setLayout(new BorderLayout());
        smallBoards[bigRow][bigCol].add(winnerLabel, BorderLayout.CENTER);
        smallBoards[bigRow][bigCol].revalidate();
        smallBoards[bigRow][bigCol].repaint();
    }

    static void checkForOverallWinner() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if      (smallBoardWinners[i][0] != 0 &&
                    smallBoardWinners[i][0] == smallBoardWinners[i][1] &&
                    smallBoardWinners[i][0] == smallBoardWinners[i][2]) {
                declareOverallWinner(smallBoardWinners[i][0]);
            }
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            if      (smallBoardWinners[0][i] != 0 &&
                    smallBoardWinners[0][i] == smallBoardWinners[1][i] &&
                    smallBoardWinners[0][i] == smallBoardWinners[2][i]) {
                declareOverallWinner(smallBoardWinners[0][i]);
            }
        }

        if      (smallBoardWinners[0][0] != 0 &&
                smallBoardWinners[0][0] == smallBoardWinners[1][1] &&
                smallBoardWinners[0][0] == smallBoardWinners[2][2]) {
            declareOverallWinner(smallBoardWinners[0][0]);
        }

        if      (smallBoardWinners[0][2] != 0 &&
                smallBoardWinners[0][2] == smallBoardWinners[1][1] &&
                smallBoardWinners[0][2] == smallBoardWinners[2][0]) {
            declareOverallWinner(smallBoardWinners[0][2]);
        }
    }

    static void declareOverallWinner(int winner) {
        JOptionPane.showMessageDialog(frame, (winner == 1 ? "X" : "O") + " wins the game!");
        resetGame();
    }

    //reset everything after wining
    static void resetGame() {
        for (int bigRow = 0; bigRow < BOARD_SIZE; bigRow++) {
            for (int bigCol = 0; bigCol < BOARD_SIZE; bigCol++) {
                smallBoards[bigRow][bigCol].removeAll();
                smallBoards[bigRow][bigCol].setLayout(new GridLayout(SMALL_BOARD_SIZE, SMALL_BOARD_SIZE));
                for (int i = 0; i < SMALL_BOARD_SIZE * SMALL_BOARD_SIZE; i++) {
                    buttons[bigRow][bigCol][i].setIcon(null);
                    buttons[bigRow][bigCol][i].setEnabled(true);
                    buttons[bigRow][bigCol][i].setBackground(null);
                    smallBoards[bigRow][bigCol].add(buttons[bigRow][bigCol][i]);
                }
                smallBoardWinners[bigRow][bigCol] = 0;
            }
        }
        isPlayerXTurn = true;
        unrestrictedMove = true;
        updateTurnLabel();
        updateBoardStatePanel();
        frame.revalidate();
        frame.repaint();
    }

    static void setNextMove(int lastMoveIndex) {
        nextBigRow = lastMoveIndex / SMALL_BOARD_SIZE;
        nextBigCol = lastMoveIndex % SMALL_BOARD_SIZE;
        unrestrictedMove = false;
        if (smallBoardWinners[nextBigRow][nextBigCol] != 0) {
            unrestrictedMove = true;
        }

        //green border where you can move
        for (int bigRow = 0; bigRow < BOARD_SIZE; bigRow++) {
            for (int bigCol = 0; bigCol < BOARD_SIZE; bigCol++) {
                if (unrestrictedMove || (bigRow == nextBigRow && bigCol == nextBigCol)) {
                    smallBoards[bigRow][bigCol].setBorder(BorderFactory.createLineBorder(Color.GREEN, 5));
                } else {
                    smallBoards[bigRow][bigCol].setBorder(BorderFactory.createEmptyBorder());
                }
            }
        }
    }

    //change whose turn it is
    static void updateTurnLabel() {
        turnLabel.setText("Turn: " + (isPlayerXTurn ? "X" : "O"));
    }

    //last turn
    static void updateBoardStatePanel() {
        boardStatePanel.removeAll();
        for (int bigRow = 0; bigRow < BOARD_SIZE; bigRow++) {
            for (int bigCol = 0; bigCol < BOARD_SIZE; bigCol++) {
                JPanel smallStatePanel = new JPanel(new GridLayout(SMALL_BOARD_SIZE, SMALL_BOARD_SIZE));
                for (int i = 0; i < SMALL_BOARD_SIZE * SMALL_BOARD_SIZE; i++) {
                    JLabel label = new JLabel();
                    Icon icon = buttons[bigRow][bigCol][i].getIcon();
                    label.setIcon(icon);
                    smallStatePanel.add(label);
                }
                smallStatePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                boardStatePanel.add(smallStatePanel);
            }
        }
        boardStatePanel.revalidate();
        boardStatePanel.repaint();
    }
}