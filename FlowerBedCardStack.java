
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JComponent;

/* This is GUI component with a embedded
 * data structure. This structure is a mixture
 * of a queue and a stack
 */
class FlowerBedCardStack extends JComponent
{
	protected final int NUM_CARDS = 52;
	protected Vector<Card> v;
	protected boolean playStack = false;
	protected int SPREAD = 18;
	protected int _x = 0;
	protected int _y = 0;
	protected Rectangle testR;

	public FlowerBedCardStack(boolean isDeck)
	{
		this.setLayout(null);
		v = new Vector<Card>();
		if (isDeck)
		{
			// set deck position
			for (Card.Suit suit : Card.Suit.values())
			{
				for (Card.Value value : Card.Value.values())
				{
					v.add(new Card(suit, value));
				}
			}
		} else
		{
			playStack = true;
		}
	}

	public boolean empty()
	{
		if (v.isEmpty())
			return true;
		else
			return false;
	}

	public void putFirst(Card c)
	{
		v.add(0, c);
	}

	public Card getFirst()
	{
		if (!this.empty())
		{
			return v.get(0);
		} else
			return null;
	}

	// analogous to peek()
	public Card getLast()
	{
		if (!this.empty())
		{
			return v.lastElement();
		} else
			return null;
	}

	// queue-like functionality
	public Card popFirst()
	{
		if (!this.empty())
		{
			Card c = this.getFirst();
			v.remove(0);
			return c;
		} else
			return null;

	}

	public void push(Card c)
	{
		v.add(c);
	}

	public Card pop()
	{
		if (!this.empty())
		{
			Card c = v.lastElement();
			v.remove(v.size() - 1);
			return c;
		} else
			return null;
	}

	// shuffle the cards
	public void shuffle()
	{
		Vector<Card> v = new Vector<Card>();
		while (!this.empty())
		{
			v.add(this.pop());
		}
		while (!v.isEmpty())
		{
			Card c = v.elementAt((int) (Math.random() * v.size()));
			this.push(c);
			v.removeElement(c);
		}

	}

	public int showSize()
	{
		return v.size();
	}
  
	public Vector<Card> getStack()
    {
        return v;
    }

	public void removeCard(Card c) {
		v.removeElement(c);
	}

	// reverse the order of the stack
	public FlowerBedCardStack reverse()
	{
		Vector<Card> v = new Vector<Card>();
		while (!this.empty())
		{
			v.add(this.pop());
		}
		while (!v.isEmpty())
		{
			Card c = v.firstElement();
			this.push(c);
			v.removeElement(c);
		}
		return this;
	}

	public void makeEmpty()
	{
		while (!this.empty())
		{
			this.popFirst();
		}
	}

	@Override
	public boolean contains(Point p)
	{
        if(playStack) {
            //Rectangle rect = new Rectangle(_x, _y, Card.CARD_WIDTH + 10, Card.CARD_HEIGHT * 2);
			Rectangle rect = new Rectangle(_x, _y, Card.CARD_WIDTH + 10, (int)(Card.CARD_HEIGHT * 3.2));
            return (rect.contains(p));
        } else {
            Rectangle rect = new Rectangle(_x, _y, Card.CARD_WIDTH * 16, Card.CARD_HEIGHT);
            return (rect.contains(p));
        }
	}

	public void setXY(int x, int y)
	{
		_x = x;
		_y = y;
		// System.out.println("CardStack SET _x: " + _x + " _y: " + _y);
        //setBounds(_x, _y, Card.CARD_WIDTH + 1000, Card.CARD_HEIGHT * 3);
		setBounds(_x, _y, Card.CARD_WIDTH + 1000, Card.CARD_HEIGHT * 4);
	}

	public Point getXY()
	{
		// System.out.println("CardStack GET _x: " + _x + " _y: " + _y);
		return new Point(_x, _y);
	}
  
	// moves a card to abs location within a component
	protected Card moveCard(Card c, int x, int y)
	{
		//c.setBounds(new Rectangle(new Point(x, y), new Dimension(Card.CARD_WIDTH + 10, Card.CARD_HEIGHT + 10)));
        c.setBounds(new Rectangle(new Point(x, y), new Dimension(Card.CARD_WIDTH, Card.CARD_HEIGHT)));
		c.setXY(new Point(x, y));
		return c;
	}
  
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		//TODO all my problems are here
		if (playStack)
		{
			removeAll();
			Point prev = new Point(); // positioning relative to the container
			for (int x = 0; x < v.size(); x++)
            {
				Card c = v.get(x);
				prev = new Point(0, (SPREAD * (v.size() - x)));
				add(moveCard(c, prev.x, prev.y ));

				Point p = getXY();
				c.setWhereAmI(new Point(p.x, p.y+ (SPREAD * (v.size() - x))));
			}

		} else {
            removeAll();
			//ListIterator<Card> iter = v.listIterator();
			Point prev = new Point(); // positioning relative to the container
			Point prevWhereAmI = new Point();// abs positioning on the board
                        
            for (int x = 0; x < v.size(); x++)
            {
                Card c = v.get(x);
                            
				// this origin is point(0,0) inside the cardstack container
				prev = new Point(+ ((SPREAD * 3) * v.size()), 0);// c.getXY(); // starting deck pos
				add(moveCard(c, prev.x, prev.y ));
				// setting x & y position
                Point p = getXY();
				c.setWhereAmI(new Point(p.x+ ((SPREAD * 3) * v.size()), p.y));
				prevWhereAmI = c.getWhereAmI();
            }
                        
            for (int x = 0; x < v.size(); x++)
            {
                Card c = v.get(x);
                c.setXY(new Point(prev.x- (SPREAD * 3), prev.y ));
                
				add(moveCard(c, prev.x- (SPREAD * 3), prev.y));
				prev = c.getXY();
				// setting x & y position
				c.setWhereAmI(new Point(prevWhereAmI.x- (SPREAD * 3), prevWhereAmI.y));
				prevWhereAmI = c.getWhereAmI();
            }
        }
	}
}// END CardStack