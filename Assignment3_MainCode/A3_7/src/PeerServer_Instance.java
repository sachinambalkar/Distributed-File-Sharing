
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class PeerServer_Instance implements Runnable
{
	static Random randomGenerator = new Random();
      static Map<String, String> mp = new ConcurrentHashMap<String, String>();

      static Map<String, String> backup_mp = new ConcurrentHashMap<String, String>();

      /*mp=> This ConcurrenHashMap store all key-value pair.
       * 	 And provides respective 'VALUE' for particular 'KEY'for 'GET' request.
       */
	  Socket sock;
	  public void set(Socket server){
		  this.sock=server;
	  }
	  
	  public void run()
	  {
		  InputStream is=null;
		  ObjectInputStream ois=null;
		  ObjectOutputStream oos = null;
		  
		try {
			/*
			 * Here first conenction with peer gets established.
			 * 			 */
			is = sock.getInputStream();
			ois = new ObjectInputStream(is);
		    OutputStream outToServer = sock.getOutputStream();         
	        oos=new ObjectOutputStream(outToServer);	

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*
		 * After successful connection with InputStream and OutPutStream,
		 * it will goes in infinte while loop to provide continue service.
		 */
		
		  while(true){
			  String searchKey;//=new String();
			  String size;
			  StringBuilder sb;
			  try {
				  searchKey=(String)ois.readObject();
				  if(searchKey!=null)
				  {						  
					  /*Follwoing IF condtion evaluate to TRUE only
					   * for one time, while connecting with peer at first time. */
					  if(searchKey.equals("request"))
						  	  oos.writeObject("connect");	  
					  else
					  	{					 
 							  String key=(searchKey.toString()).split("##")[0];
 							  String op=(searchKey.toString()).split("##")[1];
 							  //If Peer requested GET operation then
 							  //following IF condition get executed.
 							  if(op.equals("GET"))
							  {
						          if (mp.get(key)==null)
						        	  oos.writeObject("NotFound");
						          else
						        	  oos.writeObject(mp.get(key));
							  }
 							  else 	if(op.equals("BACKUP_GET"))
							  {
						          if (backup_mp.get(key)==null)
						        	  oos.writeObject("NotFound");
						          else
						        	  oos.writeObject(backup_mp.get(key));
							  } 								 
							  else if(op.equals("PUT"))
							  {
								  /*KEY and VALUE are appended with delimiter '@@'.
								   * Following code seperates KEY and VALUE from string
								   * and store it in 'keyS' and 'valS' respectively.
								   */
								  String keyS=key.split("@@")[0];
								  String valS=key.split("@@")[1];
								  if(mp.get(keyS)==null)
									  mp.put(keyS, valS);
								  else{
									  StringBuffer temp=new StringBuffer(mp.get(keyS));
									  temp.append("xmxmxm").append(valS);
									  mp.put(keyS,temp.toString());
								  }
						          oos.writeObject("Registered");
							  }
							  else if(op.equals("DEL"))
							  {
								  mp.remove(key);
						          oos.writeObject("Deleted");
							  }
							  else if(op.equals("DOWN"))
							  {								  
					        	  	String returnResult=key;						        	  			
					        	  	String IPPORTA=(returnResult.toString()).split("wwwww")[0];
		        	  			    String temp=(returnResult.toString()).split("wwwww")[1];
		        	  			    String fadd=temp.split("qqqqq")[0];
		        	  			    temp=temp.split("qqqqq")[1];
		        	  			    String fname=temp.split("rrrrr")[0];
		        	  			    String fsize=temp.split("rrrrr")[1];
		        	  			    downloadFile("BackupFiles",IPPORTA.split(":")[0],IPPORTA.split(":")[1],fadd,fname,Integer.parseInt(fsize));
		        	  			    oos.writeObject("Downloaded");
							  }
 					}							  						  
				  }
			} catch (IOException e) {
				System.out.println("Server error");
			} catch (ClassNotFoundException e) {
				System.out.println("Server error");
			}
		  }
	  }	 	
	   
		 public static void downloadFile(String foldername,String ip,String port,String filepath,String fname,int fsize){
			    Socket sock;
			    String pathName = new String();
				try {
					sock = new Socket(ip,Integer.parseInt(port));
					
					//Sending File name to server which peer want to download				
					OutputStream outToServer = sock.getOutputStream();         
			        ObjectOutputStream oos=new ObjectOutputStream(outToServer);	
			        oos.writeObject(filepath);
					
			      //DownloadedFiles => Folder name file will get download  
			  	  String FolderName=foldername; 
				  Path currentRelativePath = Paths.get("");
				  String s = currentRelativePath.toAbsolutePath().toString();
				  pathName=s+"/"+FolderName+"/"+fname;				  	    
				  /////////////////////////////////////////////////////////			  
					File severDirectory = new File(s+"/"+FolderName);	
					// if the directory does not exist, create it
					if (!severDirectory.exists()) {
					    boolean result = false;				
					    try{
					    	severDirectory.mkdir();
					        result = true;
					    } 
					    catch(SecurityException se){
					        //handle it
					    }        
					}
				  ///////////////////////////////////////////////////////
				  
				/*Check file is already available at DownloadedFiles.
				  If present then modify file name*/
					File f = new File(pathName);
					if(f.exists()){
						String justFilename=fname.split("\\.")[0];
						String extension=fname.split("\\.")[1];
						
						int randomInt = randomGenerator.nextInt(100);
						pathName=s+"/"+FolderName+"/"+justFilename+"_"+randomInt+"."+extension;
					}
							
				    InputStream is = sock.getInputStream();
				    FileOutputStream fos = new FileOutputStream(pathName);
				    BufferedOutputStream bos = new BufferedOutputStream(fos);    
				        
				    int convertByteIntoMB=1024*1024;
				    float filesizeByte=fsize;
				    float fileSizeMB=filesizeByte/convertByteIntoMB;
				    float fileSizeKB=filesizeByte/1024;
				    boolean showSizeInMB;// = true;
				    int divideConstant;
				    float filesize;
				    if(fileSizeMB>1)
				    {
				    	showSizeInMB=true;
				    	divideConstant=1024*1024;	
				    	filesize=fileSizeMB;
				    }
				    else{
				    	showSizeInMB=false;
				    	divideConstant=1024;
				    	filesize=fileSizeKB;
				    }
				    int count=1;
				    int dataSize;
				    if(fsize<SocketConstants.streamConstant)
				    	dataSize=fsize;
				    else
				    	dataSize=SocketConstants.streamConstant;
				              
				    byte[] mybytearray = new byte[dataSize];
				    int bytesRead;
				    
				    do{
					    bytesRead = is.read(mybytearray, 0,dataSize);
					    if(bytesRead>1)
					    bos.write(mybytearray, 0, bytesRead);
					    count++;
				    }while(bytesRead>0);
				    bos.close();
				    sock.close();
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			     String completeaddress=ip+":"+port+"wwwww"+pathName+"qqqqq"+fname+"rrrrr"+fsize;			 	   
				 backup_mp.put(fname,completeaddress);
		 }	  
}