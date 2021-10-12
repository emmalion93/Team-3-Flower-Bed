import javax.sound.sampled.FloatControl;
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
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;

import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MenuButtons {

    public static enum Direction
	{
		LEFT, RIGHT, UP, DOWN;
	}

    private Direction _Direction = Direction.UP;

    private JFrame frame;
	protected JPanel table;

    private GameMode currentGameMode;

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

    public MenuButtons(JPanel myTable, JFrame myFrame) {
        table = myTable;
        frame = myFrame;
		//startTimer();
    }

    public void setGameMode(GameMode myGameMode) {
        currentGameMode = myGameMode;
    }

    // BUTTON LISTENERS
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

	private class MainMenuListener implements ActionListener
	{
		private JFrame confirmFrame = new JFrame("Return To Menu");
		private JPanel confirmTable = new JPanel();
		private JEditorPane confirmText = new JEditorPane();
		private JButton confirmButton = new JButton("Confirm");
		private ActionListener myConfirm = new confirmMenuListener();

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

		private class confirmMenuListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentGameMode.startMenu();
				confirmFrame.dispose();
			}
		}
	}

	private class SaveGameListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			currentGameMode.saveGame();
		}
	}

	private class LoadGameListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			currentGameMode.loadGame();
		}
	}

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

	protected void startTimer()
	{
		scoreClock = new ScoreClock();
		
		// set the timer to update every second
		timeRunning = true;
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(scoreClock, 1000, 1000);
	}

	public void toggleTimer()
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

	public void stopTimer()
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

	private class ScoreClock extends TimerTask
	{
		@Override
		public void run()
		{
			updateTimer();
		}
	}

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

    private class optionsListener implements ActionListener
	{
		private JFrame ruleFrame = new JFrame("OPTIONS");
		private JPanel ruleTable = new JPanel();
		private JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL, StartMenu.volumeMin, StartMenu.volumeMax, StartMenu.volumeStart);
		private JSlider musicVolumeSlider = new JSlider(JSlider.HORIZONTAL, StartMenu.volumeMin, StartMenu.volumeMax, StartMenu.volumeStart);
		private JEditorPane volumeText = new JEditorPane();
		private JEditorPane musicVolumeText = new JEditorPane();
		private JButton confirmButton = new JButton("Confirm");
		private ActionListener myConfirm = new confirmOptionsListener();
		private JEditorPane dropDownText = new JEditorPane();
		private String[] dropDownOptions = { "Top", "Bottom" };
		private JComboBox<String> dropDown = new JComboBox<String>(dropDownOptions);
		private JEditorPane cardDropText = new JEditorPane();
		private String[] cardDropDownOptions = { "Ocean", "Original" };// "CardImages\\greywyvern-cardset\\", "CardImages\\original\\"
		private JComboBox<String> cardDropDown = new JComboBox<String>(cardDropDownOptions);
		private JEditorPane backgroundDropText = new JEditorPane();
		private String[] backgroundDropDownOptions = { "Green", "White", "Red", "Blue" };
		private JComboBox<String> backgroundDropDown = new JComboBox<String>(backgroundDropDownOptions);
		private JEditorPane autoMoveText = new JEditorPane();
		private JCheckBox checkBox;
		private boolean checked;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			ruleTable.removeAll();
			Container contentPane;


			ruleTable.setLayout(null);

			contentPane = ruleFrame.getContentPane();
			contentPane.add(ruleTable);

			ruleFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			ruleFrame.setSize(400, 400);

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

			checked = GameMode.autoMove;

			autoMoveText.setText("Auto Move: ");
			autoMoveText.setFont(new Font("Arial", Font.BOLD, 15));
			autoMoveText.setEditable(false);
			autoMoveText.setOpaque(false);
			autoMoveText.setBounds(5, 290, 120, 60);

			checkBox= new JCheckBox();
			checkBox.addActionListener(new CheckListener());
			checkBox.setSelected(checked);
			checkBox.setBounds(150,290,30,30);

			
			confirmButton.setBounds(135, 330, 130, 30);
			confirmButton.removeActionListener(myConfirm);
			confirmButton.addActionListener(myConfirm);
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
			ruleTable.add(checkBox);
			ruleTable.add(autoMoveText);

			ruleTable.setVisible(true);
			ruleFrame.setVisible(true);
		}

		private class confirmOptionsListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				FloatControl volumeControl = (FloatControl) StartMenu.music.getControl(FloatControl.Type.MASTER_GAIN);
				volumeControl.setValue(StartMenu.musicVolume);
				if(dropDown.getSelectedItem().equals("Top")) {
					_Direction = Direction.UP;
				} else if(dropDown.getSelectedItem().equals("Bottom")) {
					_Direction = Direction.DOWN;
				}

				if(cardDropDown.getSelectedItem().equals("Ocean")) {
					GameMode.cardPath = "CardImages/greywyvern-cardset/";
				} else if(cardDropDown.getSelectedItem().equals("Original")) {
					GameMode.cardPath =  "CardImages/original/";
				}
				if(currentGameMode != null) {
					currentGameMode.refreshCards();
				}

				GameMode.autoMove = checked;

				if(StartMenu.backgroundColors.length > backgroundDropDown.getSelectedIndex()) {
					table.setBackground(StartMenu.backgroundColors[backgroundDropDown.getSelectedIndex()]);
				}
				/*if(backgroundDropDown.getSelectedItem().equals("Green")) {
					table.setBackground(StartMenu.backgroundColors[0]);
				} else if(backgroundDropDown.getSelectedItem().equals("White")) {
					table.setBackground(StartMenu.backgroundColors[1]);
				}*/
				
				positionButtons(_Direction);
				ruleFrame.dispose();
			}
		}

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

		private class SliderListener implements ChangeListener
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				JSlider slider = (JSlider) e.getSource();
				if(slider.getName() == "musicSlider") {
					StartMenu.musicVolume = (int)slider.getValue();
				} else {
					StartMenu.volume = (int)slider.getValue();
				}
			}
		}
	}

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

	private class HintListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			currentGameMode.hint();
		}
	}

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

	private class CardMovementManager extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e)
		{
			if(currentGameMode != null) {
				currentGameMode.mousePressed(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if(currentGameMode != null) {
				currentGameMode.mouseReleased(e);
				String newScore = "Score: " + currentGameMode.score;
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

		//mainMenuButton.setBounds(0, TABLE_HEIGHT - 70, 120, 30);

		//newGameButton.setBounds(120, TABLE_HEIGHT - 70, 120, 30);

		//showRulesButton.setBounds(240, TABLE_HEIGHT - 70, 120, 30);

		gameTitle.setText("<b>Team Three's Solitaire</b> <br> CPSC 4900 <br> Fall 2021");
		gameTitle.setEditable(false);
		gameTitle.setOpaque(false);
		//gameTitle.setBounds(775, 20, 100, 100);

		//scoreBox.setBounds(360, TABLE_HEIGHT - 70, 120, 30);
		scoreBox.setText("Score: 0");
		scoreBox.setEditable(false);
		scoreBox.setOpaque(false);

		//timeBox.setBounds(480, TABLE_HEIGHT - 70, 120, 30);
		timeBox.setText("Seconds: 0");
		timeBox.setEditable(false);
		timeBox.setOpaque(false);

		//toggleTimerButton.setBounds(600, TABLE_HEIGHT - 70, 125, 30);

		//statusBox.setBounds(725, TABLE_HEIGHT - 70, 180, 30);
		statusBox.setEditable(false);
		statusBox.setOpaque(false);

		//saveButton.setBounds(905, TABLE_HEIGHT - 70, 125, 30);

		//loadButton.setBounds(1030, TABLE_HEIGHT - 70, 125, 30);

		//optionsButton.setBounds(1155, TABLE_HEIGHT - 70, 130, 30);
		optionsButton.addActionListener(new optionsListener());
		optionsButton.setEnabled(true);

        positionButtons(_Direction);
        addButtons();
		table.addMouseListener(new CardMovementManager());
		table.addMouseMotionListener(new CardMovementManager());
    }

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
