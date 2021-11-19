import java.awt.Container;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;
import java.util.ArrayList;

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

	private JButton pageLeftButton = new JButton("<");
	private JButton pageRightButton = new JButton(">");

	/**
	 * The background color options for the game platform.
	 * If a new color is added then it must be added as an option in MenuButtons->backgroundDropDownOptions
	 */
	public static Color[] backgroundColors = {  new Color(0, 180, 0), new Color(255, 255, 255), new Color(204, 0, 0), new Color(51, 204, 255), Color.decode("#C594343"), Color.decode("#6F7498"), Color.decode("#A3B7F9"),  Color.decode("#092C48")};


	// Music Control Components
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
     * The current volume of the special effects. 
     */
	public static int volume = -40;
	/**
     * The current volume of the music.
     */
	public static int musicVolume = 0;
	/**
     * The path to the .wav music file currently in use
     */
	public static String musicPath = "Sounds/Several_Species_Of_Small_Furry_Animals.wav";


	// GameModeButton control components
	/**
	 * The amount of buttons to display vertically
	 */
	private int gridHeight = 6;
	/**
	 * The amount of buttons to display horizontally
	 */
	private int gridWidth = 3;
	/**
	 * The current page of GameModeButtons displayed 
	 */
	private int gridPage = 0;
	/**
	 * The total maximum amount of GameModeButtons displayed on a single page
	 */
	private int pageSize = gridHeight * gridWidth;
	/**
	 * The total amount of pages, calculated based on the total amount of GameModeButtons / the pageSize
	 */
	private int maxPages = 1;
	/**
	 * How much to horizontally offset columns of GameModeButtons from each other
	 */
	private int gameButtonXOffset = 250;
	/**
	 * How much to horizontally offset the first column of GameModeButtons from the edge of the platform
	 */
	private int gameButtonXOrigin = 20;
	/**
	 * How much to vertically offset columns of GameModeButtons
	 */
	private int gameButtonYOffset = 100;
	/**
	 * How much to vertically offset the first column of GameModeButtons from the edge of the platform
	 */
	private int gameButtonYOrigin = 90;

	/**
     * Nested class that provides the action listener for the show favorites button. When the button is clicked it
	 * cycles between showing the users favorite games and all the games on the platform.
     */
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

	/**
     * Nested class that provides the action listener for the left arrow button. When the button is clicked it
	 * cycles LEFT through the displayed favorite games if the total amount of games exceeds the page limit.
     */
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

	/**
     * Nested class that provides the action listener for the left arrow button. When the button is clicked it
	 * cycles RIGHT through the displayed favorite games if the total amount of games exceeds the page limit.
     */
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

	/**
	 * Displays all the favorite games from the currently selected page. Switches the show favorites button to display the show all option
	 */
	private void showFavorites() {
		table.removeAll();
		table.add(showFavoritesButton);
		table.add(pageLeftButton);
		table.add(pageRightButton);

		showFavoritesButton.setText("Show All");
		showFavorites = false;

		int row = 0;
		int col = 0;
		boolean hide = false;
		int startCount = pageSize * gridPage;
		int count = 0;
		for (int x = 0; x < gameModeButtons.size(); x++)
		{
			if(gameModeButtons.get(x).getFavorite()) {

				if(count >= startCount && count < startCount + pageSize) {
					if(!hide) {
						int height = gameButtonYOrigin + gameButtonYOffset * row;
						gameModeButtons.get(x).setPosition(gameButtonXOrigin + gameButtonXOffset * col, height);
						gameModeButtons.get(x).refreshScores();

						if(row + 1 < gridHeight) {
							row += 1;
						} else {
							row = 0;
							if(col + 1 < gridWidth) {
								col += 1;
							} else {
								break;
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

	/**
	 * Displays all the games from the currently selected page. Switches the show favorites button to display the show favorites option
	 */
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
				int height = gameButtonYOrigin + gameButtonYOffset * row;
				gameModeButtons.get(x).setPosition(gameButtonXOrigin + gameButtonXOffset * col, height);
				gameModeButtons.get(x).refreshScores();

				if(row + 1 < gridHeight) {
					row += 1;
				} else {
					row = 0;
					if(col + 1 < gridWidth) {
						col += 1;
					} else {
						break;
					}
				}
			}
		}
		menuButtons.addButtons();
		table.repaint();
	}

	/**
	 * Starts the selected music file based on the musicPath string.
	 */
	private void startMusic() {
			setMusic(musicPath);
			setVolume();
	}

	/**
	 * Sets the selected music file based on the musicPath string.
	 */
	public static void setMusic(String mPath) {
		try {
			AudioInputStream musicStream = AudioSystem.getAudioInputStream(new File(mPath));
			if(music != null){
				music.stop();
			}
			music = AudioSystem.getClip();
			music.open(musicStream);
			music.loop(Clip.LOOP_CONTINUOUSLY);
			musicPath = mPath;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the volume of the currently playing music
	 */
	public static void setVolume() {
		FloatControl volumeControl = (FloatControl) music.getControl(FloatControl.Type.MASTER_GAIN);
		volumeControl.setValue(musicVolume);
	}

	/**
     * Plays the audio file at the selected path.
     * @param audioPath a string containing the path location of the audio file in relation to the project folder.
     */
	public static void playSound(String audioPath) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(audioPath));
            Clip soundEffect = AudioSystem.getClip();
            soundEffect.open(audioStream);
            FloatControl volumeControl = (FloatControl) soundEffect.getControl(FloatControl.Type.MASTER_GAIN);
            int vol = volume;
            if(vol + 10 > 6) {
                vol = 6;
            } else {
                vol += 10;
            }
            volumeControl.setValue(vol);
            soundEffect.start();
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * Creates the initial platform UI elements and adds them to the table and starts the music. Game modes and their buttons are added for each game mode the platform supports.
	 */
    private void playGamePlatform()
	{
		startMusic();
		table.removeAll();

		//Add GameModes here
		myGameModes.add(new FlowerBed(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));
		myGameModes.add(new Solitaire(table ,frame, this, GameMode.cardPath, GameMode.backgroundPath));


		int col = 0;
		int row = 0;
		boolean hide = false;
		for (int x = pageSize * gridPage; x < myGameModes.size(); x++)
		{
			gameModeButtons.add(new GameModeButton(myGameModes.get(x), table, frame, this));
			
			if(!hide) {
				if(gameModeButtons.get(x).getFavorite()) {
					int height = gameButtonYOrigin + gameButtonYOffset * row;
					gameModeButtons.get(x).setPosition(gameButtonXOrigin + gameButtonXOffset * col, height);
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
     * Updates scores and stats in the word file containing all the stored GameMode stats. Called from a GameMode child class when a game is won or lost. 
	 * If no entry exsists for the current game then one is made and stored.
     * Format: GameMode name:favorited(1 yes 0 no):high score,best time,total games played,total games won,last score,last time
	 * Example: Flower Bed:1:10015,2,412,65,0,1
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
     * Saves the current state of the game in the word file coresponding to the game mode. Called from a GameMode child class. If no file exsists for the current game then one is made and stored.
     * @param gameName a string containing the name of the game mode being saved
	 * @param cardList a string containing each stack of cards
     * (example: value,suit:value,suit: then repeat on new line for each stack)
     */
	public void saveGame(String gameName, String cardList) {
		File saveFile = new File(gameName + "SavedFile.txt");
		boolean fileExists = saveFile.exists();

		if(fileExists) {
			System.out.println("old file");
			try {
				PrintWriter writer = new PrintWriter(gameName + "SavedFile.txt");
				writer.print(cardList);
				writer.close();
			} catch(IOException i) {
				i.printStackTrace();
			}
		} else {
			System.out.println("new file");
			try {
				PrintWriter writer = new PrintWriter(saveFile);
				writer.print(cardList);
				writer.close();
			} catch(IOException i) {
				i.printStackTrace();
			}
		}
	}

	/**
     * Loads the current state of the game from the word file coresponding to the game mode. Called from a GameMode child class and returns stack and score/time information.
	 * If no file exsists for the current game mode or the file contains an error then a error message UI box is displayed instead
     * @return List of strings containing each stack of cards and the current score/time
     * (example: value,suit:value,suit: for each stack, final two lines are score and time)
     */
	public List<String> loadGame(String gameName) {
		BufferedReader reader;
		List<String> lines = new ArrayList<String>();

		File loadFile = new File(gameName + "SavedFile.txt");
		boolean fileExists = loadFile.exists();

		if(fileExists) {
			try {
				reader = new BufferedReader(new FileReader(loadFile));
				while(reader.ready()) {
					lines.add(reader.readLine());
				}
				reader.close();
			} catch(IOException i) {
				i.printStackTrace();
			}
		} else {
			JFrame confirmFrame = new JFrame("Something went wrong");
			JPanel confirmTable = new JPanel();
			JEditorPane confirmText = new JEditorPane();
			Container contentPane;


			confirmTable.setLayout(null);

			contentPane = confirmFrame.getContentPane();
			contentPane.add(confirmTable);

			confirmFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			confirmFrame.setSize(400, 200);

			confirmText.setText("Sorry! No save file exists for this game!");
			confirmText.setFont(new Font("Arial", Font.BOLD, 15));
			confirmText.setEditable(false);
			confirmText.setOpaque(false);
			confirmText.setBounds(5, 45, 400, 60);

			confirmTable.add(confirmText);

			confirmTable.setVisible(true);
			confirmFrame.setVisible(true);
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
        menu.playGamePlatform();

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
