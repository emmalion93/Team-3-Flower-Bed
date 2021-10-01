// created by team-3
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
	private List<CardHistory> undoStack = new ArrayList<CardHistory>();
	private List<CardHistory> redoStack = new ArrayList<CardHistory>();
	// used for moving single cards
	private FlowerBedCardStack source = null;
	private FlowerBedCardStack dest = null;
	// used for moving a stack of cards
	private FlowerBedCardStack transferStack = new FlowerBedCardStack(false);

	public FlowerBed(JPanel myTable, JFrame myFrame, StartMenu myMenu, String myCardPath, String myBackgroundPath) {
		gameName = "Flower Bed";
		gameDesc = "Move cards one at a time onto stacks regardless of suit/color to fill foundations.";
	
		gameRules = "<b>Klondike Solitaire Rules</b>"
				+ "<br><br> (From Wikipedia) Taking a shuffled standard 52-card deck of playing cards (without Jokers),"
				+ " one upturned card is dealt on the left of the playing area, then six downturned cards"
				+ " (from left to right).<p> On top of the downturned cards, an upturned card is dealt on the "
				+ "left-most downturned pile, and downturned cards on the rest until all piles have an "
				+ "upturned card. The piles should look like the figure to the right.<p>The four foundations "
				+ "(light rectangles in the upper right of the figure) are built up by suit from Ace "
				+ "(low in this game) to King, and the tableau piles can be built down by alternate colors,"
				+ " and partial or complete piles can be moved if they are built down by alternate colors also. "
				+ "Any empty piles can be filled with a King or a pile of cards with a King.<p> The point of "
				+ "the game is to build up a stack of cards starting with 2 and ending with King, all of "
				+ "the same suit. Once this is accomplished, the goal is to move this to a foundation, "
				+ "where the player has previously placed the Ace of that suit. Once the player has done this, "
				+ "they will have \"finished\" that suit- the goal being, of course, to finish all suits, "
				+ "at which time the player will have won.<br><br><b> Scoring </b><br><br>"
				+ "Moving cards directly from the Waste stack to a Foundation awards 10 points. However, "
				+ "if the card is first moved to a Tableau, and then to a Foundation, then an extra 5 points "
				+ "are received for a total of 15. Thus in order to receive a maximum score, no cards should be moved "
				+ "directly from the Waste to Foundation.<p>	Time can also play a factor in Windows Solitaire, if the Timed game option is selected. For every 10 seconds of play, 2 points are taken away."
				+ "<b><br><br>Notes On My Implementation</b><br><br>"
				+ "Drag cards to and from any stack. As long as the move is valid the card, or stack of "
				+ "cards, will be repositioned in the desired spot. The game follows the standard scoring and time"
				+ " model explained above with only one waste card shown at a time."
				+ "<p> The timer starts running as soon as "
				+ "the game begins, but it may be paused by pressing the pause button at the bottom of"
				+ "the screen. ";

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
			temp.getSuit();
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

					undoStack.add(new CardHistory(movedCard, dest, source));
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

						undoStack.add(new CardHistory(movedCard, dest, source));
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

					undoStack.add(new CardHistory(movedCard, dest, source));
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
					
					undoStack.add(new CardHistory(c, dest, source));
					redoStack.clear();

					table.repaint();

					System.out.print("Destination ");
					dest.showSize();
					if (sourceIsFinalDeck)
						setScore(15);
					else
						setScore(10);
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

					undoStack.add(new CardHistory(c, dest, source));
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

							undoStack.add(new CardHistory(c, dest, source));
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

						undoStack.add(new CardHistory(c, dest, source));
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
			JOptionPane.showMessageDialog(table, "Congratulations! You've Won!");
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
    
		table.repaint();
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
