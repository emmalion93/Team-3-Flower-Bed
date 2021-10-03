// created by team 3
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.awt.Point;
import java.awt.Rectangle;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;


import java.awt.Graphics2D;

class PreviewCard extends JComponent
{
	

	private Card.Suit _suit;

	private Card.Value _value;

	private Boolean _faceup;

	private Point _location; // location relative to container

	private Point whereAmI; // used to create abs postion rectangle for contains
	// functions

	private int x; // used for relative positioning within CardStack Container
	private int y;
	
	public static final int CARD_HEIGHT = 200;
	public static final int CARD_WIDTH = 133;
	public static final int CORNER_ANGLE = 25;

	private String imageInfo;
	private Image myImage;

    private JEditorPane Text = new JEditorPane();

	PreviewCard(Card.Suit suit, Card.Value value)
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

	PreviewCard()
	{
        this.setLayout(null);
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

	public Card.Suit getSuit()
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

	public Card.Value getValue()
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
        _location.x = x;
        _location.y = y;
        setBounds(x, y, Card.CARD_WIDTH + 1000, Card.CARD_HEIGHT * 4);
	}

    public void setText()
	{
		
	}

	public void setSuit(Card.Suit suit)
	{
		_suit = suit;
	}

	public void setValue(Card.Value value)
	{
		_value = value;
	}

	public PreviewCard setFaceup()
	{
		_faceup = true;
		try {
			myImage = getImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PreviewCard setFacedown()
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
		Rectangle rect = new Rectangle(whereAmI.x, whereAmI.y, PreviewCard.CARD_WIDTH, PreviewCard.CARD_HEIGHT);
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
	protected void paintComponent(Graphics g)
	{
		if(myImage != null) {
			g.drawImage(myImage, _location.x, _location.y, null);
		}
	}

}// END PreviewCard