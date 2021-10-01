public class CardHistory {
    private Card myCard;
    private FlowerBedCardStack myCardStack;
    private FlowerBedCardStack myLastCardStack;

    public CardHistory(Card card, FlowerBedCardStack cardStack, FlowerBedCardStack lastStack) {
        myCard = card;
        myCardStack = cardStack;
        myLastCardStack = lastStack;
    }

    public Card getCard() { return myCard; }
    public FlowerBedCardStack getCardStack() { return myCardStack; }
    public FlowerBedCardStack getLastCardStack() { return myLastCardStack; }

}
