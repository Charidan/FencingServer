package mel.fencing.server;

import java.util.ArrayList;
import java.util.HashMap;

public class Lobby
{
    private UserSession openChallenger = null;
    private ArrayList<UserSession> unavailable = new ArrayList<UserSession>();
    private HashMap<UserSession,UserSession> challenger2target = new HashMap<UserSession,UserSession>();
    private HashMap<UserSession,UserSession> target2challegner = new HashMap<UserSession,UserSession>();
    
    /*
     * OPCODES FOR LOBBY
     * E = Error
     * T = Targeted (you have been challenged)
     * A = Accept
     * C = Canceled by challenger
     * c = rejected by target
     */
    
    synchronized public void challengeOpen(UserSession challenger)
    {
        if(unavailable.contains(challenger))
        {
            // TODO RFE support multiple games at once
            // TODO RFE support saving game state
            challenger.send("EOne game at a time.");
            return;
        }
        if(challenger2target.containsKey(challenger) || openChallenger == challenger)
        {
            challenger.send("ECancel prior challenge first");
            return;
        }
        if(openChallenger == null)
        {
            openChallenger = challenger;
            challenger.send("Wan opponent");
        }
        else 
        {
            acceptChallenge(challenger, openChallenger);
            openChallenger = null;
        }
    }
    
    synchronized public void challengeTarget(UserSession challenger, UserSession target)
    {
        if(target == null)
        {
            challenger.send("EOpponent is not logged in");
            return;
        }
        if(challenger2target.containsKey(challenger) || openChallenger == challenger)
        {
            challenger.send("ECancel prior challenge first");
            return;
        }
        if(challenger == target)
        {
            challenger.send("ECannot challenge self.");
            return;
        }
        if(unavailable.contains(target))
        {
            challenger.send("E"+target.getName()+" is already in a game.");
            return;
        }
        if(unavailable.contains(challenger))
        {
            challenger.send("EOne game at a time.");
            return;
        }
        challenger2target.put(challenger, target);
        target2challegner.put(target, challenger);
        challenger.send("W"+target.getName());
        target.send("T"+challenger.getName());
    }
    
    synchronized public void cancel(UserSession out)
    {
        if(out == openChallenger) openChallenger = null;
        if(challenger2target.containsKey(out))
        {
            UserSession target = challenger2target.get(out);
            target.send("C"+out.getName());
            challenger2target.remove(out);
            target2challegner.remove(target);
        }
        if(target2challegner.containsKey(out)) rejectChallenge(out);
    }
    
    public void acceptChallenge(UserSession target)
    {
        UserSession challenger = target2challegner.get(target);
        acceptChallenge(challenger, target);
    }
    
    public void acceptChallenge(UserSession challenger, UserSession target)
    {
        if(unavailable.contains(challenger))
        {
            target.send("E"+challenger.getName()+" is already in a game");
            return;
        }
        unavailable.add(challenger);
        unavailable.add(target);
        challenger2target.remove(challenger);
        target2challegner.remove(target);
        Game.newGame(challenger,target);
    }
    
    public void rejectChallenge(UserSession target)
    {
        UserSession challenger = target2challegner.get(target);
        challenger2target.remove(challenger);
        target2challegner.remove(target);
        challenger.send("c"+target.getName());
    }
}
