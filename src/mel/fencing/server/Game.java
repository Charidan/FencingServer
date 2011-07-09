package mel.fencing.server;

public class Game
{
    public static final int TURN_BLACK_MOVE = 0;
    public static final int TURN_BLACK_PARRY = 1;
    public static final int TURN_WHITE_MOVE = 2;
    public static final int TURN_WHITE_PARRY = 3;
    public static final int TURN_GAME_OVER = 4;
    public static final int COLOR_NONE = 0;
    public static final int COLOR_WHITE = 1;
    public static final int COLOR_BLACK = 2;
    
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
    
    void attack(UserSession player, String values)
    {
        int playerColor = player == white ? COLOR_WHITE : player == black ? COLOR_BLACK : COLOR_NONE;
        if(playerColor == COLOR_NONE) player.send("EYou are not a player in this game");
        if(playerColor == COLOR_WHITE && turn != TURN_WHITE_MOVE) player.send("ENot your turn to move");
        if(playerColor == COLOR_BLACK && turn != TURN_BLACK_MOVE) player.send("ENot your turn to move");
        
        //TODO handle move logic
    }
    
    void move(UserSession player, String values)
    {
        int playerColor = player == white ? COLOR_WHITE : player == black ? COLOR_BLACK : COLOR_NONE;
        if(playerColor == COLOR_NONE) player.send("EYou are not a player in this game");
        if(playerColor == COLOR_WHITE && turn != TURN_WHITE_MOVE) player.send("ENot your turn to move");
        if(playerColor == COLOR_BLACK && turn != TURN_BLACK_MOVE) player.send("ENot your turn to move");
        
        //TODO handle move logic
    }
    
    void retreat(UserSession player, String values)
    {
        int playerColor = player == white ? COLOR_WHITE : player == black ? COLOR_BLACK : COLOR_NONE;
        if(playerColor == COLOR_NONE) player.send("EYou are not a player in this game");
        if(playerColor == COLOR_WHITE && turn != TURN_WHITE_MOVE) player.send("ENot your turn to move");
        if(playerColor == COLOR_BLACK && turn != TURN_BLACK_MOVE) player.send("ENot your turn to move");
        
        //TODO handle move logic
    }
    
    void parry(UserSession player, String values)
    {
        int playerColor = player == white ? COLOR_WHITE : player == black ? COLOR_BLACK : COLOR_NONE;
        if(playerColor == COLOR_NONE) player.send("EYou are not a player in this game");
        if(playerColor == COLOR_WHITE && turn != TURN_WHITE_PARRY) player.send("ENot your turn to parry");
        if(playerColor == COLOR_BLACK && turn != TURN_BLACK_PARRY) player.send("ENot your turn to parry");
        
        //TODO handle parry logic
    }
}
