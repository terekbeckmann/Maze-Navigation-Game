package teamMazeGame;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This menu allows the user to choose a difficulty level and start the game.
 * The user can also read instructions on how to play the game.
 * 
 * Upon starting the game, the menu window will be closed.
 * 
 * @author Sahak I.
 * @author Terek Beckmann
 */
public class GameMenu extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JComboBox<String> difficultyComboBox;
	private JButton startButton;
	private final String[] difficultyLevels = { "1 (Easiest)", "2", "3", "4", "5", "6", "7 (Hardest)" };

	/**
	 * Constructs a new GameMenu instance and initializes the GUI components.
	 */
	public GameMenu() {
		setTitle("Maze Game");
		setSize(400, 350);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		JPanel panel = new JPanel();
		panel.setBackground(new Color(200, 200, 235));
		panel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new java.awt.Insets(10, 20, 10, 20);

		// Title label
		JLabel titleLabel = new JLabel("Welcome to the Maze Game!", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(Color.BLACK);
		gbc.gridy++;
		panel.add(titleLabel, gbc);

		// Instructions
		JLabel instructionLabel1 = new JLabel("Use the arrow keys to move around.");
		JLabel instructionLabel2 = new JLabel("Use (space bar + arrow key) to jump onto mountains.");
		gbc.gridy++;
		panel.add(instructionLabel1, gbc);
		gbc.gridy++;
		panel.add(instructionLabel2, gbc);

		// Difficulty ComboBox
		difficultyComboBox = new JComboBox<>(difficultyLevels);
		gbc.gridy++;
		panel.add(difficultyComboBox, gbc);

		// Start button
		startButton = new JButton("Start Game");
		startButton.setBackground(Color.WHITE);
		startButton.setForeground(Color.BLACK);
		startButton.addActionListener(this);
		gbc.gridy++;
		panel.add(startButton, gbc);

		add(panel);
		setVisible(true);
	}

	/**
	 * Handles button click events.
	 *
	 * @param e The event representing the action.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startButton) {
			String selectedDifficulty = (String) difficultyComboBox.getSelectedItem();
			int difficultyLevel = Integer.parseInt(selectedDifficulty.split(" ")[0]);
			new MazeGame(difficultyLevel);
			dispose(); // Close the menu window
		}
	}
}