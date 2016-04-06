package ch.heigvd.res.caesar.server;

import ch.heigvd.res.caesar.protocol.CipherMessage;
import ch.heigvd.res.caesar.protocol.Protocol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Olivier Liechti (olivier.liechti@heig-vd.ch)
 */
public class CaesarServer {

  private static final Logger LOG = Logger.getLogger(CaesarServer.class.getName());
  
  
        boolean shouldRun;
	ServerSocket serverSocket;
	final List<Worker> connectedWorkers;

	public CaesarServer() {
		this.shouldRun = true;
		this.connectedWorkers = Collections.synchronizedList(new LinkedList<Worker>());
	}

	private void registerWorker(Worker worker) {
		LOG.log(Level.INFO, ">> Waiting for lock before registring worker {0}", worker.userName);
		connectedWorkers.add(worker);
		LOG.log(Level.INFO, "<< Worker {0} registered.", worker.userName);
	}

	private void unregisterWorker(Worker worker) {
		LOG.log(Level.INFO, ">> Waiting for lock before unregistring worker {0}", worker.userName);
		connectedWorkers.remove(worker);
		LOG.log(Level.INFO, "<< Worker {0} unregistered.", worker.userName);
	}

	private void notifyConnectedWorkers(String message) {
		LOG.info(">> Waiting for lock before notifying workers");
		synchronized (connectedWorkers) {
		LOG.info("Notifying workers");
			for (Worker worker : connectedWorkers) {
				worker.sendNotification(message);
			}
		}
		LOG.info("<< Workers notified");
	}

	private void disconnectConnectedWorkers() {
		LOG.info(">> Waiting for lock before disconnecting workers");
		synchronized (connectedWorkers) {
		LOG.info("Disconnecting workers");
			for (Worker worker : connectedWorkers) {
				worker.disconnect();
			}
		}
		LOG.info("<< Workers disconnected");
	}

	public void run() {
		try {
			LOG.log(Level.INFO, "Starting Presence Server on port {0}", Protocol.DEFAULT_PORT);
			serverSocket = new ServerSocket(Protocol.DEFAULT_PORT);
			while (shouldRun) {
				Socket clientSocket = serverSocket.accept();
				Worker newWorker = new Worker(clientSocket);
				registerWorker(newWorker);
				new Thread(newWorker).start();
			}
			serverSocket.close();
			LOG.info("shouldRun is false... server going down");
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
                        System.exit(-1);
		}
	}

	private void shutdown() {
		LOG.info("Shutting down server...");
		shouldRun = false;
		try {
			serverSocket.close();
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
		disconnectConnectedWorkers();
	}

	class Worker implements Runnable {

		Socket clientSocket;
		BufferedReader in;
		PrintWriter out;
		boolean connected;
                int key;
		String userName = "An anonymous user";

		public Worker(Socket clientSocket) {
			this.clientSocket = clientSocket;
			try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream());
				connected = true;
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}

		@Override
		public void run() {
			String commandLine;
                        String message;
			try {   
                            //Catch key
                            key = Integer.parseInt(in.readLine());                            
                            LOG.info("key :" + key);
                            while (connected) 
                            {
                                //Read message cipher
                                commandLine = in.readLine();
                                message = CipherMessage.deCipher(commandLine, key);
                                LOG.log(Level.INFO, "Reception : " + commandLine);
                                LOG.log(Level.INFO, "Reception decipher : " + message);
                                
                                //Test if end of the connection
                                if(message == null)
                                {
                                    disconnect();
                                    unregisterWorker(this);
                                }
                                else
                                {
                                    //Resend de message
                                    String messCipher = CipherMessage.cipher(message, key);
                                    LOG.log(Level.INFO, "Message without cipher : " + message);
                                    LOG.log(Level.INFO, "Message send : " + messCipher);  
                                    out.println(messCipher);
                                    out.flush();
                                }
                            }
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			} 
		}

		private void cleanup() {
			LOG.log(Level.INFO, "Cleaning up worker used by {0}", userName);

			LOG.log(Level.INFO, "Closing clientSocket used by {0}", userName);
			try {
				if (clientSocket != null) {
					clientSocket.close();
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			}

			LOG.log(Level.INFO, "Closing in used by {0}", userName);
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			}

			LOG.log(Level.INFO, "Closing out used by {0}", userName);
			if (out != null) {
				out.close();
			}

			LOG.log(Level.INFO, "Clean up done for worker used by {0}", userName);
		}

                
		public void sendNotification(String message) {
			out.println(message);
			out.flush();
		}

		private void disconnect() {
			LOG.log(Level.INFO, "Disconnecting worker");
			connected = false;
			cleanup();
		}

	}

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tH:%1$tM:%1$tS::%1$tL] Server > %5$s%n");
    LOG.info("Caesar server starting...");
    LOG.info("Protocol constant: " + Protocol.DEFAULT_PORT);
    
    CaesarServer serv = new CaesarServer();
    serv.run();
    
      
  }
  
}
