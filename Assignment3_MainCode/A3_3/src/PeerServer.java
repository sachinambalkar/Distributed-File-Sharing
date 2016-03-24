
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;

public class PeerServer extends Thread
{
  @Override
	public void run() {
		super.run();
	    Properties property=new Properties();
		try {
			property.load(new FileInputStream(new File("config.property")));
			//'config.property' file contains information about PORT number.
			  
			 
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
       int ServerPort=Integer.parseInt(property.getProperty("ServerPort"));
       //PORT number is gets fetched from 'config.property' file
       
		try{

		ServerSocket servsock = new ServerSocket(ServerPort);  
	      PeerServer_Instance r = new PeerServer_Instance();
		    while (true) {
		    
		    /*This WHILE loop is responsible for NON-BLOCKING schema of this program. 
		     * For every new Peer it creates new thread, and assigned it to that peer. 
		     * This thread provides continous service to the PEER.
		     */
		      Socket sock = servsock.accept();
		      r.set(sock);
		      Thread t1 = new Thread(r, "Thread A");	        
		      t1.start();	
		    }
		}catch(Exception e){
			e.printStackTrace();
		}
	}

  private InetAddress getLocalAddress(){
	      try {
	          Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
	          while( b.hasMoreElements()){
	              for ( InterfaceAddress f : b.nextElement().getInterfaceAddresses())
	                  if ( f.getAddress().isSiteLocalAddress())
	                      return f.getAddress();
	          }
	      } catch (SocketException e) {
	          e.printStackTrace();
	      }
	      return null;
	  }

}