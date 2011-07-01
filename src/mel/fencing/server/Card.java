package mel.fencing.server;

public class Card
{
    private final int value;
    
    public Card(int value) { this.value = value; }

    public int getValue() { return value; }
    
    public int toChar() { return '0'+value; }
    public String toString() { return ""+value; }
}
