package mel.fencing.server;

public class Hand
{
    public static final int HAND_SIZE = 5;
    Card card[] = new Card[HAND_SIZE];
    
    public void fill(Deck deck)
    {
        for(int i=0; i<HAND_SIZE; i++) if(card[i] == null) card[i] = deck.drawCard();
    }
    
    public void removeByValue(int value)
    {
        for (int i = 0; i < HAND_SIZE; i++)
        {
            if (getValue(i) == value)
            {
                card[i] = null;
                return;
            }
        }
    }
    
    public void removeByValue(int value, int count)
    {
        for (int i = 0; i < HAND_SIZE; i++)
        {
            if (getValue(i) == value)
            {
                card[i] = null;
                count--;
                if(count == 0) return;
            }
        }
    }
    
    public int getValue(int index)
    {
        try
        {
            return card[index].getValue();
        } catch(Exception e)
        {
            return -1;
        }
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

    public boolean hasCard(int value)
    {
        for(Card c : card) if(c != null && c.getValue() == value) return true;
        return false;
    }
    
    public boolean hasCards(int value, int quantity)
    {
        int count = 0;
        for(Card c : card) if(c != null && c.getValue() == value) count++;
        return count >= quantity;
    }
    
    public boolean hasCardWithCards(int single, int multiple, int count)
    {
        if(single == multiple) return hasCards(single, count+1);
        else return hasCard(single) && hasCards(multiple, count);
    }
}
