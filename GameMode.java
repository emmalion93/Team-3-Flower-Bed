import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.event.MouseEvent;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;
/**
 * The Parent class that acts as an abstract class for all solitaire game modes. 
 * Contains variables and methods needed by all game modes. Methods should be 
 * overridden in child class to give functionality specific to the child class.
 * @author Emily
 */
public class GameMode {
    // GUI COMPONENTS (top level)
    /**
     * The Frame the program takes place in. Set in constructor for child class.
     */
	protected JFrame frame;
    /**
     *The panel cards and other graphical components are added to. Set in constructor for child class.
     */
	protected JPanel table;

    // Game information Variables
    /**
     *The name of the specific game mode. Set in constructor for child class.
     */
	protected String gameName = "";
     /**
     *A short description of the specific game mode. Set in constructor for child class.
     */
	protected String gameDesc = "";
    /**
     *The long form rules of the specific game mode. Set in constructor for child class.
     */
    protected String gameRules = "No Rules Provided"; 
    /**
     *A reference to the game platform. Set in constructor for child class.
     */
    protected Platform mainMenu;
    /**
     *The time the current game has been running.
     */
    protected int time;
    /**
     *The score of the current game.
     */
    protected int score;
    /**
     *The path location in relation to the project folder of the current card images.
     */
    public static String cardPath = "CardImages/greywyvern-cardset/";
    /**
     *The path location in relation to the project folder of the current background image.
     */
    public static String backgroundPath = "CardImages/greywyvern-cardset/";
    public static boolean autoMove = true;

    /**
     *For use with the UI or Platform classes.
     * @return gameName
     */
    public String getName() { return gameName; }
    /**
     *For use with the UI or Platform classes.
     * @return gameDesc
     */
	public String getDesc() { return gameDesc; }
    /**
     *For use with the UI or Platform classes.
     * @return gameRules
     */
    public String getRules() { return gameRules; }

    /**
     *Begins a new iteration of the game. Called from the game platform.
     */
    public void execute() { }

    /**
     *Begins a new game of this game mode. Called from UI.
     */
    public void newGame() { JOptionPane.showMessageDialog(table, "This functionality is not enabled for this game mode"); }
    /**
     *Gathers information about the current game state and then passes it to the platform to be saved. Called from UI. *implementation in child class optional*
     */
    public void saveGame() { JOptionPane.showMessageDialog(table, "This functionality is not enabled for this game mode"); }
    /**
     *Gets information from the platform about a saved game state and then sets the current game state to that saved state. Called from UI. *implementation in child class optional*
     */
    public void loadGame() { JOptionPane.showMessageDialog(table, "This functionality is not enabled for this game mode"); }
    /**
     *Makes all cards belonging to this game mode refresh their graphics components. Called from UI. *implementation in child class optional*
     */
    public void refreshCards() {  }
    /**
     *Reverses a previous move using a stack of CardState objects. Called from UI. *implementation in child class optional*
     */
    public void undo() { JOptionPane.showMessageDialog(table, "This functionality is not enabled for this game mode"); }
    /**
     *Reverses a previous undo using a stack of CardState objects. Called from UI. *implementation in child class optional*
     */
    public void redo() { JOptionPane.showMessageDialog(table, "This functionality is not enabled for this game mode"); }
    public void hint() { JOptionPane.showMessageDialog(table, "This functionality is not enabled for this game mode"); }
    /**
     *Updates the current game timer by one second. Called from UI.
     */
    public void updateTimer() { }
    /**
     *Passes data regarding user interaction and then reacts to that interaction according to the specific game mode. Called from UI.
     * @param e The location and time information regarding a mouse press.
     */
    public void mousePressed(MouseEvent e) { }
    /**
     *Passes data regarding user interaction and then reacts to that interaction according to the specific game mode. Called from UI.
     * @param e The location and time information regarding a mouse release.
     */
    public void mouseReleased(MouseEvent e) { }
    /**
     *Passes data regarding user interaction and then reacts to that interaction according to the specific game mode. Called from UI.
     * @param e The location and time information regarding a mouse movement.
     */
    public void mouseMoved(MouseEvent e) { }

     /**
     * Ends the current game and calls the returnToPlatform() method in the platform. Called from UI.
     */
    public void returnToPlatform() {
        updateScores();
		score = 0;
		time = 0;
		mainMenu.returnToPlatform();
    }

     /**
     * Calls the updateScores() method in the platform and passes the required information regarding score keeping.
     */
    public void updateScores() {
        mainMenu.updateScores(gameName, score, time, false);
    }

    /**
     * Plays the audio file at the selected path.
     * @param audioPath a string containing the path location of the audio file in relation to the project folder.
     */
    protected void playSound(String audioPath) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(audioPath));
            Clip soundEffect = AudioSystem.getClip();
            soundEffect.open(audioStream);
            FloatControl volumeControl = (FloatControl) soundEffect.getControl(FloatControl.Type.MASTER_GAIN);
            int vol = Platform.volume;
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
}
