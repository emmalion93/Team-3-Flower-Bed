/**
 * Object class that stores information needed for the undo() redo() methods such as the card that was moved, 
 * the stack it came from, the stack it was moved to, and the score the player recieved for it moving it
 */
public class CardHistory {
    private Card myCard;
    private FlowerBedCardStack myCardStack;
    private FlowerBedCardStack myLastCardStack;
    private int scoreValue;

    /**
     * The constructor for the CardHistory class.
	 * @param suit
     * @param card
     * @param cardStack
     * @param lastStack
     * @param myValue
     */
    public CardHistory(Card card, FlowerBedCardStack cardStack, FlowerBedCardStack lastStack, int myValue) {
        myCard = card;
        myCardStack = cardStack;
        myLastCardStack = lastStack;
        scoreValue = myValue;
    }

    // Get methods
    public Card getCard() { return myCard; }
    public FlowerBedCardStack getCardStack() { return myCardStack; }
    public FlowerBedCardStack getLastCardStack() { return myLastCardStack; }
    public int getScoreValue() { return scoreValue; }

}