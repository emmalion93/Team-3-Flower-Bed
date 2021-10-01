import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.event.MouseEvent;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
public class GameMode {
    // GUI COMPONENTS (top level)
	protected JFrame frame;
	protected JPanel table;

    // Game information Variables
	protected String gameName = "";
	protected String gameDesc = "";
    protected String gameRules = "No Rules Provided"; 
    protected StartMenu mainMenu;
    protected int time;
    protected int score;
    public static String cardPath = "CardImages/greywyvern-cardset/";
    public static String backgroundPath = "CardImages/greywyvern-cardset/";

    public String getName() { return gameName; }
	public String getDesc() { return gameDesc; }
    public String getRules() { return gameRules; }

    public void execute() { }

    public void newGame() { JOptionPane.showMessageDialog(table, "This functionality is not enabled for this game mode"); }
    public void saveGame() { JOptionPane.showMessageDialog(table, "This functionality is not enabled for this game mode"); }
    public void loadGame() { JOptionPane.showMessageDialog(table, "This functionality is not enabled for this game mode"); }
    public void refreshCards() {  }
    public void undo() { JOptionPane.showMessageDialog(table, "This functionality is not enabled for this game mode"); }
    public void redo() { JOptionPane.showMessageDialog(table, "This functionality isnot enabled for this game mode"); }
    public void updateTimer() { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseMoved(MouseEvent e) { }

    /**
     * testing this
     */
    public void startMenu() {
        updateScores();
		score = 0;
		time = 0;
		mainMenu.returnToMenu();
     }
    public void updateScores() {
        mainMenu.updateScores(gameName, score, time, false);
    }

    protected void playSound(String audioPath) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(this.getClass().getResource(audioPath));
            Clip soundEffect = AudioSystem.getClip();
            soundEffect.open(audioStream);
            FloatControl volumeControl = (FloatControl) soundEffect.getControl(FloatControl.Type.MASTER_GAIN);
            int vol = StartMenu.volume;
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
