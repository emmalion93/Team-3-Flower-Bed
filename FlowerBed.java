// created by team-3
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;

/**
 * A child class of GameMode. Contains the actual implementation for the Flower Bed version of solitaire. 
 * Methods from the parent class should be overridden to give functionality specific to this game mode.
 * Contains and manages FlowerBedCardStacks and their respective Card objects.
 * @author Emily
 */
public class FlowerBed extends GameMode
{
	// CONSTANTS
	public static final int TABLE_HEIGHT = Card.CARD_HEIGHT * 5;
	public static final int TABLE_WIDTH = (Card.CARD_WIDTH * 12) + 100;
	public static final int NUM_FINAL_DECKS = 4;
	public static final int NUM_PLAY_DECKS = 7;
	public static final Point DECK_POS = new Point(5, 525);
	public static final Point SHOW_POS = new Point(DECK_POS.x + Card.CARD_WIDTH + 5, 5);
	public static final Point FINAL_POS = new Point(SHOW_POS.x + Card.CARD_WIDTH + 650, 35);
    public static final Point PLAY_POS = new Point(5, 35);

	
	// GAMEPLAY STRUCTURES
	private static FlowerBedFinalStack[] final_cards;// Foundation Stacks
	private static FlowerBedCardStack[] playCardStack; // Tableau stacks
	private static FlowerBedCardStack deck; // populated with standard 52 card deck
  
	// CARD MOVEMENT
	private Card prevCard = null;// tracking card for waste stack
	private Card movedCard = null;// card moved from waste stack
	private boolean sourceIsFinalDeck = false;
	private boolean putBackOnDeck = true;// used for waste card recycling
	private boolean checkForWin = false;// should we check if game is over?
	private boolean gameOver = true;// easier to negate this than affirm it
	private Point start = null;// where mouse was clicked
	private Point stop = null;// where mouse was released
	private Card card = null; // card to be moved
	private Card focusedCard = null; // card to be focused
	private PreviewCard previewCard = null; // UI that shows the currently focused card
	private JEditorPane previewText = new JEditorPane(); // UI text above the preview card
	private List<CardHistory> undoStack = new ArrayList<CardHistory>(); // Stack containing the move information for the users previous moves
	private List<CardHistory> redoStack = new ArrayList<CardHistory>(); // Stack containing the move information for the users previous undos, things moved from the undoStack are moved to here
	private int lastHintSelected = 0; // positioning variable for the hint system, identifies the last card stack that was selected for a hint search
	private int lastHintSearched = 0; // positioning variable for the hint system, identifies the last card stack position searched during a hint search
	// used for moving single cards
	private FlowerBedCardStack source = null;
	private FlowerBedCardStack dest = null;
	private FlowerBedCardStack focusedStack = null;
	// used for moving a stack of cards
	private FlowerBedCardStack transferStack = new FlowerBedCardStack(false);

	/**
         * The constructor for the FlowerBed class. Sets the gameName, gameDesc, and gameRules for this game mode. 
         * @param myTable Used to set the table variable.
         * @param myFrame used to set the frame variable.
         * @param myMenu used to set the menu variable.
         * @param myCardPath used to set the cardPath variable.
         * @param myBackgroundPath used to set the backgroundPath variable.
         */
	public FlowerBed(JPanel myTable, JFrame myFrame, Platform myMenu, String myCardPath, String myBackgroundPath) {
		gameName = "Flower Bed";
		gameDesc = "Move cards one at a time onto stacks regardless of suit/color to fill foundations.";
	
		gameRules = "<b>Flower Bed Solitaire Rules</b>"
				+ "<br><br><b><br>How it’s played:<br></b> In this variation of Flower Bed, 35 cards are dealt into seven 5-card columns. The columns are referred to as ‘flower-beds’ and together these ‘flower-beds’ all make up ‘the garden’. The 17 leftover cards below ‘the garden’ are known as ‘the bouquet’. The top four slots are referred to as ‘foundations’ where Ace to King is stacked by suit."
				+ "Cards can only be moved one at a time. The top cards of each flower-bed and all bouquet cards are playable. The foundations are built by suit, Ace to King. However, the flower-beds are built down regardless of suit, and the bouquet cards can be moved into the garden or stacked onto foundations. If a flower-bed opens up in the garden a card of any number or suit may fill the spot. Filling all four foundations, like any solitaire, is how you win the game."
				+ "<b><br><br>Scoring:</b><br><br>"
				+ "10 points are awarded to every successful move whether the card moved from the bouquet to the garden or among the garden or to the foundations. Speed is important, every 10 seconds 2 points are deducted causing a greater penalty for a longer lasting game."
				+ "<b><br><br>Buttons:</b><br><br>"
				+ "The following buttons are viewable at the bottom or top of the screen (depending on your UI settings):"
				+"<br><br>You may display game rules as you please using the show rules button."
				+"<br><br>You may pause and resume the timer as you wish using the timer pause/start timer button."
				+"<br><br>You may also save a state and load as desired using the appropriate buttons."
				+"<br><br>You may start a new game at any time with the new game button."
				+"<br><br>You may return to the main menu at any time with the main menu button.";

		table = myTable;
		frame = myFrame;
		mainMenu = myMenu;
		cardPath = myCardPath;
		backgroundPath = myBackgroundPath;
	}

	// add/subtract points based on gameplay actions
	protected void setScore(int deltaScore)
	{
		score += deltaScore;
	}

	// GAME TIMER UTILITIES
	public void updateTimer()
	{
		time += 1;
		// every 10 seconds elapsed we take away 2 points
		if (time % 10 == 0)
		{
			setScore(-2);
		}
	}

	// BUTTON LISTENERS
	/**
     * Ends the current game and calls the returnToPlatform() method in the platform. Called from UI.
     */
	public void returnToPlatform() { 
		updateScores();
		score = 0;
		time = 0;
		undoStack.clear();
		redoStack.clear();
		deck.makeEmpty();
		for (int x = 0; x < NUM_FINAL_DECKS; x++)
		{
			final_cards[x].makeEmpty();
		}
		
		
		for (int x = 0; x < NUM_PLAY_DECKS; x++)
		{
			playCardStack[x].makeEmpty();
		}
		mainMenu.returnToPlatform();
	}

	/**
     *Gathers information about the current game state and then passes it to the platform to be saved. Called from UI. Stacks are added to a string 
	 * seperated onto different lines, cards within a stack are spereated by ";", and the card information was an individual card is seperated by ",". 
	 * This information once compiled is sent to the platform where it will be written to a save file for this game mode
     */
	public void saveGame() {
		String cardList = "";
		for (int x = 0; x < NUM_PLAY_DECKS; x++)
		{
			Vector<Card> stack = playCardStack[x].getStack();

			for (int y = 0; y < stack.size(); y++)
			{
				Card c = (Card) stack.get(y);
				cardList = cardList + c.getSuit() + "," + c.getValue() + ";";
			}
			if(stack.size() == 0) {
				cardList = cardList + ";";
			}
			cardList = cardList + "\n";
		}
		for (int x = 0; x < NUM_FINAL_DECKS; x++)
		{
			Vector<Card> stack = final_cards[x].getStack();

			for (int y = 0; y < stack.size(); y++)
			{
				Card c = (Card) stack.get(y);
				cardList = cardList + c.getSuit() + "," + c.getValue() + ";";
			}
			if(stack.size() == 0) {
				cardList = cardList + ";";
			}
			cardList = cardList + "\n";
		}
		Vector<Card> stack = deck.reverse().getStack();

		for (int y = 0; y < stack.size(); y++)
		{
			Card c = (Card) stack.get(y);
			cardList = cardList + c.getSuit() + "," + c.getValue() + ";";
		}
		if(stack.size() == 0) {
			cardList = cardList + ";";
		}
		deck.reverse();

		cardList = cardList + "\n" + score + "\n" + time;

		mainMenu.saveGame(gameName, cardList);
	}

	/**
     * Gets information from the platform about a saved game state and then sets the current game state to that saved state. Called from UI. 
	 * Cards within a stack are spereated by ";", and the card information was an individual card is seperated by ",".
	 * If the platform returns an empty list then the player is notified that no load data exsists for this game mode.
     */
	public void loadGame() {
		List<String> stacks = mainMenu.loadGame(gameName);

		if(!stacks.isEmpty()) {

			if (playCardStack != null && final_cards != null)
			{
				for (int x = 0; x < NUM_PLAY_DECKS; x++)
				{
					playCardStack[x].makeEmpty();
				}
				for (int x = 0; x < NUM_FINAL_DECKS; x++)
				{
					final_cards[x].makeEmpty();
				}
			}
			deck.makeEmpty();
			
			for (int x = 0; x < NUM_PLAY_DECKS; x++)
			{
				List<String> cardList = Arrays.asList(stacks.get(x).split(";"));
				for (int y = 0; y < cardList.size(); y++)
				{
					List<String> cardInfo = Arrays.asList(cardList.get(y).split(","));
					Card c = new Card(Card.Suit.valueOf(cardInfo.get(0)), Card.Value.valueOf(cardInfo.get(1)));
					playCardStack[x].push(c.setFaceup());
				}
				playCardStack[x].repaint();
			}
			for (int x = 0; x < NUM_FINAL_DECKS; x++)
			{
				List<String> cardList = Arrays.asList(stacks.get(NUM_PLAY_DECKS + x).split(";"));

				for (int y = 0; y < cardList.size(); y++)
				{
					List<String> cardInfo = Arrays.asList(cardList.get(y).split(","));
					Card c = new Card(Card.Suit.valueOf(cardInfo.get(0)), Card.Value.valueOf(cardInfo.get(1)));
					final_cards[x].push(c.setFaceup());
				}
				final_cards[x].repaint();
			}
			List<String> cardList = Arrays.asList(stacks.get(NUM_PLAY_DECKS + NUM_FINAL_DECKS).split(";"));
			for (int y = 0; y < cardList.size(); y++)
			{
				List<String> cardInfo = Arrays.asList(cardList.get(y).split(","));
				Card c = new Card(Card.Suit.valueOf(cardInfo.get(0)), Card.Value.valueOf(cardInfo.get(1)));
				deck.push(c.setFaceup());
			}
			deck.reverse();

			undoStack.clear();
			redoStack.clear();
			score = Integer.parseInt(stacks.get(NUM_PLAY_DECKS + NUM_FINAL_DECKS + 1));
			time = Integer.parseInt(stacks.get(NUM_PLAY_DECKS + NUM_FINAL_DECKS + 2));
		}
	}

	/**
     * Reverses a previous undo using a stack of CardState objects. Called from UI. Moves that redone are moved back onto the undo stack. The card and its previous/current stack are 
	 * repainted to reflect its current position. Any score changes are redone.
     */
	public void redo() {
		if(redoStack.size() > 0) {
			source =  redoStack.get(redoStack.size() - 1).getLastCardStack();
			card = redoStack.get(redoStack.size() - 1).getCard();
			dest = redoStack.get(redoStack.size() - 1).getCardStack();
			setScore(redoStack.get(redoStack.size() - 1).getScoreValue());

			undoStack.add(redoStack.get(redoStack.size() - 1));
			redoStack.remove(redoStack.size() - 1);

			Card c = card;
			source.removeCard(card);

			c.repaint();

			if (source.getFirst() != null)
			{
				Card temp = source.getFirst().setFaceup();
				temp.repaint();
				source.repaint();
			}

			boolean isFinalDeck = false;
			for (int x = 0; x < NUM_FINAL_DECKS; x++)
			{
				FlowerBedFinalStack finalDeck = final_cards[x];
				if(dest == finalDeck) {
					dest.push(c);
					isFinalDeck = true;
					break;
				}
			}

			if(!isFinalDeck) {
				dest.setXY(dest.getXY().x, dest.getXY().y);
				dest.putFirst(c);
			}


			source.repaint();
			
			table.repaint();

			start = null;
			stop = null;
			source = null;
			dest = null;
			card = null;
			sourceIsFinalDeck = false;
			checkForWin = false;
			gameOver = false;
		}
	}

	/**
     *Reverses a previous move using a stack of CardState objects. Called from UI. Moves that undone are moved onto the redo stack. The card and its previous/current stack are 
	 * repainted to reflect its current position. Any score changes are undone.
     */
	public void undo() {
		if(undoStack.size() > 0) {

			source =  undoStack.get(undoStack.size() - 1).getCardStack();
			card = undoStack.get(undoStack.size() - 1).getCard();
			dest = undoStack.get(undoStack.size() - 1).getLastCardStack();
			setScore(undoStack.get(undoStack.size() - 1).getScoreValue() * -1);
			redoStack.add( undoStack.get(undoStack.size() - 1));
			undoStack.remove(undoStack.size() - 1);

			Card c = card;
			source.removeCard(card);


			c.repaint();
			// if playstack, turn next card up
			if (source.getFirst() != null)
			{
				Card temp = source.getFirst().setFaceup();
				temp.repaint();
				source.repaint();
			}

			dest.setXY(dest.getXY().x, dest.getXY().y);
			dest.putFirst(c);

			source.repaint();
			
			table.repaint();

			start = null;
			stop = null;
			source = null;
			dest = null;
			card = null;
			sourceIsFinalDeck = false;
			checkForWin = false;
			gameOver = false;
		}
	}

	/**
     * Selects a card that can still be moved and then moves it using a legal move. Determines movable card by looping through all available cards and checking them all stacks for legal moves.
	 * The last previously searched available stack and the last previously searched stack for a legal move determine the starting location for the next time hint is used to prevent getting 
	 * stuck in a loop.
     */
	public void hint() {
		boolean cardMoved = false;
		if(tryAutoMove()) {
			cardMoved = true;
			if(GameMode.autoMove) {
				while(tryAutoMove()) { }
			}
		}

		if(!cardMoved) {
			for (int y = lastHintSelected; y < NUM_PLAY_DECKS; y++)
			{
				if(hintLoop(y)) {
					cardMoved = true;
					break;
				}
			}

			if(!cardMoved) {
				if(lastHintSelected >= NUM_PLAY_DECKS) {
					lastHintSelected = NUM_PLAY_DECKS;
				}
				for (int y = 0; y < lastHintSelected - 1; y++)
				{
					if(hintLoop(y)) {
						cardMoved = true;
						break;
					}
				}
			}
		}
	}

	/**
	 * Checks through all available cards to determine if any of them can be moved onto the final stack foundations. If so, moves that card and returns true.
	 * @return cardMoved
	 */
	private boolean tryAutoMove() {
		boolean cardMoved = false;
		for (int y = 0; y < NUM_FINAL_DECKS; y++)
		{
			FlowerBedCardStack tempYStack = final_cards[y];
			Card cStacking = (Card) tempYStack.getLast();

			for (int x = 0; x < NUM_PLAY_DECKS; x++)
			{
				FlowerBedCardStack tempXStack = playCardStack[x];
				if(tempXStack.v.size() == 0) {
					continue;
				}
				Card cMoving = (Card) tempXStack.getFirst();

				if(hintLoopDeck(cMoving, cStacking, tempXStack, tempYStack)) {
					cardMoved = true;
					break;
				}
			}
			if(cardMoved) {
				break;
			}

			for (int x = 0; x < deck.v.size(); x++)
			{
				FlowerBedCardStack tempXStack = deck;
				if(tempXStack.v.size() == 0) {
					continue;
				}
				Card cMoving = (Card) deck.v.get(x);

				if(hintLoopDeck(cMoving, cStacking, tempXStack, tempYStack)) {
					cardMoved = true;
					break;
				}
			}
			if(cardMoved) {
				break;
			}
		}

		if(cardMoved) {
			boolean gameNotOver = false;
			// cycle through final decks, if they're all full then game over
			for (int x = 0; x < NUM_FINAL_DECKS; x++)
			{
				dest = final_cards[x];
				if (dest.showSize() != 13)
				{
					// one deck is not full, so game is not over
					gameNotOver = true;
					break;
				}
			}

			if (!gameNotOver)
			{
				mainMenu.updateScores(gameName, score, time, true);
				
				undoStack.clear();
				redoStack.clear();
				ImageIcon myCard;
				myCard = new ImageIcon(FlowerBed.this.getClass().getResource("Victory/winner.gif"));
				JLabel winnerImage = new JLabel(myCard);
				winnerImage.setBounds(-100, 30, Card.CARD_WIDTH + 1000, Card.CARD_HEIGHT * 4);
				table.add(winnerImage);
			}
		}


		return cardMoved;
	}

	/**
	 * Checks if individual card can be automatically moved onto the final foundation and if so, moves it, repaints the previous/current stack, and returns true.
	 * @param cMoving
	 * @param cStacking
	 * @param tempXStack
	 * @param tempYStack
	 * @return cardMoved
	 */
	private boolean hintLoopDeck(Card cMoving, Card cStacking, FlowerBedCardStack tempXStack, FlowerBedCardStack tempYStack) {
		boolean cardMoved = false;
		if(tempYStack.empty()) {
			if (cMoving.getValue() == Card.Value.ACE)
			{
				source =  tempXStack;
				card = cMoving;
				dest = tempYStack;
				undoStack.add(new CardHistory(card, dest, source, 0));
				redoStack.clear();

				Card c = card;
				source.removeCard(card);


				c.repaint();
				// if playstack, turn next card up
				if (source.getFirst() != null)
				{
					Card temp = source.getFirst().setFaceup();
					temp.repaint();
					source.repaint();
				}

				dest.setXY(dest.getXY().x, dest.getXY().y);
				dest.push(c);

				source.repaint();
				
				table.repaint();

				start = null;
				stop = null;
				source = null;
				dest = null;
				card = null;
				sourceIsFinalDeck = false;
				checkForWin = false;
				gameOver = false;
				cardMoved = true;
			}
		} else {
			if(validFinalStackMove(cMoving, cStacking)) {
				source =  tempXStack;
				card = cMoving;
				dest = tempYStack;
				undoStack.add(new CardHistory(card, dest, source, 0));
				redoStack.clear();

				Card c = card;
				source.removeCard(card);


				c.repaint();
				if (source.getFirst() != null)
				{
					Card temp = source.getFirst().setFaceup();
					temp.repaint();
					source.repaint();
				}

				dest.setXY(dest.getXY().x, dest.getXY().y);
				dest.push(c);

				source.repaint();
				
				table.repaint();

				start = null;
				stop = null;
				source = null;
				dest = null;
				card = null;
				sourceIsFinalDeck = false;
				checkForWin = false;
				gameOver = false;
				cardMoved = true;
			}
		}

		return cardMoved;
	}

	/**
	 * Loops through all available cards and all available moves and checks if they are a legal move. If so, moves them and returns true.
	 * @param y
	 * @return cardMoved
	 */
	private boolean hintLoop(int y) {
		boolean cardMoved = false;
		FlowerBedCardStack tempYStack = playCardStack[y];
		Card cStacking = (Card) tempYStack.getFirst();

		for (int x = lastHintSearched; x < NUM_PLAY_DECKS; x++)
		{
			FlowerBedCardStack tempXStack = playCardStack[x];
			if(tempXStack.v.size() == 0) {
				continue;
			}
			Card cMoving = (Card) tempXStack.getFirst();

			if(hintTryMoveCard(cMoving, cStacking, tempXStack, tempYStack, x, y)) {
				cardMoved = true;
				break;
			}
		}
		if(cardMoved) {
			return cardMoved;
		}


		for (int x = lastHintSearched - NUM_PLAY_DECKS; x < deck.v.size(); x++)
		{
			if(x < 0) {
				x = 0;
			}
			FlowerBedCardStack tempXStack = deck;
			if(tempXStack.v.size() == 0) {
				continue;
			}
			Card cMoving = (Card) deck.v.get(x);

			if(hintTryMoveCard(cMoving, cStacking, tempXStack, tempYStack, x + NUM_PLAY_DECKS, y)) {
				cardMoved = true;
				break;
			}
		}
		if(cardMoved) {
			return cardMoved;
		}



		
		//1st 2nd loop
		for (int x = 0; x < lastHintSearched; x++)
		{
			if(x >= NUM_PLAY_DECKS) {
				break;
			}
			FlowerBedCardStack tempXStack = playCardStack[x];
			if(tempXStack.v.size() == 0) {
				continue;
			}
			Card cMoving = (Card) tempXStack.getFirst();

			if(hintTryMoveCard(cMoving, cStacking, tempXStack, tempYStack, x, y)) {
				cardMoved = true;
				break;
			}
		}
		if(cardMoved) {
			return cardMoved;
		}

		for (int x = 0; x < lastHintSearched - NUM_PLAY_DECKS; x++)
		{
			if(x >= deck.v.size()) {
				break;
			}

			FlowerBedCardStack tempXStack = deck;
			if(tempXStack.v.size() == 0) {
				continue;
			}
			Card cMoving = (Card) deck.v.get(x);

			if(hintTryMoveCard(cMoving, cStacking, tempXStack, tempYStack, x + NUM_PLAY_DECKS, y)) {
				cardMoved = true;
				break;
			}
		}
		if(cardMoved) {
			return cardMoved;
		}

		return cardMoved;
	}

	/**
	 * checks if a particular move is a legal move and if so moves the card, repaints the current/previous stack, and returns true.
	 * @param y
	 * @return cardMoved
	 */
	private boolean hintTryMoveCard(Card cMoving, Card cStacking, FlowerBedCardStack tempXStack, FlowerBedCardStack tempYStack, int x, int y) {
		boolean cardMoved = false;

		if(tempYStack.empty()) {
			source =  tempXStack;
			card = cMoving;
			dest = tempYStack;
			undoStack.add(new CardHistory(card, dest, source, 0));
			redoStack.clear();

			Card c = card;
			source.removeCard(card);


			c.repaint();
			// if playstack, turn next card up
			if (source.getFirst() != null)
			{
				Card temp = source.getFirst().setFaceup();
				temp.repaint();
				source.repaint();
			}

			dest.setXY(dest.getXY().x, dest.getXY().y);
			dest.push(c);

			source.repaint();
			
			table.repaint();

			start = null;
			stop = null;
			source = null;
			dest = null;
			card = null;
			sourceIsFinalDeck = false;
			checkForWin = false;
			gameOver = false;

			cardMoved = true;
			lastHintSelected = y;
			lastHintSearched = x;
		} else {
			if(validPlayStackMove(cMoving, cStacking)) {
				source =  tempXStack;
				card = cMoving;
				dest = tempYStack;
				undoStack.add(new CardHistory(card, dest, source, 0));
				redoStack.clear();

				Card c = card;
				source.removeCard(card);


				c.repaint();
				// if playstack, turn next card up
				if (source.getFirst() != null)
				{
					Card temp = source.getFirst().setFaceup();
					temp.repaint();
					source.repaint();
				}

				dest.setXY(dest.getXY().x, dest.getXY().y);
				dest.putFirst(c);

				source.repaint();
				
				table.repaint();

				start = null;
				stop = null;
				source = null;
				dest = null;
				card = null;
				sourceIsFinalDeck = false;
				checkForWin = false;
				gameOver = false;

				cardMoved = true;
				lastHintSelected = y;
				lastHintSearched = x;
			}
		}

		return cardMoved;
	}

	/**
	 * Checks if a card can legally be moved to a specific stack based on its value and suit. If so returns true
	 * @param source the selected card
	 * @param dest the card it is attempted to be stacked on
	 * @return true if legal move
	 */
	private boolean validPlayStackMove(Card source, Card dest)
	{
		int s_val = source.getValue().ordinal();
		int d_val = dest.getValue().ordinal();


		// destination card should be one higher value
		if ((s_val + 1) == d_val)
		{
			return true;
		} else
			return false;
	}

	/**
	 * Checks if a card can legally be moved to a specific foundation stack based on its value and suit. If so returns true
	 * @param source the selected card
	 * @param dest the card it is attempted to be stacked on
	 * @return true if legal move
	 */
	private boolean validFinalStackMove(Card source, Card dest)
	{
		int s_val = source.getValue().ordinal();
		int d_val = dest.getValue().ordinal();
		Card.Suit s_suit = source.getSuit();
		Card.Suit d_suit = dest.getSuit();
		if (s_val == (d_val + 1)) // destination must one lower
		{
			if (s_suit == d_suit)
				return true;
			else
				return false;
		} else
			return false;
	}

	/**
	 * Determines which card is the top most card at the mouse poisition in case of cards that are closely stacked together.
	 * @param stack
	 * @param p
	 * @return the top most card
	 */
	public Card getTop(Vector<Card> stack, Point p) {
		Card c = null;
		for (int x = stack.size() - 1; x >= 0; x--)
		{
			Card temp = (Card) stack.get(x);
			if(temp.contains(p)) {
				c = temp;
			}
		}
		return c;
	}

	/**
     * Determines which card, if any, the mouse has been pressed on and then holds that card until the mouse is released. Called from UI.
     * @param e The location and time information regarding a mouse press.
     */
	public void mousePressed(MouseEvent e)
	{
		  

		start = e.getPoint();
		boolean stopSearch = false;
		transferStack.makeEmpty();

		/*
		* Here we determine which stack the mouse is over and the top most card in that stack that the mouse is over
		*/
		for (int x = 0; x < NUM_PLAY_DECKS; x++)
		{
			if (stopSearch)
				break;
			source = playCardStack[x];
			// pinpointing exact card pressed
			if(source.contains(start) && source.showSize() > 0) {
				Card c = (Card) source.getStack().get(0);
				if(c.contains(start)) {
					transferStack.putFirst(c);
					card = c;
					stopSearch = true;
				}
			}
		}

		if(card == null) {
			source = deck;
			// pinpointing exact card pressed
			Vector<Card> stack = source.getStack();
			for (int x = 0; x < source.showSize(); x++)
			{
				transferStack.makeEmpty();
				Card c = (Card) stack.get(x);
				if(source.contains(start) && c == getTop(source.getStack(), start)) {
					transferStack.putFirst(c);
					card = c;
					stopSearch = true;
					break;
				}                                    
			}
		}
					
		
		

		// FINAL (FOUNDATION) CARD OPERATIONS
		for (int x = 0; x < NUM_FINAL_DECKS; x++)
		{
			if (final_cards[x].contains(start))
			{
				source = final_cards[x];
				card = source.getLast();
				transferStack.putFirst(card);
				sourceIsFinalDeck = true;
				break;
			}
		}
		putBackOnDeck = true;

	}

	/**
     * Determines which stack, if any, the mouse has been released on and if it is a stack the selected card can be legally moved onto then it moves the card and repaints both stacks. 
	 * Called from UI.
     * @param e The location and time information regarding a mouse release.
     */
	public void mouseReleased(MouseEvent e)
	{
		stop = e.getPoint();
		// used for status bar updates
		boolean validMoveMade = false;
		// SHOW CARD MOVEMENTS
		if (movedCard != null)
		{
			// Moving from SHOW TO PLAY
			for (int x = 0; x < NUM_PLAY_DECKS; x++)
			{
				dest = playCardStack[x];
				// to empty play stack, only kings can go
				if (dest.empty() && movedCard != null && dest.contains(stop)
						&& movedCard.getValue() == Card.Value.KING)
				{
					movedCard.setXY(dest.getXY());
					table.remove(prevCard);
					dest.putFirst(movedCard);
					undoStack.add(new CardHistory(movedCard, dest, source, 5));
					redoStack.clear();

					table.repaint();
					movedCard = null;
					putBackOnDeck = false;
					if(putBackOnDeck) { 
						//TODO Remove this 
					}
					setScore(5);
					validMoveMade = true;
					break;
				}
			}
			// Moving from SHOW TO FINAL
			for (int x = 0; x < NUM_FINAL_DECKS; x++)
			{
				dest = final_cards[x];
				// only aces can go first
				if (dest.empty() && dest.contains(stop))
				{
					if (movedCard.getValue() == Card.Value.ACE)
					{
						dest.push(movedCard);
						table.remove(prevCard);
						undoStack.add(new CardHistory(movedCard, dest, source, 10));
						redoStack.clear();

						dest.repaint();
						table.repaint();
						movedCard = null;
						putBackOnDeck = false;
						setScore(10);
						validMoveMade = true;
						break;
					}
				}
				if (!dest.empty() && dest.contains(stop) && validFinalStackMove(movedCard, dest.getLast()))
				{
					dest.push(movedCard);
					table.remove(prevCard);
					undoStack.add(new CardHistory(movedCard, dest, source, 10));
					redoStack.clear();

					dest.repaint();
					table.repaint();
					movedCard = null;
					putBackOnDeck = false;
					checkForWin = true;
					setScore(10);
					validMoveMade = true;
					break;
				}
			}
		}// END SHOW STACK OPERATIONS

		// PLAY STACK OPERATIONS
		if (card != null && source != null)
		{ // Moving from PLAY TO PLAY
			for (int x = 0; x < NUM_PLAY_DECKS; x++)
			{
				dest = playCardStack[x];
				// MOVING TO POPULATED STACK
				if (card.getFaceStatus() == true && dest.contains(stop) && source != dest && !dest.empty()
						&& validPlayStackMove(card, dest.getFirst()) && transferStack.showSize() == 1)
				{
					Card c = card;
					source.removeCard(card);


					c.repaint();
					// if playstack, turn next card up
					if (source.getFirst() != null)
					{
						Card temp = source.getFirst().setFaceup();
						temp.repaint();
						source.repaint();
					}

					dest.setXY(dest.getXY().x, dest.getXY().y);
					dest.putFirst(c);

					dest.repaint();
					
					redoStack.clear();

					table.repaint();
					dest.showSize();
					if (sourceIsFinalDeck) {
						setScore(15);
						undoStack.add(new CardHistory(c, dest, source, 15));
					}
					else {
						setScore(10);
						undoStack.add(new CardHistory(c, dest, source, 10));
					}
					validMoveMade = true;
					break;
				} else if (dest.empty() && transferStack.showSize() == 1 && dest.contains(stop))
				{// MOVING TO EMPTY STACK, ONLY KING ALLOWED
					Card c = card;
					source.removeCard(card);

					c.repaint();
					// if playstack, turn next card up
					if (source.getFirst() != null)
					{
						Card temp = source.getFirst().setFaceup();
						temp.repaint();
						source.repaint();
					}

					dest.setXY(dest.getXY().x, dest.getXY().y);
					dest.putFirst(c);

					dest.repaint();

					undoStack.add(new CardHistory(c, dest, source, 5));
					redoStack.clear();

					table.repaint();

					dest.showSize();
					setScore(5);
					validMoveMade = true;
					break;
				}
			}
			// from PLAY TO FINAL
			for (int x = 0; x < NUM_FINAL_DECKS; x++)
			{
				dest = final_cards[x];

				if (card.getFaceStatus() == true && source != null && dest.contains(stop) && source != dest)
				{// TO EMPTY STACK
					if (dest.empty())// empty final should only take an ACE
					{
						if (card.getValue() == Card.Value.ACE)
						{
							Card c = card;
							source.removeCard(card);
							c.repaint();
							if (source.getFirst() != null)
							{

								Card temp = source.getFirst().setFaceup();
								temp.repaint();
								source.repaint();
							}

							dest.setXY(dest.getXY().x, dest.getXY().y);
							dest.push(c);

							dest.repaint();

							undoStack.add(new CardHistory(c, dest, source, 10));
							redoStack.clear();

							table.repaint();

							dest.showSize();
							card = null;
							setScore(10);
							validMoveMade = true;
							break;
						}// TO POPULATED STACK
					} else if (validFinalStackMove(card, dest.getLast()))
					{
						Card c = card;
						source.removeCard(card);
						c.repaint();
						if (source.getFirst() != null)
						{

							Card temp = source.getFirst().setFaceup();
							temp.repaint();
							source.repaint();
						}

						dest.setXY(dest.getXY().x, dest.getXY().y);
						dest.push(c);

						dest.repaint();

						undoStack.add(new CardHistory(c, dest, source, 10));
						redoStack.clear();

						table.repaint();

						dest.showSize();
						card = null;
						checkForWin = true;
						setScore(10);
						validMoveMade = true;
						break;
					}
				}

			}
		}// end cycle through play decks

		// SHOWING STATUS MESSAGE IF MOVE INVALID
		if (!validMoveMade && dest != null && card != null)
		{
		} else if(validMoveMade) {
			playSound("Sounds/mixkit-poker-card-flick-2002.wav");
			if(GameMode.autoMove) {
				while(tryAutoMove()) { }
			}
		}
		// CHECKING FOR WIN
		if (checkForWin)
		{
			boolean gameNotOver = false;
			// cycle through final decks, if they're all full then game over
			for (int x = 0; x < NUM_FINAL_DECKS; x++)
			{
				dest = final_cards[x];
				if (dest.showSize() != 13)
				{
					// one deck is not full, so game is not over
					gameNotOver = true;
					break;
				}
			}
			if (!gameNotOver) {
				gameOver = true;
			} else {
				gameOver = false;
			}
		}

		if (checkForWin && gameOver)
		{
			mainMenu.updateScores(gameName, score, time, true);
			
			undoStack.clear();
			redoStack.clear();
			ImageIcon myCard;
			myCard = new ImageIcon(FlowerBed.this.getClass().getResource("Victory/winner.gif"));
			JLabel winnerImage = new JLabel(myCard);
			winnerImage.setBounds(-100, 30, Card.CARD_WIDTH + 1000, Card.CARD_HEIGHT * 4);
			table.add(winnerImage);
		}
		// RESET VARIABLES FOR NEXT EVENT
		start = null;
		stop = null;
		source = null;
		dest = null;
		card = null;
		sourceIsFinalDeck = false;
		checkForWin = false;
		gameOver = false;
	}// end mousePressed()

	/**
     * Determines if the mouse has been moved over a card and if so it changes the preview card UI to show a blown up version of that card. Called from UI.
     * @param e The location and time information regarding a mouse movement.
     */
	public void mouseMoved(MouseEvent e) {
		start = e.getPoint();
		boolean stopSearch = false;

		// If a card is already being focused on then check it first to see if the mouse is still over it. If so then dont check any other cards
		if(focusedStack != null) {
			if(focusedStack.contains(start) && focusedStack.showSize() > 0) {
				if(focusedCard == getTop(focusedStack.getStack(), start)) {
					stopSearch = true;
				}
			}
		}


		// Then check all the regular play stacks 
		for (int x = 0; x < NUM_PLAY_DECKS; x++)
		{
			if (stopSearch)
				break;
			FlowerBedCardStack tempSource = playCardStack[x];
			// pinpointing exact card
			if(tempSource.contains(start) && tempSource.showSize() > 0) {
				for (int y = 0; y < tempSource.showSize(); y++)
				{
					Card c = (Card) tempSource.getStack().get(y);
					if(c == getTop(tempSource.getStack(), start)) {
						focusedStack = tempSource;
						focusedCard = c;
						stopSearch = true;
					}
				}
			}
		}

		// check all the cards in the players hand
		if(stopSearch == false) {
			FlowerBedCardStack tempSource = deck;
			// pinpointing exact card
			Vector<Card> stack = tempSource.getStack();
			for (int x = 0; x < tempSource.showSize(); x++)
			{
				Card c = (Card) stack.get(x);
				if(tempSource.contains(start) && c == getTop(tempSource.getStack(), start)) {
					focusedStack = tempSource;
					focusedCard = c;
					stopSearch = true;
					break;
				}                                    
			}
		}
					
		// check all the final foundations
		for (int x = 0; x < NUM_FINAL_DECKS; x++)
		{
			if (final_cards[x].contains(start))
			{
				focusedStack = final_cards[x];
				focusedCard = final_cards[x].getLast();
				stopSearch = true;
				break;
			}
		}

		// if the mouse is over a card then set the preview card to it
		if(focusedCard != null) {
			previewCard.setSuit(focusedCard.getSuit());
			previewCard.setValue(focusedCard.getValue());
			previewCard.refreshImage();
			previewCard.repaint();
		}
	}

	/**
     * Updates the stored stats based on the current game and then begins a new game of this game mode. Called from UI.
     */
	public void newGame() {
		updateScores();
		playNewGame();
	}

	/**
	 * Clears the previous scores, times, and all stacks and then deals a new game.
	 */
	private void playNewGame()
	{
		score = 0;
		time = 0;
		undoStack.clear();
		redoStack.clear();
		deck = new FlowerBedCardStack(true); // deal 52 cards
		deck.shuffle();
        
		// reset stacks if user starts a new game in the middle of one
		if (playCardStack != null && final_cards != null)
		{
			for (int x = 0; x < NUM_PLAY_DECKS; x++)
			{
				playCardStack[x].makeEmpty();
			}
			for (int x = 0; x < NUM_FINAL_DECKS; x++)
			{
				final_cards[x].makeEmpty();
			}
		}
		// initialize & place final (foundation) decks/stacks
		final_cards = new FlowerBedFinalStack[NUM_FINAL_DECKS];
		for (int x = 0; x < NUM_FINAL_DECKS; x++)
		{
			final_cards[x] = new FlowerBedFinalStack();

			final_cards[x].setXY((FINAL_POS.x + (x * Card.CARD_WIDTH)) + 10, FINAL_POS.y);
			table.add(final_cards[x]);

		}
		// place new card distribution button
		// initialize & place play (tableau) decks/stacks
		playCardStack = new FlowerBedCardStack[NUM_PLAY_DECKS];
		for (int x = 0; x < NUM_PLAY_DECKS; x++)
		{
			playCardStack[x] = new FlowerBedCardStack(false);
            playCardStack[x].setXY((DECK_POS.x + (x * (Card.CARD_WIDTH + 10))), PLAY_POS.y);

			table.add(playCardStack[x]);
		}

		// Dealing new game
		for (int x = 0; x < NUM_PLAY_DECKS; x++)
		{
			Card c = deck.pop().setFaceup();
			playCardStack[x].putFirst(c);

			
            for (int y = 0; y < 4; y++)
			{
				c = deck.pop().setFaceup();
				playCardStack[x].push(c);
			}
		}

        deck.setXY(DECK_POS.x, DECK_POS.y);
        table.add(deck);

		Vector<Card> stack = deck.getStack();
        for (int x = 0; x < deck.showSize(); x++)
		{
            Card c = (Card) stack.get(x);
			c.setFaceup();
		}


		// reset time
		time = 0;

		previewCard = new PreviewCard();
		previewCard.setXY(new Point(500, 150));
		previewCard.setFaceup();
		table.add(previewCard);
		previewText.setText("Card View: ");
		previewText.setFont(new Font("Arial", Font.BOLD, 15));
		previewText.setEditable(false);
		previewText.setOpaque(false);
		previewText.setBounds(1020, 275, 120, 60);
		table.add(previewText);
		previewCard.repaint();

		table.repaint();

		// if the user has automove enabled then move all cards that can be moved to the final foundations
		if(GameMode.autoMove) {
			while(tryAutoMove()) { }
		}
	}

	/**
	 * Tells all cards to Reset their card file paths and repaint when the card background has changed.
	 */
	public void refreshCards() {
		for (int x = 0; x < NUM_FINAL_DECKS; x++)
		{
			for(int y = 0; y < final_cards[x].v.size(); y++)
			{
				Card c = (Card)final_cards[x].v.get(y);
				c.refreshImage();
			}
			final_cards[x].repaint();
		}
		
		
		for (int x = 0; x < NUM_PLAY_DECKS; x++)
		{
			for(int y = 0; y < playCardStack[x].v.size(); y++)
			{
				Card c = (Card)playCardStack[x].v.get(y);
				c.refreshImage();
			}
			playCardStack[x].repaint();
		}

		for (int x = 0; x < deck.v.size(); x++)
		{
			Card c = (Card)deck.v.get(x);
			c.refreshImage();
		}
		deck.repaint();
	}

	/**
	 * Initializes this game mode when the platform selects it as the current game mode.
	 */
	public void execute() {
		frame.setSize(TABLE_WIDTH, TABLE_HEIGHT);
        
        playNewGame();
	}
}
