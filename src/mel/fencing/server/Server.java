package mel.fencing.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Server
{
    static ArrayList<UserSession> sessions = new ArrayList<UserSession>();
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        //TODO publish the ServerSocket and accept connections
	try
	{
	    ServerSocket server = new ServerSocket(0);
	    while(true)
	    {
		UserSession s = UserSession.makeSession(server.accept());
		if(s != null) sessions.add(s);
	    }
	} catch (IOException e)
	{
	    System.err.println(e.getMessage());
	}
    }

}
