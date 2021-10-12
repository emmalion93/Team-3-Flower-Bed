public class CardHistory {
    private Card myCard;
    private FlowerBedCardStack myCardStack;
    private FlowerBedCardStack myLastCardStack;
    private int scoreValue;

    public CardHistory(Card card, FlowerBedCardStack cardStack, FlowerBedCardStack lastStack, int myValue) {
        myCard = card;
        myCardStack = cardStack;
        myLastCardStack = lastStack;
        scoreValue = myValue;
    }

    public Card getCard() { return myCard; }
    public FlowerBedCardStack getCardStack() { return myCardStack; }
    public FlowerBedCardStack getLastCardStack() { return myLastCardStack; }
    public int getScoreValue() { return scoreValue; }

}
