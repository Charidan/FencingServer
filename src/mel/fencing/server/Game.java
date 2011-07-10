package mel.fencing.server;

public class Game
{
    public static final int COLOR_NONE = 6;
    public static final int COLOR_WHITE = 0;
    public static final int COLOR_BLACK = 10;
    public static final int TURN_MOVE = 0;
    public static final int TURN_PARRY = 1;
    public static final int TURN_PARRY_OR_RETREAT = 2;
    public static final int TURN_BLACK_MOVE =               COLOR_BLACK+TURN_MOVE;
    public static final int TURN_BLACK_PARRY =              COLOR_BLACK+TURN_PARRY;
    public static final int TURN_BLACK_PARRY_OR_RETREAT =   COLOR_BLACK+TURN_PARRY_OR_RETREAT; 
    public static final int TURN_WHITE_MOVE =               COLOR_WHITE+TURN_MOVE;
    public static final int TURN_WHITE_PARRY =              COLOR_WHITE+TURN_PARRY;
    public static final int TURN_WHITE_PARRY_OR_RETREAT =   COLOR_WHITE+TURN_PARRY_OR_RETREAT; 
    public static final int TURN_GAME_OVER = -1;
    
    UserSession black;
    UserSession white;
    Deck deck;
    Hand whiteHand = new Hand();
    Hand blackHand = new Hand();
    int blackHP = 5;
    int whiteHP = 5;
    int blackpos = 23;
    int whitepos = 1;
    int turn = TURN_WHITE_MOVE;
    
    private Game(UserSession challenger, UserSession target)
    {
        black = challenger;
        white = target;
        deck = new Deck();
        deck.shuffle();
        black.setGame(this);
        black.setColor(COLOR_BLACK);
        white.setGame(this);
        white.setColor(COLOR_WHITE);
        sendNames();
        blackHand.fill(deck);
        sendBlackHand();
        whiteHand.fill(deck);
        sendWhiteHand();
    }
    
    public static Game newGame(UserSession challenger, UserSession target)
    {
        return new Game(challenger, target);
    }
    
    private void sendWhiteHand() { white.send(whiteHand.toString()); }
    private void sendBlackHand() { black.send(blackHand.toString()); }
    
    private void sendNames()
    {
        white.send("w"+black.getUsername());
        black.send("b"+white.getUsername());
    }
    
    private final int playerColor(UserSession player)
    {
        if(player == white) return COLOR_WHITE;
        if(player == black) return COLOR_BLACK;
        return COLOR_NONE;
    }
    
    
    void attack(UserSession player, String values)
    {
        int color = playerColor(player);
        if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
        if(turn != color+TURN_MOVE) 
        {
            if(turn == color+TURN_PARRY || turn == color+TURN_PARRY_OR_RETREAT) send(player, "EDefend before attacking");
            else send(player, "ENot your turn to attack");
            return;
        }
        
        //TODO handle move logic
    }
    
    void move(UserSession player, String values)
    {
        int color = playerColor(player);
        if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
        if(turn != color+TURN_MOVE) 
        {
            if(turn == color+TURN_PARRY || turn == color+TURN_PARRY_OR_RETREAT) send(player, "EDefend before advancing");
            else send(player, "ENot your turn to advance");
            return;
        }
        
        //TODO handle move logic
    }
    
    void retreat(UserSession player, String values)
    {
        int color = playerColor(player);
        if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
        if(turn != color+TURN_PARRY_OR_RETREAT && turn != color+TURN_MOVE)
        {
            if(turn == color+TURN_PARRY) send(player, "EYou cannot retreat from a standing attack");
            else send(player, "ENot your turn to retreat");
        }
        
        //TODO handle move logic
    }
    
    void parry(UserSession player, String values)
    {
        int color = playerColor(player);
        if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
        if(turn != color+TURN_PARRY_OR_RETREAT && turn != color+TURN_PARRY)
        {
            if(turn == color+TURN_MOVE) send(player, "EThere is no attack to parry");
            else send(player, "ENot your turn to parry");
        }
        
        //TODO handle parry logic
    }
    
    private synchronized void send(UserSession who, String what)
    {
        if(who != null) who.send(what);
    }
}
