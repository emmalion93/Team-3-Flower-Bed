import java.awt.Container;
import java.awt.Color;
import javax.swing.JButton;
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
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Arrays;

public class StartMenu {

    // CONSTANTS
	/*public static final int TABLE_HEIGHT = Card.CARD_HEIGHT * 4;
	public static final int TABLE_WIDTH = (Card.CARD_WIDTH * 12) + 100;*/

    // GUI COMPONENTS (top level)
    private static final JFrame frame = new JFrame("Solitaire");
    protected static final JPanel table = new JPanel();
	protected static final MenuButtons menuButtons = new MenuButtons(table, frame);

	private JButton showFavoritesButton = new JButton("Show All");
	private boolean showFavorites = false;

	private List<GameMode> myGameModes = new ArrayList<GameMode>();
	private List<GameModeButton> gameModeButtons = new ArrayList<GameModeButton>();
	public static Clip music;
	public static int volumeMax = 6;
	public static int volumeMin = -54;
	public static int volumeStart = -40;
	public static int volume = -40;
	public static int musicVolume = -40;

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

	private void showFavorites() {
		table.removeAll();
		table.add(showFavoritesButton);

		showFavoritesButton.setText("Show All");
		showFavorites = false;

		int count = 0;
		for (int x = 0; x < gameModeButtons.size(); x++)
		{
			if(gameModeButtons.get(x).getFavorite()) {
				int height = 90 + 90 * count;
				gameModeButtons.get(x).setPosition(20, height);
				gameModeButtons.get(x).refreshScores();
				count++;
			}
		}
		menuButtons.addButtons();
		table.repaint();
	}

	private void showAll() {
		table.removeAll();
		table.add(showFavoritesButton);

		showFavoritesButton.setText("Show Favorites");
		showFavorites = true;
		for (int x = 0; x < gameModeButtons.size(); x++)
		{
			int height = 90 + 90 * x;
			gameModeButtons.get(x).setPosition(20, height);
		}
		menuButtons.addButtons();
		table.repaint();
	}

	private void startMusic() {
		// TODO music starts in loud 
		try {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(this.getClass().getResource("Sounds/Loop_The_Old_Tower_Inn.wav"));
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
      
		int count = 0;
		for (int x = 0; x < myGameModes.size(); x++)
		{
			gameModeButtons.add(new GameModeButton(myGameModes.get(x), table, frame, this));
			if(gameModeButtons.get(x).getFavorite()) {
				int height = 90 + 90 * count;
				gameModeButtons.get(x).setPosition(20, height);
				count++;
			}
		}

		showFavoritesButton.setBounds(0, 50, 150, 30);
		showFavoritesButton.addActionListener(new ShowFavoritesListener());
		showFavorites();
		
		table.repaint();
	}

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

	public void saveGame(String cardList) {
		try {
			PrintWriter writer = new PrintWriter("SavedFile.txt");
			writer.print(cardList);
			writer.close();
		} catch(IOException i) {
			i.printStackTrace();
		}
	}

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

	public void returnToMenu() {
		table.removeAll();
		showFavorites();
		menuButtons.addButtons();
		menuButtons.disableMainMenuButtons();
		menuButtons.setGameMode(null);
		menuButtons.stopTimer();
		table.repaint();
	}

	public void startGame(GameMode gameMode) {
		table.removeAll();
		menuButtons.addButtons();
		menuButtons.enableAllButtons();
		menuButtons.setGameMode(gameMode);
		menuButtons.startTimer();
		gameMode.execute();
	}

	public static void execute() {
		Container contentPane;
		frame.setSize(FlowerBed.TABLE_WIDTH, FlowerBed.TABLE_HEIGHT);

		table.setLayout(null);
		table.setBackground(new Color(0, 180, 0));

		contentPane = frame.getContentPane();
		contentPane.add(table);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
		StartMenu menu = new StartMenu();
		menuButtons.generateButtons();
		menuButtons.disableMainMenuButtons();
        menu.playMainMenu();

		frame.setVisible(true);
	}


	public static void main(String[] args)
	{
		execute();
	}
}
