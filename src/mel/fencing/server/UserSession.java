package mel.fencing.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import mel.security.Account;

public class UserSession extends Thread
{
    public static final int MAX_TRIES = 3;
    
    private Socket s;
    private BufferedReader in;
    private PrintStream out;
    private String name;

    private UserSession(Socket socket) throws IOException
    {
        s = socket;
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintStream(s.getOutputStream());
    }

    public static UserSession makeSession(Socket socket)
    {
        try
        {
            UserSession s = new UserSession(socket);
            s.start();
            return s;
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    @Override
    public void run()
    {
        try
        {
            if(login()) readLoop();
            else Server.purge(this);
        }
        catch(Exception e)
        {
            Server.purge(this);
        }
        
    }

    private void readLoop() throws IOException
    {
        while(true)
        {
            //TODO respond to commands
            String s = in.readLine();
            //System.out.println(s);
            out.println(s);
            out.flush();
        }
    }

    private boolean login() throws IOException
    {
        for(int i = 0; i < MAX_TRIES; i++)
        {
            name = in.readLine();
            String password = in.readLine();
            if(auth(name, password))
            {
                sendSuccess();
                return true;
            } else
            {
                sendFailure();
            }
        }
        return false;
    }

    private void sendFailure()
    {
        out.println("E"+name);
        out.flush();
    }

    private void sendSuccess()
    {
        out.println("L"+name);
        out.flush();
    }
    
    private boolean auth(String name, String password)
    {
        Account a = Server.accMan.getAccount(name);
        try
        {
            a.authenticate(password);
            return true;
        }
        catch(SecurityException e)
        {
            return false;
        }
    }
}
