import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Container;
import java.awt.Font;

import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Manages all the UI buttons and user interactions and contains the nested event classes 
 * for those buttons. Each event calls a corresponding method in the currentGameMode.
 * For example, when the new game button is pressed it calls currentGameMode.newGame().
 * @author Emily
 */
public class UI {

	/**
     *Contains location information for setting the UI bar.
     */
    public static enum Direction
	{
		LEFT, RIGHT, UP, DOWN;
	}

    private Direction _Direction = Direction.UP;

	 /**
     * The Frame the program takes place in. Set in constructor.
     */
    private JFrame frame;
	/**
     *The panel cards and other graphical components are added to. Set in constructor.
     */
	protected JPanel table;

	 /**
     * Reference to the currently running game mode.
     */
    private GameMode currentGameMode;

	// GUI components
    private  JEditorPane gameTitle = new JEditorPane("text/html", "");
    private JButton showRulesButton = new JButton("Show Rules");
	private  JButton newGameButton = new JButton("New Game");
	private  JButton mainMenuButton = new JButton("Main Menu");
	private  JButton toggleTimerButton = new JButton("Pause Timer");
	private  JButton saveButton = new JButton("Save");
	private  JButton loadButton = new JButton("Load");
	private  JButton optionsButton = new JButton("Options");
	private  JTextField scoreBox = new JTextField();// displays the score
	private  JTextField timeBox = new JTextField();// displays the time
	private  JTextField statusBox = new JTextField();// status messages
	private JButton redoButton = new JButton("Redo");
	private JButton undoButton = new JButton("Undo");
	private JButton hintButton = new JButton("Hint");

	private ScoreClock scoreClock;
	private boolean timeRunning = false;

	/**
     *The constructor for the UI class.
     * @param myTable Used to set the table variable.
     * @param myFrame Used to set the frame variable.
     */
    public UI(JPanel myTable, JFrame myFrame) {
        table = myTable;
        frame = myFrame;
    }

	/**
     * Sets the currentGameMode. Called from Platform.
     * @param myGameMode Used to set the gameMode variable.
     */
    public void setGameMode(GameMode myGameMode) {
        currentGameMode = myGameMode;
    }

    // BUTTON LISTENERS
	/**
     * Nested class that implements ActionListener(When the button is pressed 
     * it calls the newGame() method in the currentGameMode). 
     */
	private class NewGameListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
            table.removeAll();
            addButtons();
			currentGameMode.newGame();
		}

	}

	/**
     * Nested class that implements ActionListener. When the button is pressed 
     * it prompts the user to make sure they want to end the current game and then calls the returnToPlatform() method in the currentGameMode
     */
	private class MainMenuListener implements ActionListener
	{
		private JFrame confirmFrame = new JFrame("Return To Menu");
		private JPanel confirmTable = new JPanel();
		private JEditorPane confirmText = new JEditorPane();
		private JButton confirmButton = new JButton("Confirm");
		private ActionListener myConfirm = new confirmMenuListener();

		/**
		 * creates the are you sure frame and its GUI components
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			confirmTable.removeAll();
			Container contentPane;


			confirmTable.setLayout(null);

			contentPane = confirmFrame.getContentPane();
			contentPane.add(confirmTable);

			confirmFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			confirmFrame.setSize(400, 200);

			confirmText.setText("Are you sure you would like to end the current game?");
			confirmText.setFont(new Font("Arial", Font.BOLD, 15));
			confirmText.setEditable(false);
			confirmText.setOpaque(false);
			confirmText.setBounds(5, 45, 400, 60);

			
			confirmButton.setBounds(125, 80, 130, 30);
			confirmButton.removeActionListener(myConfirm);
			confirmButton.addActionListener(myConfirm);
			confirmButton.setEnabled(true);

			confirmTable.add(confirmButton);
			confirmTable.add(confirmText);

			confirmTable.setVisible(true);
			confirmFrame.setVisible(true);
		}

		/**
		 * Nested class that implements ActionListener. When the confirm button is pressed it calls the returnToPlatform() method in the currentGameMode
		 */
		private class confirmMenuListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentGameMode.returnToPlatform();
				confirmFrame.dispose();
			}
		}
	}

	 /**
     * Nested class that implements ActionListener(When the button is pressed 
     * it calls the saveGame() method in the currentGameMode). 
     */
	private class SaveGameListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			currentGameMode.saveGame();
		}
	}

	/**
     * Nested class that implements ActionListener(When the button is pressed 
     * it calls the loadGame() method in the currentGameMode). 
     */
	private class LoadGameListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			currentGameMode.loadGame();
		}
	}

	/**
	 * Nested class that implements ActionListener(When the button is pressed 
     * it adjust the button text and toggles the timer on or off)
	 */
    private class ToggleTimerListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
            if (!timeRunning)
			{
				toggleTimerButton.setText("Pause Timer");
			} else
			{
				toggleTimerButton.setText("Start Timer");
			}
			toggleTimer();
		}
	}

	/**
     * Updates the timer in the currentGameMode on a set interval and updates the UI components to reflect the change.
     */
	protected void updateTimer()
	{
		try {
			currentGameMode.updateTimer();
			String text = "Seconds: " + currentGameMode.time;
			String newScore = "Score: " + currentGameMode.score;
			scoreBox.setText(newScore);
			scoreBox.repaint();
			timeBox.setText(text);
			timeBox.repaint();
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
	}

	/**
     * Starts a new timer the updates on a fixed interval.
     */
	protected void startTimer()
	{
		scoreClock = new ScoreClock();
		
		// set the timer to update every second
		timeRunning = true;
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(scoreClock, 1000, 1000);
	}

	/**
     * Toggles the current timer on or off depending on its current state.
     */
	protected void toggleTimer()
	{
		if (timeRunning && scoreClock != null)
		{
			scoreClock.cancel();
			timeRunning = false;
		} else
		{
			startTimer();
		}
	}

	/**
     * Stops the current timer and resets the UI components to reflect this change.
     */
	protected void stopTimer()
	{
		scoreClock.cancel();
		String text = "Seconds: 0";
		String newScore = "Score: 0";
		toggleTimerButton.setText("Pause Timer");
		scoreBox.setText(newScore);
		scoreBox.repaint();
		timeBox.setText(text);
		timeBox.repaint();
	}

	/**
	 * Starts the timer and tells it to update at a set interval
	 */
	private class ScoreClock extends TimerTask
	{
		@Override
		public void run()
		{
			updateTimer();
		}
	}

	/**
     * Nested class that implements ActionListener(When the button is pressed 
     * it opens a window containing currentGameMode.getRules() ). 
     */
	private class ShowRulesListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JDialog ruleFrame = new JDialog(frame, true);
			ruleFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			ruleFrame.setSize(FlowerBed.TABLE_HEIGHT, FlowerBed.TABLE_WIDTH);
			JEditorPane rulesTextPane = new JEditorPane("text/html", "");
			rulesTextPane.setEditable(false);
			String rulesText = currentGameMode.getRules();
			rulesTextPane.setText(rulesText);
			JScrollPane scroll = new JScrollPane(rulesTextPane);
			ruleFrame.add(scroll);

			ruleFrame.setVisible(true);
		}
	}

	/**
	 * Nested class that implements ActionListener for the options button. Creates a new frame with several options for the user to select between and once they hit
	 *  confirm updates the platform based on those options.
	 */
    private class optionsListener implements ActionListener
	{
		private JFrame ruleFrame = new JFrame("OPTIONS");
		private JPanel ruleTable = new JPanel();
		private JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL, Platform.volumeMin, Platform.volumeMax, Platform.volumeStart);
		private JSlider musicVolumeSlider = new JSlider(JSlider.HORIZONTAL, Platform.volumeMin, Platform.volumeMax, Platform.volumeStart);
		private JEditorPane volumeText = new JEditorPane();
		private JEditorPane musicVolumeText = new JEditorPane();
		private JButton confirmButton = new JButton("Confirm");
		private ActionListener myConfirmListener = new confirmOptionsListener();
		private JEditorPane dropDownText = new JEditorPane();
		private String[] dropDownOptions = { "Top", "Bottom" };
		private JComboBox<String> dropDown = new JComboBox<String>(dropDownOptions);
		private JEditorPane cardDropText = new JEditorPane();
		private String[] cardDropDownOptions = { "Ocean", "Original" };
		private JComboBox<String> cardDropDown = new JComboBox<String>(cardDropDownOptions);
		private JEditorPane backgroundDropText = new JEditorPane();
		private String[] backgroundDropDownOptions = { "Green", "White", "Red", "Blue", "Brown-Color Blind", "Dark Purple-Color Blind", "light Purple-Color Blind", "Dark Blue-Color Blind" };
		private JComboBox<String> backgroundDropDown = new JComboBox<String>(backgroundDropDownOptions);
		private JEditorPane musicDropText = new JEditorPane();
		private String[] musicDropDownOptions = { "Tavern", "Arcade", "Pink Floyd" };
		private JComboBox<String> musicDropDown = new JComboBox<String>(musicDropDownOptions);
		private JEditorPane autoMoveText = new JEditorPane();
		private JCheckBox checkBox;
		private ActionListener myCheckListener = new CheckListener();
		private boolean checked;

		/**
		 * Creates all the GUI components for the options menu frame
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			ruleTable.removeAll();
			Container contentPane;


			ruleTable.setLayout(null);

			contentPane = ruleFrame.getContentPane();
			contentPane.add(ruleTable);

			ruleFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			ruleFrame.setSize(400, 500);

			volumeText.setText("Effects Volume: ");
			volumeText.setFont(new Font("Arial", Font.BOLD, 15));
			volumeText.setEditable(false);
			volumeText.setOpaque(false);
			volumeText.setBounds(5, 45, 120, 60);
			
			volumeSlider.setName("volumeSlider");
			volumeSlider.addChangeListener(new SliderListener());
			volumeSlider.setMajorTickSpacing(10);
			volumeSlider.setBounds(115, 50, 260, 30);
			volumeSlider.setPaintTicks(true);
			volumeSlider.setOpaque(false);

			musicVolumeText.setText("Music Volume: ");
			musicVolumeText.setFont(new Font("Arial", Font.BOLD, 15));
			musicVolumeText.setEditable(false);
			musicVolumeText.setOpaque(false);
			musicVolumeText.setBounds(5, 95, 120, 60);
			
			musicVolumeSlider.setName("musicSlider");
			musicVolumeSlider.addChangeListener(new SliderListener());
			musicVolumeSlider.setMajorTickSpacing(10);
			musicVolumeSlider.setBounds(115, 100, 260, 30);
			musicVolumeSlider.setPaintTicks(true);
			musicVolumeSlider.setOpaque(false);


			dropDownText.setText("UI Location: ");
			dropDownText.setFont(new Font("Arial", Font.BOLD, 15));
			dropDownText.setEditable(false);
			dropDownText.setOpaque(false);
			dropDownText.setBounds(5, 145, 120, 60);
			dropDown.setBounds(150,145,150,30);

			cardDropText.setText("Card Back: ");
			cardDropText.setFont(new Font("Arial", Font.BOLD, 15));
			cardDropText.setEditable(false);
			cardDropText.setOpaque(false);
			cardDropText.setBounds(5, 195, 120, 60);
			cardDropDown.setBounds(150,195,150,30);

			backgroundDropText.setText("Background Color: ");
			backgroundDropText.setFont(new Font("Arial", Font.BOLD, 15));
			backgroundDropText.setEditable(false);
			backgroundDropText.setOpaque(false);
			backgroundDropText.setBounds(5, 245, 150, 60);
			backgroundDropDown.setBounds(150,245,150,30);

			musicDropText.setText("Music: ");
			musicDropText.setFont(new Font("Arial", Font.BOLD, 15));
			musicDropText.setEditable(false);
			musicDropText.setOpaque(false);
			musicDropText.setBounds(5, 295, 150, 60);
			musicDropDown.setBounds(150,295,150,30);

			checked = GameMode.autoMove;

			autoMoveText.setText("Auto Move: ");
			autoMoveText.setFont(new Font("Arial", Font.BOLD, 15));
			autoMoveText.setEditable(false);
			autoMoveText.setOpaque(false);
			autoMoveText.setBounds(5, 340, 120, 60);

			checkBox= new JCheckBox();
			checkBox.removeActionListener(myCheckListener);
			checkBox.addActionListener(myCheckListener);
			checkBox.setSelected(checked);
			checkBox.setBounds(150,340,30,30);

			
			confirmButton.setBounds(135, 380, 130, 30);
			confirmButton.removeActionListener(myConfirmListener);
			confirmButton.addActionListener(myConfirmListener);
			confirmButton.setEnabled(true);

			ruleTable.add(confirmButton);
			ruleTable.add(volumeSlider);
			ruleTable.add(volumeText);
			ruleTable.add(musicVolumeSlider);
			ruleTable.add(musicVolumeText);
			ruleTable.add(dropDown);
			ruleTable.add(dropDownText);
			ruleTable.add(cardDropDown);
			ruleTable.add(cardDropText);
			ruleTable.add(backgroundDropDown);
			ruleTable.add(backgroundDropText);
			ruleTable.add(musicDropDown);
			ruleTable.add(musicDropText);
			ruleTable.add(checkBox);
			ruleTable.add(autoMoveText);

			ruleTable.setVisible(true);
			ruleFrame.setVisible(true);
		}

		/**
		 * Nested class that implements the ActionListener for the confirm options button.
		 */
		private class confirmOptionsListener implements ActionListener
		{
			/**
			 * adjust the game settings based on the users selected preferences
			 */
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// sets the temporary music path to the selected music's file path
				String musicPath = "";
				if(musicDropDown.getSelectedItem().equals("Tavern")) {
					musicPath = "Sounds/Loop_The_Old_Tower_Inn.wav";
				} else if(musicDropDown.getSelectedItem().equals("Arcade")) {
					musicPath =  "Sounds/boss_battle_#2.wav";
				} else if(musicDropDown.getSelectedItem().equals("Pink Floyd")) {
					musicPath =  "Sounds/Several_Species_Of_Small_Furry_Animals.wav";
				}

				/**
				 * Checks if the selected music is currently playing and if not, stops the current music and sets the music and the music path in the platform to the temp music path
				 */
				if(Platform.musicPath != musicPath) {
					Platform.setMusic(musicPath);
				}

				// Adjusts the music volume
				Platform.setVolume();

				// Adjust the positioning of the UI components
				if(dropDown.getSelectedItem().equals("Top")) {
					_Direction = Direction.UP;
				} else if(dropDown.getSelectedItem().equals("Bottom")) {
					_Direction = Direction.DOWN;
				}
				positionButtons(_Direction);

				// Adjusts the card image based on the selected cardset
				if(cardDropDown.getSelectedItem().equals("Ocean")) {
					GameMode.cardPath = "CardImages/greywyvern-cardset/";
				} else if(cardDropDown.getSelectedItem().equals("Original")) {
					GameMode.cardPath =  "CardImages/original/";
				}
				if(currentGameMode != null) {
					currentGameMode.refreshCards();
				}

				// changes if the game should automatically move cards to the foundation
				GameMode.autoMove = checked;

				// changes the background color of the platform
				if(Platform.backgroundColors.length > backgroundDropDown.getSelectedIndex()) {
					table.setBackground(Platform.backgroundColors[backgroundDropDown.getSelectedIndex()]);
				}
				
				// removes the current options menu 
				ruleFrame.dispose();
			}
		}

		/**
		 * Nested class that implements the action listener for the check box and adjusted checked accordingly
		 */
		private class CheckListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JCheckBox myBox = (JCheckBox) e.getSource();
				if( myBox.isSelected()) {
					checked = true;
				} else {
					checked = false;
				}
			}
		}

		/**
		 * Nested class that implements the change listener for the music and volume sliders and adkists the volumes accordingly
		 */
		private class SliderListener implements ChangeListener
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				JSlider slider = (JSlider) e.getSource();
				if(slider.getName() == "musicSlider") {
					Platform.musicVolume = (int)slider.getValue();
				} else {
					Platform.volume = (int)slider.getValue();
				}
			}
		}
	}

	/**
     * Nested class that implements ActionListener(When the button is pressed 
     * it calls the current gameMode to redo the last move and then updates the scores that are displayed). 
     */
	private class RedoListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			currentGameMode.redo();
			String newScore = "Score: " + currentGameMode.score;
			scoreBox.setText(newScore);
			scoreBox.repaint();
		}
	}

	/**
     * Nested class that implements ActionListener(When the button is pressed 
     * it calls the current gameMode to do a hint move). 
     */
	private class HintListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			currentGameMode.hint();
		}
	}

	/**
     * Nested class that implements ActionListener(When the button is pressed 
     * it calls the current gameMode to undo the last move and then updates the scores that are displayed). 
     */
	private class UndoListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			currentGameMode.undo();
			String newScore = "Score: " + currentGameMode.score;
			scoreBox.setText(newScore);
			scoreBox.repaint();
		}
	}

	/**
     * Nested class containing mousePressed, mouseReleased, and mouseMoved methods( 
     * These methods call their corresponding methods in the currentGameMode).
     */
	private class CardMovementManager extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e)
		{
			if(currentGameMode != null) {
				currentGameMode.mousePressed(e);
				statusBox.setText("");
			}
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if(currentGameMode != null) {
				currentGameMode.mouseReleased(e);
				String newScore = "Score: " + currentGameMode.score;
				if(newScore.equals(scoreBox.getText())) {
					statusBox.setText("That Is Not A Valid Move");
				} else {
					statusBox.setText("That Is A Valid Move");
				}
				scoreBox.setText(newScore);
				scoreBox.repaint();
			}
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			if(currentGameMode != null) {
				currentGameMode.mouseMoved(e);
			}
		}

	}

	/**
     * Creates the UI buttons when the UI is first instantiated.
     */
    public void generateButtons() {
        mainMenuButton.addActionListener(new MainMenuListener());

		newGameButton.addActionListener(new NewGameListener());

		showRulesButton.addActionListener(new ShowRulesListener());
		
		toggleTimerButton.addActionListener(new ToggleTimerListener());

		saveButton.addActionListener(new SaveGameListener());

		loadButton.addActionListener(new LoadGameListener());

		redoButton.addActionListener(new RedoListener());

		undoButton.addActionListener(new UndoListener());

		hintButton.addActionListener(new HintListener());

		gameTitle.setText("<b>Team Three's Solitaire</b> <br> CPSC 4900 <br> Fall 2021");
		gameTitle.setEditable(false);
		gameTitle.setOpaque(false);

		scoreBox.setText("Score: 0");
		scoreBox.setEditable(false);
		scoreBox.setOpaque(false);

		timeBox.setText("Seconds: 0");
		timeBox.setEditable(false);
		timeBox.setOpaque(false);

		statusBox.setEditable(false);
		statusBox.setOpaque(false);

		optionsButton.addActionListener(new optionsListener());
		optionsButton.setEnabled(true);

        positionButtons(_Direction);
        addButtons();
		table.addMouseListener(new CardMovementManager());
		table.addMouseMotionListener(new CardMovementManager());
    }

	/**
     * Positions buttons based on the current UI settings.
     * @param myDirection
     */
    public void positionButtons(Direction myDirection) {
        _Direction = myDirection;
        switch (_Direction)
		{
            case LEFT:
                break;
            case RIGHT:
                break;
            case UP:
                mainMenuButton.setBounds(0, 0, 120, 30);
                newGameButton.setBounds(120, 0, 120, 30);
                showRulesButton.setBounds(240, 0, 120, 30);
                gameTitle.setBounds(775, 35, 100, 100);
                scoreBox.setBounds(360, 0, 120, 30);
                timeBox.setBounds(480, 0, 120, 30);
                toggleTimerButton.setBounds(600, 0, 125, 30);
                statusBox.setBounds(725, 0, 180, 30);
                saveButton.setBounds(905, 0, 125, 30);
                loadButton.setBounds(1030, 0, 125, 30);
                optionsButton.setBounds(1155, 0, 130, 30);
				undoButton.setBounds(1030, FlowerBed.TABLE_HEIGHT - 100, 125, 30);
				redoButton.setBounds(1155, FlowerBed.TABLE_HEIGHT - 100, 130, 30);
				hintButton.setBounds(1155, FlowerBed.TABLE_HEIGHT - 130, 130, 30);
                break;
            case DOWN:
                mainMenuButton.setBounds(0, FlowerBed.TABLE_HEIGHT - 70, 120, 30);
                newGameButton.setBounds(120, FlowerBed.TABLE_HEIGHT - 70, 120, 30);
                showRulesButton.setBounds(240, FlowerBed.TABLE_HEIGHT - 70, 120, 30);
                gameTitle.setBounds(775, 35, 100, 100);
                scoreBox.setBounds(360, FlowerBed.TABLE_HEIGHT - 70, 120, 30);
                timeBox.setBounds(480, FlowerBed.TABLE_HEIGHT - 70, 120, 30);
                toggleTimerButton.setBounds(600, FlowerBed.TABLE_HEIGHT - 70, 125, 30);
                statusBox.setBounds(725, FlowerBed.TABLE_HEIGHT - 70, 180, 30);
                saveButton.setBounds(905, FlowerBed.TABLE_HEIGHT - 70, 125, 30);
                loadButton.setBounds(1030, FlowerBed.TABLE_HEIGHT - 70, 125, 30);
                optionsButton.setBounds(1155, FlowerBed.TABLE_HEIGHT - 70, 130, 30);
				undoButton.setBounds(1030, FlowerBed.TABLE_HEIGHT - 100, 125, 30);
				redoButton.setBounds(1155, FlowerBed.TABLE_HEIGHT - 100, 130, 30);
				hintButton.setBounds(1155, FlowerBed.TABLE_HEIGHT - 130, 130, 30);
                break;
		}
    }

	/**
     * Adds buttons to JPanel.
     */
    public void addButtons() {
		table.add(statusBox);
		table.add(toggleTimerButton);
		table.add(saveButton);
		table.add(loadButton);
		table.add(optionsButton);
		table.add(gameTitle);
		table.add(timeBox);
		table.add(mainMenuButton);
		table.add(newGameButton);
		table.add(showRulesButton);
		table.add(scoreBox);
		table.add(undoButton);
		table.add(redoButton);
		table.add(hintButton);
		table.repaint();
	}

	/**
     * Makes non-platform buttons unclickable.
     */
	public void disableMainMenuButtons() {
		toggleTimerButton.setEnabled(false);
		statusBox.setEnabled(false);
		saveButton.setEnabled(false);
		loadButton.setEnabled(false);
		timeBox.setEnabled(false);
		mainMenuButton.setEnabled(false);
		newGameButton.setEnabled(false);
		showRulesButton.setEnabled(false);
		scoreBox.setEnabled(false);
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);
		hintButton.setEnabled(false);
	}

	/**
     * Enables all UI buttons.
     */
	public void enableAllButtons() {
		toggleTimerButton.setEnabled(true);
		statusBox.setEnabled(true);
		saveButton.setEnabled(true);
		loadButton.setEnabled(true);
		optionsButton.setEnabled(true);
		timeBox.setEnabled(true);
		mainMenuButton.setEnabled(true);
		newGameButton.setEnabled(true);
		showRulesButton.setEnabled(true);
		scoreBox.setEnabled(true);
		undoButton.setEnabled(true);
		redoButton.setEnabled(true);
		hintButton.setEnabled(true);
	}
}
