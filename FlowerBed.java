// created by team-3
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Font;

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

	
	// other components

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
	private FlowerBedCardStack previewCardStack;
	private JEditorPane previewText = new JEditorPane();
	private List<CardHistory> undoStack = new ArrayList<CardHistory>();
	private List<CardHistory> redoStack = new ArrayList<CardHistory>();
	private int lastHintSelected = 0;
	private int lastHintSearched = 0;
	// used for moving single cards
	private FlowerBedCardStack source = null;
	private FlowerBedCardStack dest = null;
	private FlowerBedCardStack focusedStack = null;
	// used for moving a stack of cards
	private FlowerBedCardStack transferStack = new FlowerBedCardStack(false);

	public FlowerBed(JPanel myTable, JFrame myFrame, StartMenu myMenu, String myCardPath, String myBackgroundPath) {
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
		//String text = "Seconds: " + time;
	}

	// BUTTON LISTENERS

	public void startMenu() { 
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
		mainMenu.returnToMenu();
	}

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

		mainMenu.saveGame(cardList);
	}

	public void loadGame() {
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

		List<String> stacks = mainMenu.loadGame();
		
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
			// if playstack, turn next card up
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


		return cardMoved;
	}

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
		}

		return cardMoved;
	}

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

	private boolean validPlayStackMove(Card source, Card dest)
	{
		int s_val = source.getValue().ordinal();
		int d_val = dest.getValue().ordinal();
		//Card.Suit s_suit = source.getSuit();
		//Card.Suit d_suit = dest.getSuit();

		// destination card should be one higher value
		if ((s_val + 1) == d_val)
		{
			return true;
		} else
			return false;
	}

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

	public Card getTop(Vector<Card> stack, Point p) {
		Card c = null;
		for (int x = stack.size() - 1; x >= 0; x--)
		{
			Card temp = (Card) stack.get(x);
			//temp.getSuit();
			if(temp.contains(p)) {
				c = temp;
			}
		}
		return c;
	}

	public void mousePressed(MouseEvent e)
	{
		  

		start = e.getPoint();
		boolean stopSearch = false;
		//statusBox.setText("");
		transferStack.makeEmpty();

		/*
			* Here we use transferStack to temporarily hold all the cards above
			* the selected card in case player wants to move a stack rather
			* than a single card
			*/
		for (int x = 0; x < NUM_PLAY_DECKS; x++)
		{
			if (stopSearch)
				break;
			source = playCardStack[x];
			// pinpointing exact card pressed-
			if(source.contains(start) && source.showSize() > 0) {
				Card c = (Card) source.getStack().get(0);
				if(c.contains(start)) {
					transferStack.putFirst(c);
					card = c;
					stopSearch = true;
					System.out.println("Transfer Size: " + transferStack.showSize());
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
					System.out.println("Transfer Size: " + transferStack.showSize());
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
					System.out.print("moving new card to empty spot ");
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
					System.out.println("-3");
					validMoveMade = true;
					break;
				}
				// this moves stuff from the deck out on the field automatically
				// to populated play stack
				/*if (movedCard != null && dest.contains(stop) && !dest.empty() && dest.getFirst().getFaceStatus()
						&& validPlayStackMove(movedCard, dest.getFirst()))
				{
					System.out.print("moving new card ");
					movedCard.setXY(dest.getFirst().getXY());
					table.remove(prevCard);
					dest.putFirst(movedCard);
					table.repaint();
					movedCard = null;
					putBackOnDeck = false;
					setScore(5);
					validMoveMade = true;
					break;
				}*/
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
						System.out.println("-2");
						validMoveMade = true;
						break;
					}
				}
				if (!dest.empty() && dest.contains(stop) && validFinalStackMove(movedCard, dest.getLast()))
				{
					System.out.println("Destin" + dest.showSize());
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
					System.out.println("-1");
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
					System.out.print("Destination ");
					dest.showSize();
					if (sourceIsFinalDeck) {
						setScore(15);
						undoStack.add(new CardHistory(c, dest, source, 15));
					}
					else {
						setScore(10);
						undoStack.add(new CardHistory(c, dest, source, 10));
					}
					System.out.println("0");
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

					System.out.print("Destination ");
					dest.showSize();
					setScore(5);
					System.out.println("1");
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

							System.out.print("Destination ");
							dest.showSize();
							card = null;
							setScore(10);
							System.out.println("2");
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

						System.out.print("Destination ");
						dest.showSize();
						card = null;
						checkForWin = true;
						setScore(10);
						System.out.println("3");
						validMoveMade = true;
						break;
					}
				}

			}
		}// end cycle through play decks

		// SHOWING STATUS MESSAGE IF MOVE INVALID
		if (!validMoveMade && dest != null && card != null)
		{
			//statusBox.setText("That Is Not A Valid Move");
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
			//JOptionPane.showMessageDialog(table, "Congratulations! You've Won!");
			
			ImageIcon myCard;
			myCard = new ImageIcon(FlowerBed.this.getClass().getResource("Victory/winner.gif"));
			JLabel winnerImage = new JLabel(myCard);
			winnerImage.setBounds(-100, 30, Card.CARD_WIDTH + 1000, Card.CARD_HEIGHT * 4);
			table.add(winnerImage);
			//statusBox.setText("Game Over!");
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

	public void mouseMoved(MouseEvent e) {
		start = e.getPoint();
		boolean stopSearch = false;

		if(focusedStack != null) {
			if(focusedStack.contains(start) && focusedStack.showSize() > 0) {
				if(focusedCard == getTop(focusedStack.getStack(), start)) {
					stopSearch = true;
				}
			}
		}


		for (int x = 0; x < NUM_PLAY_DECKS; x++)
		{
			if (stopSearch)
				break;
			FlowerBedCardStack tempSource = playCardStack[x];
			// pinpointing exact card pressed-
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

		if(stopSearch == false) {
			FlowerBedCardStack tempSource = deck;
			// pinpointing exact card pressed
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


		if(focusedCard != null) {
			previewCard.setSuit(focusedCard.getSuit());
			previewCard.setValue(focusedCard.getValue());
			previewCard.refreshImage();
			previewCard.repaint();
		}
	}

	public void newGame() {
		updateScores();
		playNewGame();
	}

	private void playNewGame()
	{
		score = 0;
		time = 0;
		undoStack.clear();
		redoStack.clear();
		deck = new FlowerBedCardStack(true); // deal 52 cards
		deck.shuffle();
		//table.removeAll();
        
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
			//c.setImage(cardPath);
			playCardStack[x].putFirst(c);

			
                        //here
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

		previewCardStack = new FlowerBedCardStack(false);
		previewCard = new PreviewCard();
		previewCard.setXY(new Point(500, 150));
		previewCard.setFaceup();
		//previewCardStack.v.add(previewCard.setFaceup());
		table.add(previewCard);
		previewText.setText("Card View: ");
		previewText.setFont(new Font("Arial", Font.BOLD, 15));
		previewText.setEditable(false);
		previewText.setOpaque(false);
		previewText.setBounds(1020, 275, 120, 60);
		table.add(previewText);
		previewCard.repaint();

		table.repaint();

		if(GameMode.autoMove) {
			while(tryAutoMove()) { }
		}
	}

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

	public void execute() {
		frame.setSize(TABLE_WIDTH, TABLE_HEIGHT);
        
        playNewGame();
	}
}
