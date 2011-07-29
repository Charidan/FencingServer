package mel.fencing.server;

import java.util.ArrayList;
import java.util.HashMap;

public class Lobby
{
    private UserSession openChallenger = null;
    private ArrayList<UserSession> playing = new ArrayList<UserSession>();
    private HashMap<UserSession,UserSession> challenger2target = new HashMap<UserSession,UserSession>();
    private HashMap<UserSession,UserSession> target2challenger = new HashMap<UserSession,UserSession>();
    
    /*
     * OPCODES FOR LOBBY
     * E = Error
     * T = Targeted (you have been challenged)
     * A = Accept
     * C = Canceled by challenger
     * c = rejected by target
     */
    
    synchronized public void removeFromGame(UserSession player)
    {
        playing.remove(player);
    }
    
    synchronized public void challengeOpen(UserSession challenger)
    {
        if(playing.contains(challenger))
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
        if(playing.contains(target))
        {
            challenger.send("E"+target.getUsername()+" is already in a game.");
            return;
        }
        if(playing.contains(challenger))
        {
            challenger.send("EOne game at a time.");
            return;
        }
        if(target2challenger.containsKey(target))
        {
            challenger2target.put(challenger, target);
            target.addChallenge(challenger.getUsername());
            challenger.send("W"+target.getUsername());
            return;
        }
        challenger2target.put(challenger, target);
        target2challenger.put(target, challenger);
        challenger.send("W"+target.getUsername());
        target.send("T"+challenger.getUsername());
    }
    
    synchronized public void cancel(UserSession out)
    {
        if(out == openChallenger) openChallenger = null;
        if(challenger2target.containsKey(out)) //if is a chalenger
        {
            UserSession target = challenger2target.get(out);
            if(target2challenger.get(target) != out) //if is secondary challenger
            {
                challenger2target.remove(out).removeChallenge(out.getUsername());
                return;
            }
            target.send("C"+out.getUsername());
            target2challenger.remove(target);
            challenger2target.remove(out);
            target.processNextChallenge();
        }
        if(target2challenger.containsKey(out)) rejectChallenge(out);
    }
    
    synchronized public void acceptChallenge(UserSession target)
    {
        UserSession challenger = target2challenger.get(target);
        acceptChallenge(challenger, target);
    }
    
    synchronized public void acceptChallenge(UserSession challenger, UserSession target)
    {
        if(playing.contains(challenger))
        {
            target.send("E"+challenger.getUsername()+" is already in a game");
            return;
        }
        playing.add(challenger);
        playing.add(target);
        challenger2target.remove(challenger);
        target2challenger.remove(target);
        challenger.rejectPending();
        target.rejectPending();
        Game.newGame(challenger,target);
    }
    
    synchronized public void rejectChallenge(UserSession target)
    {
        UserSession challenger = target2challenger.get(target);
        challenger2target.remove(challenger);
        target2challenger.remove(target);
        challenger.send("c"+target.getUsername());
        target.processNextChallenge();
    }
    
    synchronized public void rejectChallenge(UserSession target, UserSession challenger)
    {
        challenger2target.remove(challenger);
        target2challenger.remove(target);
        challenger.send("c"+target.getUsername());
    }

    synchronized public void challengeFromQueue(UserSession challenger, UserSession target)
    {
        target2challenger.put(target, challenger);
        target.send("T"+challenger.getUsername());
    }
}
