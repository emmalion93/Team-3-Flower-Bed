import java.awt.Container;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;
import java.util.ArrayList;

import javax.print.attribute.standard.PagesPerMinute;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * The game platform that contains and manages all the GameMode and UI objects. 
 * Contains methods that save, load, or manage data and methods for allowing a 
 * player to select between game modes.
 * @author Emily
 */
public class Platform {

    // GUI COMPONENTS (top level)
    private static final JFrame frame = new JFrame("Solitaire");
    protected static final JPanel table = new JPanel();
	protected static final UI menuButtons = new UI(table, frame);

	private JButton showFavoritesButton = new JButton("Show All");
	private boolean showFavorites = false;

	private List<GameMode> myGameModes = new ArrayList<GameMode>();
	private List<GameModeButton> gameModeButtons = new ArrayList<GameModeButton>();
	/**
     * The currently running music clip.
     */
	public static Clip music;
	/**
     * the maximum value for volume
     */
	public static int volumeMax = 6;
	/**
     * The minimum value for volume.
     */
	public static int volumeMin = -54;
	/**
     * The starting value for volume. 
     */
	public static int volumeStart = -40;
	/**
     * The volume of the special effects. 
     */
	public static int volume = -40;
	/**
     * The volume of the music.
     */
	public static int musicVolume = -40;

	public static String musicPath = "Sounds/Loop_The_Old_Tower_Inn.wav";

	// Add these colors as options in MenuButtons->backgroundDropDownOptions
	public static Color[] backgroundColors = {  new Color(0, 180, 0), new Color(255, 255, 255), new Color(204, 0, 0), new Color(51, 204, 255), Color.decode("#C594343"), Color.decode("#6F7498"), Color.decode("#A3B7F9"),  Color.decode("#092C48")};

	private int gridHeight = 6;
	private int gridWidth = 3;
	private int gridPage = 0;
	private int pageSize = gridHeight * gridWidth;
	private int maxPages = 1;

	private JButton pageLeftButton = new JButton("<");;
	private JButton pageRightButton = new JButton(">");;

	private class ShowFavoritesListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (showFavorites)
			{
				showFavorites();
			} else
			{
				showAll();
			}
		}
	}

	private class PageLeftListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(gridPage - 1 < 0) {
				gridPage = maxPages;
			} else {
				gridPage -= 1;
			}
		}
	}

	private class PageRightListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(gridPage + 1 <= maxPages) {
				gridPage += 1;
			} else {
				gridPage = 0;
			}
			if (!showFavorites)
				{
					showFavorites();
				} else
				{
					showAll();
				}
		}
	}

	private void showFavorites() {
		table.removeAll();
		table.add(showFavoritesButton);
		table.add(pageLeftButton);
		table.add(pageRightButton);

		showFavoritesButton.setText("Show All");
		showFavorites = false;

		int count = 0;
		int row = 0;
		int col = 0;
		boolean hide = false;
		int startCount = pageSize * gridPage;
		for (int x = 0; x < gameModeButtons.size(); x++)
		{
			if(gameModeButtons.get(x).getFavorite()) {

				if(count >= startCount && count < startCount + pageSize) {
					if(!hide) {
						int height = 90 + 100 * row;
						gameModeButtons.get(x).setPosition(20 + 250 * col, height);
						gameModeButtons.get(x).refreshScores();

						if(row + 1 < gridHeight) {
							row += 1;
						} else {
							row = 0;
							if(col + 1 < gridWidth) {
								col += 1;
							} else {
								hide = true;
							}
						}
					}
				}
				count++;
			}
		}
		menuButtons.addButtons();
		table.repaint();
	}

	private void showAll() {
		table.removeAll();
		table.add(showFavoritesButton);
		table.add(pageLeftButton);
		table.add(pageRightButton);

		showFavoritesButton.setText("Show Favorites");
		showFavorites = true;
		int row = 0;
		int col = 0;
		boolean hide = false;
		for (int x = pageSize * gridPage; x < gameModeButtons.size(); x++)
		{
			if(!hide) {
				int height = 90 + 100 * row;
				gameModeButtons.get(x).setPosition(20 + 250 * col, height);

				if(row + 1 < gridHeight) {
					row += 1;
				} else {
					row = 0;
					if(col + 1 < gridWidth) {
						col += 1;
					}else {
						hide = true;
					}
				}
			}
		}
		menuButtons.addButtons();
		table.repaint();
	}

	private void startMusic() {
		// TODO music starts in loud 
		try {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(musicPath));
			music = AudioSystem.getClip();
			music.open(audioStream);
			music.loop(Clip.LOOP_CONTINUOUSLY);
			FloatControl volumeControl = (FloatControl) music.getControl(FloatControl.Type.MASTER_GAIN);
			volumeControl.setValue(musicVolume);
			music.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void playMainMenu()
	{
		startMusic();
		table.removeAll();

		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new Solitaire(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));



		int col = 0;
		int row = 0;
		boolean hide = false;
		for (int x = pageSize * gridPage; x < myGameModes.size(); x++)
		{
			gameModeButtons.add(new GameModeButton(myGameModes.get(x), table, frame, this));
			
			if(!hide) {
				if(gameModeButtons.get(x).getFavorite()) {
					int height = 90 + 100 * row;
					gameModeButtons.get(x).setPosition(20 + 250 * col, height);
				}

				if(row + 1 < gridHeight) {
					row += 1;
				} else {
					row = 0;
					if(col + 1 < gridWidth) {
						col += 1;
					} else {
						hide = true;
					}
				}
			}
		}
		maxPages = (int)Math.ceil((double)(gameModeButtons.size() / pageSize));

		pageLeftButton.addActionListener(new PageLeftListener());
		pageLeftButton.setBounds(270, 50, 50, 30);

		pageRightButton.addActionListener(new PageRightListener());
		pageRightButton.setBounds(470, 50, 50, 30);

		showFavoritesButton.setBounds(320, 50, 150, 30);
		showFavoritesButton.addActionListener(new ShowFavoritesListener());
		showFavorites();
		
		table.repaint();
	}

	/**
     * Updates scores and stats in the database. called from a GameMode child class when a game is won or lost.
     * @param gameName The name of the game wanting to update its scores.
     * @param score The current score of the game.
     * @param time The current time of the game.
     * @param win Whether the game was won or lost.
     */
	public void updateScores(String gameName, int score, int time, boolean win) {
		BufferedReader reader;
		List<String> lines = new ArrayList<String>();

		try {
			reader = new BufferedReader(new FileReader("SavedScores.txt"));
			while(reader.ready()) {
				lines.add(reader.readLine());
			}
			reader.close();
		} catch(IOException i) {
			i.printStackTrace();
		}

		boolean found = false;
		for(int x = 0; x < lines.size(); x++)
		{
			List<String> games = Arrays.asList(lines.get(x).split(":"));
			if(gameName.equals(games.get(0))) {
				List<String> highScores = Arrays.asList(games.get(2).split(","));
				String favorited = games.get(1);
				int gameNumber = Integer.parseInt(highScores.get(2)) + 1;
				int winNumber = Integer.parseInt(highScores.get(3));
				if(win) {
					if(score > Integer.parseInt(highScores.get(0))) {
						highScores.set(0, "" + score);
					}
					if(time < Integer.parseInt(highScores.get(1))) {
						highScores.set(1, "" + time);
					}
					winNumber += 1;
				}
				lines.set(x, games.get(0) + ":" + favorited + ":" + highScores.get(0) + "," + highScores.get(1) + "," + gameNumber + "," + winNumber + "," + score + "," + time);
				found = true;
				break;
			}
		}
		if(!found) {
			if(win) {
				lines.add(gameName + ":" + "1:" + score + "," + time + ",1,1," + score + "," + time);
			} else {
				lines.add(gameName + ":" + "1:" + score + "," + time + ",1,0," + score + "," + time);
			}
		}

		try {
			PrintWriter writer = new PrintWriter("SavedScores.txt");
			for(int x = 0; x < lines.size(); x++)
			{
				writer.println(lines.get(x));
			}
			writer.close();
		} catch(IOException i) {
			i.printStackTrace();
		}
	}

	/**
     * Saves the current state of the game to the database. called from a GameMode 
     * child class.
     * @param cardList a string containing each stack of cards
     * (example: value,suit:value,suit: then repeat on new line for each stack)
     */
	public void saveGame(String cardList) {
		try {
			PrintWriter writer = new PrintWriter("SavedFile.txt");
			writer.print(cardList);
			writer.close();
		} catch(IOException i) {
			i.printStackTrace();
		}
	}

	/**
     * loads the current state of the game to the database. called from a GameMode 
     * child class and returns stack and score/time information.
     * @return List of strings containing each stack of cards and the current score/time
     * (example: value,suit:value,suit: for each stack, final two lines are score and time)
     */
	public List<String> loadGame() {
		BufferedReader reader;
		List<String> lines = new ArrayList<String>();

		try {
			reader = new BufferedReader(new FileReader("SavedFile.txt"));
			while(reader.ready()) {
				lines.add(reader.readLine());
			}
			reader.close();
		} catch(IOException i) {
			i.printStackTrace();
		}
		return lines;
	}

	/**
     * Stops currently running game and shows the platform menu.
     */
	public void returnToPlatform() {
		table.removeAll();
		showFavorites();
		menuButtons.addButtons();
		menuButtons.disableMainMenuButtons();
		menuButtons.setGameMode(null);
		menuButtons.stopTimer();
		table.repaint();
	}

	/**
     * Disables the platform menu and starts the selected game.
     * @param gameMode the selected game.
     */
	public void startGame(GameMode gameMode) {
		table.removeAll();
		menuButtons.addButtons();
		menuButtons.enableAllButtons();
		menuButtons.setGameMode(gameMode);
		menuButtons.startTimer();
		gameMode.execute();
	}

	/**
     * Sets the frame and table information, generates the UI, and starts the 
     * platform menu for the first time.
     */
	public static void execute() {
		Container contentPane;
		frame.setSize(FlowerBed.TABLE_WIDTH, FlowerBed.TABLE_HEIGHT);

		table.setLayout(null);
		table.setBackground(backgroundColors[0]);

		contentPane = frame.getContentPane();
		contentPane.add(table);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
		Platform menu = new Platform();
		menuButtons.generateButtons();
		menuButtons.disableMainMenuButtons();
        menu.playMainMenu();

		frame.setVisible(true);
	}

	/**
     * Runs the execute method.
     * @param args
     */
	public static void main(String[] args)
	{
		execute();
	}
}
