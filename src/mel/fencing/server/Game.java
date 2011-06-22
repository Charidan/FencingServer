package mel.fencing.server;

public class Game
{
    public static final boolean BLACK = false;
    public static final boolean WHITE = true;
    public static final int TURN_BLACK = 0;
    public static final int TURN_WHITE = 1;
    public static final int TURN_OVER = 2;
    public static final int TURN_START = 3;
    
    UserSession black;
    UserSession white;
    Deck deck;
    //Hand whiteHand;
    //Hand blackHand;
    int blackHP = 5;
    int whiteHP = 5;
    int blackpos;
    int whitepos;
    int turn = TURN_START;
    
    private Game(UserSession challenger, UserSession target)
    {
        black = challenger;
        white = target;
        deck = new Deck();
        deck.shuffle();
        //fillHand(BLACK);
        //fillHand(WHITE);
    }
    
    public static Game newGame(UserSession challenger, UserSession target)
    {
        //TODO Initialize the game: shuffle the deck, deal hands, etc.
        return new Game(challenger, target);
    }
    
    /*private void fillHand(boolean player)
    {
        if(player == WHITE) while(whiteHand.size() < 5) whiteHand.add(deck.drawCard());
        else if(player == BLACK) while(blackHand.size() < 5) blackHand.add(deck.drawCard());
    }*/
}
