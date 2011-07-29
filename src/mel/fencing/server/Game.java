package mel.fencing.server;

public class Game
{
    //TODO RFI consider using powers of two so one bit each for color, canAdvance/Attack, canRetreat, and canParry 
    public static final int COLOR_NONE = -10;
    public static final int COLOR_GREEN = 0;
    public static final int COLOR_PURPLE = 10;
    public static final int TURN_MOVE = 0;
    public static final int TURN_PARRY = 1;
    public static final int TURN_PARRY_OR_RETREAT = 2;
    public static final int TURN_PURPLE_MOVE =               COLOR_PURPLE+TURN_MOVE;
    public static final int TURN_PURPLE_PARRY =              COLOR_PURPLE+TURN_PARRY;
    public static final int TURN_PURPLE_PARRY_OR_RETREAT =   COLOR_PURPLE+TURN_PARRY_OR_RETREAT; 
    public static final int TURN_GREEN_MOVE =                COLOR_GREEN+TURN_MOVE;
    public static final int TURN_GREEN_PARRY =               COLOR_GREEN+TURN_PARRY;
    public static final int TURN_GREEN_PARRY_OR_RETREAT =    COLOR_GREEN+TURN_PARRY_OR_RETREAT; 
    public static final int TURN_GAME_OVER = -1;
    
    UserSession purple;
    UserSession green;
    Deck deck;
    Hand greenHand = new Hand();
    Hand purpleHand = new Hand();
    int blackHP = 5;
    int whiteHP = 5;
    int purplePos = 23;
    int greenPos = 1;
    int turn = TURN_GREEN_MOVE;
    int parryVal = -1;
    int parryCount = -1;
    boolean finalParry = false;
    
    private Game(UserSession challenger, UserSession target)
    {
        purple = challenger;
        green = target;
        deck = new Deck();
        deck.shuffle();
        purple.setGame(this);
        purple.setColor(COLOR_PURPLE);
        green.setGame(this);
        green.setColor(COLOR_GREEN);
        sendNames();
        purpleHand.fill(deck);
        sendPurpleHand();
        greenHand.fill(deck);
        sendGreenHand();
    }
    
    public static Game newGame(UserSession challenger, UserSession target)
    {
        return new Game(challenger, target);
    }
    
    private void sendGreenHand() { green.send(greenHand.toString()); }
    private void sendPurpleHand() { purple.send(purpleHand.toString()); }
    
    private void sendNames()
    {
        green.send("w"+purple.getUsername());
        purple.send("b"+green.getUsername());
    }
    
    private final int playerColor(UserSession player)
    {
        if(player == green) return COLOR_GREEN;
        if(player == purple) return COLOR_PURPLE;
        return COLOR_NONE;
    }
    
    synchronized void jumpAttack(UserSession player, String values)
    {
        if(values.length() != 3) send(player, "ESyntax error in attack:"+values);
        int distance = parseDigit(values.charAt(0));
        int value = parseDigit(values.charAt(1));
        int count = parseDigit(values.charAt(2));
        if(value<0 || count<1) send(player, "ESyntax error in attack:"+values);
        
        int color = playerColor(player);
        if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
        if(turn != color+TURN_MOVE) 
        {
            if(turn == color+TURN_PARRY || turn == color+TURN_PARRY_OR_RETREAT) send(player, "EDefend before attacking");
            else send(player, "ENot your turn to attack");
            return;
        }
        
        Hand hand = handOf(color);
        if(!hand.hasCardWithCards(distance, value, count))
        {
            send(player, "EYou don't have the cards to jump-attack ("+distance+":"+value+","+count+")");
            return;
        }
        
        if(purplePos - greenPos != (distance+value))
        {
            send(player, "EYou are the wrong distance to jump-attack with a "+distance+" and "+value);
            return;
        }
        
        parryVal = value;
        parryCount = count; 
        movePosOf(color, distance);        
        hand.removeByValue(distance);
        hand.removeByValue(value, count);
        hand.fill(deck);
        if(deck.isEmpty()) { finalParry = true; notifyFinalParry(); }
        turn = otherColor(color)+TURN_PARRY_OR_RETREAT;
        
        notifyPositions();
        notifyHand(player, hand);
        notifyAttack(value, count, distance);
        notifyTurn();
    }
    
    private void notifyAttack(int value, int count, int distance)
    {
        sendAll("a"+value+""+count+""+distance);
    }
    
    private void notifyFinalParry()
    {
        sendAll("f");
    }

    synchronized void standingAttack(UserSession player, String values)
    {
        if(values.length() != 2) send(player, "ESyntax error in attack:"+values);
        int value = parseDigit(values.charAt(0));
        int count = parseDigit(values.charAt(1));
        if(value<0 || count<1) send(player, "ESyntax error in attack:"+values);
        
        int color = playerColor(player);
        if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
        if(turn != color+TURN_MOVE) 
        {
            if(turn == color+TURN_PARRY || turn == color+TURN_PARRY_OR_RETREAT) send(player, "EDefend before attacking");
            else send(player, "ENot your turn to attack");
            return;
        }

        Hand hand = handOf(color);
        if(!hand.hasCards(value, count))
        {
            send(player, "EYou don't have the cards to attack ("+value+","+count+")");
            return;
        }
        
        if(purplePos - greenPos != value)
        {
            send(player, "EYou are the wrong distance to attack with a "+value);
            return;
        }
        
        parryVal = value;
        parryCount = count;
        hand.removeByValue(value, count);
        hand.fill(deck);
        
        if(checkmate(color)) { notifyCannotParry(color); turn = TURN_GAME_OVER; clearPlayers(); }
        else 
        {
            if(deck.isEmpty()) { finalParry = true; notifyFinalParry(); }
            turn = otherColor(color)+TURN_PARRY;
        }
        
        notifyHand(player, hand);
        notifyAttack(value, count, 0);
        notifyTurn();        
    }
    
    private void notifyCannotParry(int color)
    {
        sendAll(color == Game.COLOR_GREEN ? "A1" : "B1");
    }

    private boolean checkmate(int color)
    {
        Hand hand = handOf(otherColor(color));
        return !hand.hasCards(parryVal, parryCount);
    }

    synchronized void move(UserSession player, String values)
    {
        if(values.length() != 1) send(player, "ESyntax error in advance:"+values);
        int distance = parseDigit(values.charAt(0));
        if(distance < 0) send(player, "ESyntax error in advance:"+values);
        
        int color = playerColor(player);
        if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
        if(turn != color+TURN_MOVE) 
        {
            if(turn == color+TURN_PARRY || turn == color+TURN_PARRY_OR_RETREAT) send(player, "EDefend before advancing");
            else send(player, "ENot your turn to advance");
            return;
        }
 
        if(purplePos-greenPos<=distance)
        {
            send(player, "EMay not move through other fencer");
            return;
        }
        
        Hand hand = handOf(color);
        if(!hand.hasCard(distance))
        {
            // hacked or buggy client
            send(player, "EYou don't have the advance card "+distance);
            return;
        }

        movePosOf(color, distance);        
        hand.removeByValue(distance);
        hand.fill(deck);
        if(deck.isEmpty()) { endGame(); }
        else turn = otherColor(color)+TURN_MOVE;
        
        notifyPositions();
        notifyHand(player, hand);
        notifyMove(distance);
        notifyTurn();
    }

    private void notifyMove(int distance)
    {
        sendAll("m"+distance);
    }
    
    private void notifyRetreat(int distance)
    {
        sendAll("r"+distance);
    }
    
    synchronized void retreat(UserSession player, String values)
    {
        if(values.length() != 1) send(player, "ESyntax error in retreat:"+values);
        int distance = parseDigit(values.charAt(0));
        if(distance < 0) send(player, "ESyntax error in retreat:"+values);
        int color = playerColor(player);
        if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
        if(turn != color+TURN_PARRY_OR_RETREAT && turn != color+TURN_MOVE)
        {
            if(turn == color+TURN_PARRY) send(player, "EYou cannot retreat from a standing attack");
            else send(player, "ENot your turn to retreat");
            return;
        }
        
        Hand hand = handOf(color);
        if(!hand.hasCard(distance))
        {
            // hacked or buggy client
            send(player, "EYou don't have the advance card "+distance);
            return;
        }
        
        movePosOf(color, -distance);
        hand.removeByValue(distance);
        hand.fill(deck);
        if(deck.isEmpty() || fencerOffStrip()) { endGame(); }
        else {
            turn = otherColor(color)+TURN_MOVE;
            if(finalParry) endGame();
        }
        notifyPositions();
        notifyHand(player, hand);
        notifyRetreat(distance);
        notifyTurn();
    }
    
    synchronized void parry(UserSession player)
    {
        int color = playerColor(player);
        if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
        if(turn != color+TURN_PARRY_OR_RETREAT && turn != color+TURN_PARRY)
        {
            if(turn == color+TURN_MOVE) send(player, "EThere is no attack to parry");
            else send(player, "ENot your turn to parry");
            return;
        }
        
        Hand hand = handOf(color);
        if(!hand.hasCards(parryVal, parryCount))
        {
            send(player, "EYou don't have the cards to parry ("+parryVal+","+parryCount+")");
            return;
        }
        
        
        hand.removeByValue(parryVal, parryCount);
        turn = color+TURN_MOVE;
        sendAll("q"); // notify parry occured
        if(finalParry) endGame();
        notifyTurn();
    }
    
    private void endGame()
    {
        turn = TURN_GAME_OVER;
        clearPlayers();
        if(greenPos < 1)  { sendAll("B0"); return; }
        if(purplePos > 23) { sendAll("A0"); return; }
        
        int finalDistance = purplePos - greenPos;
        int whiteCount = greenHand.countCards(finalDistance);
        int blackCount = purpleHand.countCards(finalDistance);
        if(whiteCount > blackCount) { sendAll("A2"); return; }
        if(blackCount > whiteCount) { sendAll("B2"); return; }
        if(12-greenPos > purplePos-12) { sendAll("B3"); return; }
        if(12-greenPos < purplePos-12) { sendAll("A3"); return; }
        sendAll("X");     
    }
    
    private void clearPlayers()
    {
        Server.lobby.removeFromGame(purple);
        Server.lobby.removeFromGame(green);
    }
    
    private final boolean fencerOffStrip()
    {
        return greenPos < 1 || purplePos > 23;
    }
    
    private void send(UserSession who, String what)
    {
        if(who != null) who.send(what);
    }
    
    private void sendAll(String what)
    {
        send(green, what);
        send(purple, what);
    }
    
    static private final int parseDigit(char in)
    {
        if(in<'0' || in > '9') return -1;
        return in-'0';
    }
    
    private void notifyPositions()
    {
        StringBuilder sb = new StringBuilder(3);
        sb.append("x");
        sb.append((char)('a'+greenPos-1));
        sb.append((char)('a'+purplePos-1));
        sendAll(sb.toString());
    }
    
    private final void notifyHand(UserSession player, Hand hand)
    {
        send(player, hand.toString());
    }
    
    private void notifyTurn()
    {
        sendAll("t"+turn);
    }
    
    private final int otherColor(int color)
    {
        if(color == COLOR_GREEN) return COLOR_PURPLE;
        if(color == COLOR_PURPLE) return COLOR_GREEN;
        return COLOR_NONE;
    }
    
    private final Hand handOf(int color)
    {
        if(color == COLOR_GREEN) return greenHand;
        if(color == COLOR_PURPLE) return purpleHand;
        return null;
    }
    
    private final void movePosOf(int color, int offset)
    {
        if(color == COLOR_GREEN) greenPos += offset;
        if(color == COLOR_PURPLE) purplePos -= offset;
    }

    public void cancel(UserSession userSession)
    {
        clearPlayers();
        sendAll("L");
    }
}
