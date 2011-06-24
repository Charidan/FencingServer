package mel.fencing.server;

import java.util.ArrayList;
import java.util.HashMap;

public class Lobby
{
    private UserSession openChallenger = null;
    private ArrayList<UserSession> unavailable = new ArrayList<UserSession>();
    private HashMap<UserSession,UserSession> challengers = new HashMap<UserSession,UserSession>();
    private HashMap<UserSession,UserSession> targets = new HashMap<UserSession,UserSession>();
    
    /*
     * OPCODES FOR LOBBY
     * F = Failure
     * T = Targeted
     * A = Accept
     * C = Canceled
     */
    
    synchronized public void challengeOpen(UserSession challenger)
    {
        if(openChallenger == null) openChallenger = challenger;
        else acceptChallenge(challenger, openChallenger);
    }
    
    synchronized public void challengeTarget(UserSession challenger, UserSession target)
    {
        if(target == null)
        {
            challenger.send("FOpponent is not logged in");
        }
        if(challenger == target)
        {
            challenger.send("FCannot challenge self.");
            return;
        }
        if(unavailable.contains(target))
        {
            challenger.send("F"+target.getName()+" is already in a game.");
            return;
        }
        challengers.put(challenger, target);
        targets.put(target, challenger);
        target.send("T"+challenger.getName());
    }
    
    synchronized public void cancel(UserSession out)
    {
        //TODO cancel challenge (if any) by out -- to be called by purge and when user manually cancels
        if(out == openChallenger) openChallenger = null;
        if(challengers.containsKey(out))
        {
            UserSession target = challengers.get(out);
            target.send("FChallenge withdrawn");
            challengers.remove(out);
            targets.remove(target);
        }
        if(targets.containsKey(out)) rejectChallenge(out);
    }
    
    public void acceptChallenge(UserSession target)
    {
        UserSession challenger = targets.get(target);
        acceptChallenge(challenger, target);
    }
    
    public void acceptChallenge(UserSession challenger, UserSession target)
    {
        if(unavailable.contains(challenger))
        {
            target.send("F"+challenger.getName()+" is already in a game");
            return;
        }
        unavailable.add(challenger);
        unavailable.add(target);
        challengers.remove(challenger);
        targets.remove(target);
        Game.newGame(challenger,target);
    }
    
    public void rejectChallenge(UserSession target)
    {
        UserSession challenger = targets.get(target);
        challengers.remove(challenger);
        targets.remove(target);
        challenger.send("FChallenge Rejected");
    }
}
