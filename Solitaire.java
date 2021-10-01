// created by team-3
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Solitaire extends GameMode
{
	// CONSTANTS
	public static final int TABLE_HEIGHT =  Card.CARD_HEIGHT * 5;
	public static final int TABLE_WIDTH = (Card.CARD_WIDTH * 12) + 100;
	public static final int NUM_FINAL_DECKS = 4;
	public static final int NUM_PLAY_DECKS = 7;
	public static final Point DECK_POS = new Point(5, 35);
	public static final Point SHOW_POS = new Point(DECK_POS.x + Card.CARD_WIDTH + 5, DECK_POS.y);
	public static final Point FINAL_POS = new Point(SHOW_POS.x + Card.CARD_WIDTH + 150, DECK_POS.y);
	public static final Point PLAY_POS = new Point(DECK_POS.x, FINAL_POS.y + Card.CARD_HEIGHT + 30);

	// GAMEPLAY STRUCTURES
	private static FinalStack[] final_cards;// Foundation Stacks
	private static CardStack[] playCardStack; // Tableau stacks
	private static final Card newCardPlace = new Card();// waste card spot
	private static CardStack deck; // populated with standard 52 card deck

	
	// other components
	private static JTextField scoreBox = new JTextField();// displays the score
	private static JTextField timeBox = new JTextField();// displays the time
	private static JTextField statusBox = new JTextField();// status messages
	private static final Card newCardButton = new Card();// reveal waste card



	// MOUSE CONTROL
	private Card prevCard = null;// tracking card for waste stack
	private Card movedCard = null;// card moved from waste stack
	private boolean sourceIsFinalDeck = false;
	private boolean putBackOnDeck = true;// used for waste card recycling
	private boolean checkForWin = false;// should we check if game is over?
	private boolean gameOver = true;// easier to negate this than affirm it
	private Point start = null;// where mouse was clicked
	private Point stop = null;// where mouse was released
	private Card card = null; // card to be moved
	// used for moving single cards
	private CardStack source = null;
	private CardStack dest = null;
	// used for moving a stack of cards
	private CardStack transferStack = new CardStack(false);


	public Solitaire(JPanel myTable, JFrame myFrame, StartMenu myMenu, String myCardPath, String myBackgroundPath) {
		gameName = "Klondike";
		gameDesc = "Traditional Solitaire";
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

	public void startMenu() { 
		updateScores();
		score = 0;
		time = 0;
		mainMenu.returnToMenu();

	}


	// moves a card to abs location within a component
	protected static Card moveCard(Card c, int x, int y)
	{
		c.setBounds(new Rectangle(new Point(x, y), new Dimension(Card.CARD_WIDTH + 10, Card.CARD_HEIGHT + 10)));
		c.setXY(new Point(x, y));
		return c;
	}

	// add/subtract points based on gameplay actions
	protected void setScore(int deltaScore)
	{
		score += deltaScore;
		String newScore = "Score: " + score;
		scoreBox.setText(newScore);
		scoreBox.repaint();
	}

	// GAME TIMER UTILITIES
	public  void updateTimer()
	{
		time += 1;
		// every 10 seconds elapsed we take away 2 points
		if (time % 10 == 0)
		{
			setScore(-2);
		}
		String timeString = "Seconds: " + time;
		timeBox.setText(timeString);
		timeBox.repaint();
	}

	
	private boolean validPlayStackMove(Card source, Card dest)
	{
		int s_val = source.getValue().ordinal();
		int d_val = dest.getValue().ordinal();
		Card.Suit s_suit = source.getSuit();
		Card.Suit d_suit = dest.getSuit();

		// destination card should be one higher value
		if ((s_val + 1) == d_val)
		{
			// destination card should be opposite color
			switch (s_suit)
			{
			case SPADES:
				if (d_suit != Card.Suit.HEARTS && d_suit != Card.Suit.DIAMONDS)
					return false;
				else
					return true;
			case CLUBS:
				if (d_suit != Card.Suit.HEARTS && d_suit != Card.Suit.DIAMONDS)
					return false;
				else
					return true;
			case HEARTS:
				if (d_suit != Card.Suit.SPADES && d_suit != Card.Suit.CLUBS)
					return false;
				else
					return true;
			case DIAMONDS:
				if (d_suit != Card.Suit.SPADES && d_suit != Card.Suit.CLUBS)
					return false;
				else
					return true;
			}
			return false; // this never gets reached
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

	@Override
	public void mousePressed(MouseEvent e)
	{
		start = e.getPoint();
		boolean stopSearch = false;
		statusBox.setText("");
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
			// pinpointing exact card pressed
			for (Component ca : source.getComponents())
			{
				Card c = (Card) ca;
				if (c.getFaceStatus() && source.contains(start))
				{
					transferStack.putFirst(c);
				}
				if (c.contains(start) && source.contains(start) && c.getFaceStatus())
				{
					card = c;
					stopSearch = true;
					System.out.println("Transfer Size: " + transferStack.showSize());
					break;
				}
			}

		}
		// SHOW (WASTE) CARD OPERATIONS
		// display new show card
		if (newCardButton.contains(start) && deck.showSize() > 0)
		{
			if (putBackOnDeck && prevCard != null)
			{
				System.out.println("Putting back on show stack: ");
				prevCard.getValue();
				prevCard.getSuit();
				deck.putFirst(prevCard);
			}

			System.out.print("poping deck ");
			deck.showSize();
			if (prevCard != null)
				table.remove(prevCard);
			Card c = deck.pop().setFaceup();
			table.add(moveCard(c, SHOW_POS.x, SHOW_POS.y));
			c.repaint();
			table.repaint();
			prevCard = c;
		}

		// preparing to move show card
		if (newCardPlace.contains(start) && prevCard != null)
		{
			movedCard = prevCard;
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

	@Override
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
					table.repaint();
					movedCard = null;
					putBackOnDeck = false;
					setScore(5);
					validMoveMade = true;
					break;
				}
				// to populated play stack
				if (movedCard != null && dest.contains(stop) && !dest.empty() && dest.getFirst().getFaceStatus()
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
					System.out.println("Destin" + dest.showSize());
					dest.push(movedCard);
					table.remove(prevCard);
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
					Card c = null;
					if (sourceIsFinalDeck)
						c = source.pop();
					else
						c = source.popFirst();

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

					table.repaint();

					System.out.print("Destination ");
					dest.showSize();
					if (sourceIsFinalDeck)
						setScore(15);
					else
						setScore(10);
					validMoveMade = true;
					break;
				} else if (dest.empty() && card.getValue() == Card.Value.KING && transferStack.showSize() == 1)
				{// MOVING TO EMPTY STACK, ONLY KING ALLOWED
					Card c = null;
					if (sourceIsFinalDeck)
						c = source.pop();
					else
						c = source.popFirst();

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

					table.repaint();

					System.out.print("Destination ");
					dest.showSize();
					setScore(5);
					validMoveMade = true;
					break;
				}
				// Moving STACK of cards from PLAY TO PLAY
				// to EMPTY STACK
				if (dest.empty() && dest.contains(stop) && !transferStack.empty()
						&& transferStack.getFirst().getValue() == Card.Value.KING)
				{
					System.out.println("King To Empty Stack Transfer");
					while (!transferStack.empty())
					{
						System.out.println("popping from transfer: " + transferStack.getFirst().getValue());
						dest.putFirst(transferStack.popFirst());
						source.popFirst();
					}
					if (source.getFirst() != null)
					{
						Card temp = source.getFirst().setFaceup();
						temp.repaint();
						source.repaint();
					}

					dest.setXY(dest.getXY().x, dest.getXY().y);
					dest.repaint();

					table.repaint();
					setScore(5);
					validMoveMade = true;
					break;
				}
				// to POPULATED STACK
				if (dest.contains(stop) && !transferStack.empty() && source.contains(start)
						&& validPlayStackMove(transferStack.getFirst(), dest.getFirst()))
				{
					System.out.println("Regular Stack Transfer");
					while (!transferStack.empty())
					{
						System.out.println("popping from transfer: " + transferStack.getFirst().getValue());
						dest.putFirst(transferStack.popFirst());
						source.popFirst();
					}
					if (source.getFirst() != null)
					{
						Card temp = source.getFirst().setFaceup();
						temp.repaint();
						source.repaint();
					}

					dest.setXY(dest.getXY().x, dest.getXY().y);
					dest.repaint();

					table.repaint();
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
							Card c = source.popFirst();
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

							table.repaint();

							System.out.print("Destination ");
							dest.showSize();
							card = null;
							setScore(10);
							validMoveMade = true;
							break;
						}// TO POPULATED STACK
					} else if (validFinalStackMove(card, dest.getLast()))
					{
						Card c = source.popFirst();
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

						table.repaint();

						System.out.print("Destination ");
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
			statusBox.setText("That Is Not A Valid Move");
		} else {
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
			if (!gameNotOver)
				gameOver = true;
		}

		if (checkForWin && gameOver)
		{
			mainMenu.updateScores(gameName, score, time, true);
			JOptionPane.showMessageDialog(table, "Congratulations! You've Won!");
			statusBox.setText("Game Over!");
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
		mainMenu.updateScores(gameName, score, time, false);
		playNewGame();
	}

	private void playNewGame()
	{
		deck = new CardStack(true); // deal 52 cards
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
		final_cards = new FinalStack[NUM_FINAL_DECKS];
		for (int x = 0; x < NUM_FINAL_DECKS; x++)
		{
			final_cards[x] = new FinalStack();

			final_cards[x].setXY((FINAL_POS.x + (x * Card.CARD_WIDTH)) + 10, FINAL_POS.y);
			table.add(final_cards[x]);

		}
		// place new card distribution button
		table.add(moveCard(newCardButton, DECK_POS.x, DECK_POS.y));
		// initialize & place play (tableau) decks/stacks
		playCardStack = new CardStack[NUM_PLAY_DECKS];
		for (int x = 0; x < NUM_PLAY_DECKS; x++)
		{
			playCardStack[x] = new CardStack(false);
			playCardStack[x].setXY((DECK_POS.x + (x * (Card.CARD_WIDTH + 10))), PLAY_POS.y);

			table.add(playCardStack[x]);
		}

		// Dealing new game
		for (int x = 0; x < NUM_PLAY_DECKS; x++)
		{
			Card c = deck.pop().setFaceup();
			playCardStack[x].putFirst(c);

			for (int y = x + 1; y < NUM_PLAY_DECKS; y++)
			{
				playCardStack[y].putFirst(c = deck.pop());
			}
		}
		// reset time
		time = 0;
		score = 0;
		table.repaint();
	}

	public void execute() {
		frame.setSize(TABLE_WIDTH, TABLE_HEIGHT);
        
        playNewGame();
	}
}