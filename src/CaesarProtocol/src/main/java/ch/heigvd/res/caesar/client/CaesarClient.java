package ch.heigvd.res.caesar.client;

import ch.heigvd.res.caesar.protocol.*;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.Random;
import java.util.Scanner;


/**
 *
 * @author Olivier Liechti (olivier.liechti@heig-vd.ch)
 */
public class CaesarClient {

  private static final Logger LOG = Logger.getLogger(CaesarClient.class.getName());
    private static Object CipherCaesar;

	Socket clientSocket;
	BufferedReader in;
	PrintWriter out;
	boolean connected = false;
	int key; 	

	/**
	 * This method is used to connect to the server and to inform the server that
	 * the user "behind" the client has a name (in other words, the HELLO command
	 * is issued after successful connection).
	 * 
	 * @param serverAddress the IP address used by the Presence Server
	 * @param serverPort the port used by the Presence Server
	 */
	public void connect(String serverAddress, int serverPort, int key) {
		try {
			clientSocket = new Socket(serverAddress, serverPort);                        
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream());
			connected = true;
			this.key = key;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Unable to connect to server: {0}", e.getMessage());
			cleanup();
			return;
		}
		// Let us start a thread, so that we can listen for server notifications
                
		//new Thread(new NotificationListener()).start();
		
		// Let us send the HELLO command to inform the server about who the user
		// is. Other clients will be notified. 
		out.flush();
	}

	public void disconnect() {
		LOG.log(Level.INFO, "disconnected.");
		connected = false;		
		cleanup();
	}

	private void cleanup() {

		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}

		if (out != null) {
			out.close();
		}

		try {
			if (clientSocket != null) {
				clientSocket.close();
			}
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
        
        //Generate a random key
        private int randomKey()
        {
            Random r = new Random();
            return 1 + r.nextInt(5);
        }
        

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tH:%1$tM:%1$tS::%1$tL] Client > %5$s%n");
    LOG.info("Caesar client starting...");
    LOG.info("Protocol constant: " + Protocol.DEFAULT_PORT);
    
    int key;
    String message;
    String messCipher;
    String reception;    
    String decipherMessage;
    Scanner sc = new Scanner(System.in);
    CaesarClient c = new CaesarClient();
    key = c.randomKey();

    //open de connection
    c.connect("localhost", Protocol.DEFAULT_PORT, key);
    
    //send key
    c.out.println(key);
    c.out.flush();
    LOG.info("Key : " + key);
    
    while(true)
    {    
        //User write a message in keyboard
        LOG.info("Write message");
        message = sc.nextLine();
        
        //Disconnect if user message is BYE
        if(message.equals(Protocol.CMD_BYE))
        {
            c.disconnect();
            return;
        }
        
        //Send cipher message
        messCipher = CipherMessage.cipher(message,key);        
        c.out.println(messCipher);               
        c.out.flush();
        
        LOG.log(Level.INFO, "Message without cipher : " + message);
        LOG.log(Level.INFO, "Message send : " + messCipher); 
        
        
        try {
            //Read message from server
            reception = c.in.readLine();
            decipherMessage = CipherMessage.deCipher(reception, key);
            LOG.log(Level.INFO, "Reception :" + reception);
            LOG.log(Level.INFO, "Reception decipher :" + decipherMessage);          
            
        } catch (IOException ex) {
            Logger.getLogger(CaesarClient.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }      
    
  }
  
}
