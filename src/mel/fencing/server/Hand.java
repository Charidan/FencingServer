package mel.fencing.server;

public class Hand
{
    public static final int HAND_SIZE = 5;
    Card card[] = new Card[HAND_SIZE];
    
    public void fill(Deck deck)
    {
        for(int i=0; i<HAND_SIZE; i++) if(card[i] == null) card[i] = deck.drawCard();
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(1+HAND_SIZE);
        sb.append("h");
        for(Card c: card)
        {
            if(c == null) sb.append('0');
            else sb.append(c.toString());
        }
        return sb.toString();
    }
}
