package mel.fencing.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class UserSession extends Thread
{
    private Socket s;
    private BufferedInputStream in;
    private PrintStream out;
    
    private UserSession(Socket socket) throws IOException
    {
	s = socket;
	in = new BufferedInputStream(s.getInputStream());
	out = new PrintStream(s.getOutputStream());
    }
    
    public static UserSession makeSession(Socket socket)
    {
	try
	{
	    return new UserSession(socket);
	} catch (IOException e)
	{
	    System.err.println(e.getMessage());
	    return null;
	}
    }
}
