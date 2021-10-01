// created by team 3
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.awt.Point;
import java.awt.Rectangle;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import java.awt.Graphics2D;

class Card extends JPanel
{
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

	private Point _location; // location relative to container

	private Point whereAmI; // used to create abs postion rectangle for contains
	// functions

	private int x; // used for relative positioning within CardStack Container
	private int y;
	
	public static final int CARD_HEIGHT = 150;

	public static final int CARD_WIDTH = 100;

	public static final int CORNER_ANGLE = 25;

	private String imageInfo;
	private Image myImage;

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
		/*switch (_suit)
		{
		case HEARTS:
			//System.out.println("Hearts");
			break;
		case DIAMONDS:
			//System.out.println("Diamonds");
			break;
		case SPADES:
			//System.out.println("Spades");
			break;
		case CLUBS:
			//System.out.println("Clubs");
			break;
		}*/
		return _suit;
	}

	public Value getValue()
	{
		/*switch (_value)
		{
		case ACE:
			//System.out.println(" Ace");
			break;
		case TWO:
			//System.out.println(" 2");
			break;
		case THREE:
			//System.out.println(" 3");
			break;
		case FOUR:
			//System.out.println(" 4");
			break;
		case FIVE:
			//System.out.println(" 5");
			break;
		case SIX:
			//System.out.println(" 6");
			break;
		case SEVEN:
			//System.out.println(" 7");
			break;
		case EIGHT:
			//System.out.println(" 8");
			break;
		case NINE:
			//System.out.println(" 9");
			break;
		case TEN:
			//System.out.println(" 10");
			break;
		case JACK:
			//System.out.println(" Jack");
			break;
		case QUEEN:
			//System.out.println(" Queen");
			break;
		case KING:
			//System.out.println(" King");
			break;
		}*/
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

	@Override
	public boolean contains(Point p)
	{
		Rectangle rect = new Rectangle(whereAmI.x, whereAmI.y, Card.CARD_WIDTH, Card.CARD_HEIGHT);
		return (rect.contains(p));
	}
  
	private String getImageInfo() {
		String s = "";// imageFile;
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

	public void refreshImage() {
		imageInfo = getImageInfo();
		if(_faceup) {
			try {
				myImage = getImage();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				myImage = getFaceDownImage();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Image getImage() throws IOException {
		Image myCard = ImageIO.read(new File(GameMode.cardPath + imageInfo));
		myCard = myCard.getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
		return myCard;
	}

	private Image getFaceDownImage() throws IOException {
		Image myCard = ImageIO.read(new File(GameMode.cardPath+ "back.png"));//imageFile + "back.png"));
		myCard = myCard.getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
		return myCard;
	}

	@Override
	public void paintComponent(Graphics g)
	{
		if(myImage != null) {
			g.drawImage(myImage, _location.x, _location.y, null);
		}
	}

}// END Card