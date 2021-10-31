// created by team 3
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.awt.Point;
import java.awt.Rectangle;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * The object that contains a specific cards information such as the suit, value, position, and graphical elements
 * @author Emily
 */
class Card extends JPanel
{
	// Card information
	public static enum Value
	{
		ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING
	}

	public static enum Suit
	{
		SPADES, CLUBS, DIAMONDS, HEARTS
	}

	private Suit _suit;

	private Value _value;

	private Boolean _faceup;

	/**
	 * location relative to container
	 */
	private Point _location;

	/**
	 * used to create abs postion rectangle for contains
	 */
	private Point whereAmI; 

	/**
	 * used for relative positioning within CardStack or FlowerBedCardStack Container
	 */
	private int x; 
	private int y;
	
	/**
	 * Graphical information about the card
	 */
	public static final int CARD_HEIGHT = 150;
	public static final int CARD_WIDTH = 100;
	public static final int CORNER_ANGLE = 25;
	public int scale = 1;

	/**
	 * The information needed to identify the correct card(suit and value) within the card file path
	 */
	private String imageInfo;
	/**
	 * The graphical component for the card
	 */
	private Image myImage;

	/**
	 * The constructor for the Card class. Sets the suit, value, graphical information, and default location.
	 * @param suit
	 * @param value
	 */
	Card(Suit suit, Value value)
	{
		_suit = suit;
		_value = value;
		_faceup = false;
		_location = new Point();
		x = 0;
		y = 0;
		_location.x = x;
		_location.y = y;
		whereAmI = new Point();
		imageInfo = getImageInfo();
		try {
			myImage = getFaceDownImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The default constructor for the Card class. Sets the suit, value, graphical information, and default location.
	 * @param suit
	 * @param value
	 */
	Card()
	{
		_suit = Card.Suit.CLUBS;
		_value = Card.Value.ACE;
		_faceup = false;
		_location = new Point();
		x = 0;
		y = 0;
		_location.x = x;
		_location.y = y;
		whereAmI = new Point();
		imageInfo = getImageInfo();
		try {
			myImage = getFaceDownImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Suit getSuit()
	{
		return _suit;
	}

	public Value getValue()
	{
		return _value;
	}

	public Point getWhereAmI()
	{
		return whereAmI;
	}

	public Point getXY()
	{
		return new Point(x, y);
	}

	public Boolean getFaceStatus()
	{
		return _faceup;
	}

	public void setWhereAmI(Point p)
	{
		whereAmI = p;
	}

	public void setXY(Point p)
	{
		x = p.x;
		y = p.y;

	}

	public void setSuit(Suit suit)
	{
		_suit = suit;
	}

	public void setValue(Value value)
	{
		_value = value;
	}

	/**
	 * sets the card faceup and updates its image accordingly
	 * @return
	 */
	public Card setFaceup()
	{
		_faceup = true;
		try {
			myImage = getImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * sets the card facedown and updates its image accordingly
	 * @return
	 */
	public Card setFacedown()
	{
		_faceup = false;
		try {
			myImage = getFaceDownImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Determines if a point(mouse position) is within the bounds of the card
	 */
	@Override
	public boolean contains(Point p)
	{
		Rectangle rect = new Rectangle(whereAmI.x, whereAmI.y, Card.CARD_WIDTH, Card.CARD_HEIGHT);
		return (rect.contains(p));
	}
	
	/**
	 * Determines the card information needed for the card file path. For example: a seven of hearts = "7H"
	 * @return cardInfo
	 */
	private String getImageInfo() {
		String s = "";
		switch (_value)
			{
			case ACE:
				s = s + "1";
				break;
			case TWO:
				s = s + "2";
				break;
			case THREE:
				s = s +  "3";
				break;
			case FOUR:
				s = s +  "4";
				break;
			case FIVE:
				s = s +  "5";
				break;
			case SIX:
				s = s +  "6";
				break;
			case SEVEN:
				s = s +  "7";
				break;
			case EIGHT:
				s = s +  "8";
				break;
			case NINE:
				s = s + "9";
				break;
			case TEN:
				s = s +  "10";
				break;
			case JACK:
				s = s +  "11";
				break;
			case QUEEN:
				s = s +  "12";
				break;
			case KING:
				s = s +  "13";
				break;
			}
		switch (_suit)
			{
			case HEARTS:
				s = s + "H";
				break;
			case DIAMONDS:
				s = s + "D";
				break;
			case SPADES:
				s = s + "S";
				break;
			case CLUBS:
				s = s + "C";
				break;
			}
		return s + ".png";
	}

	/**
	 * Resets the card file path in instances where the card background might have changed
	 */
	public void refreshImage() {
		imageInfo = getImageInfo();
		if(_faceup) {
			try {
				myImage = getImage();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				myImage = getFaceDownImage();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sets the image of the card based on cardInfo and the current cardPath(card background).
	 * @return cardImage
	 * @throws IOException
	 */
	private Image getImage() throws IOException {
		Image myCard = ImageIO.read(new File(GameMode.cardPath + imageInfo));
		myCard = myCard.getScaledInstance(CARD_WIDTH * scale, CARD_HEIGHT * scale, Image.SCALE_SMOOTH);
		return myCard;
	}

	/**
	 * Sets the image of the card based on the current cardPath(card background) and the facedown image from that card background.
	 * @return cardImage
	 * @throws IOException
	 */
	private Image getFaceDownImage() throws IOException {
		Image myCard = ImageIO.read(new File(GameMode.cardPath+ "back.png"));
		myCard = myCard.getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
		return myCard;
	}

	/**
	 * Draws the card generated from getImage() at the card current x and y location.
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		if(myImage != null) {
			g.drawImage(myImage, _location.x, _location.y, null);
		}
	}

}// END Card