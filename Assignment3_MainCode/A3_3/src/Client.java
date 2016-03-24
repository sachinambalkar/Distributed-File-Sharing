import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.Choice;
import java.awt.List;
import java.io.*;

import javax.swing.plaf.SliderUI;

public class Client
{
	public static String localIpandPortAddress;
	static ArrayList<ObjectInputStream> ois_list;
	static ArrayList<ObjectOutputStream> oos_list;
	static Random randomGenerator = new Random();  

	private	static ConcurrentHashMap<String,ArrayList<ConcurrentHashMap<String,String>>> fileDetails;
	static ArrayList<ConcurrentHashMap<String, String>> filelist=new ArrayList<ConcurrentHashMap<String, String>>(); 
    static int ServerPort;
    static int 	UploadServerPort;
	
	
	   public static void main(String [] args)
	   {	   	
		    Properties property=new Properties();
		    fileDetails=new ConcurrentHashMap<String,ArrayList<ConcurrentHashMap<String,String>>>();
		    Boolean runCode=true;
		    int  totalPeer=0;
		 	try 
		 	{
			
				property.load(new FileInputStream(new File("config.property")));
			    ServerPort=Integer.parseInt(property.getProperty("ServerPort"));
			    UploadServerPort=Integer.parseInt(property.getProperty("UploadServerPort"));

//		 		if(false)
		 		{
			      ois_list=new ArrayList<ObjectInputStream>();
			      oos_list=new ArrayList<ObjectOutputStream>();			    	
				 /*	ois_list & oos_list =>
				  * Two arraylist each for ObjectInputStream and 
				  * ObjectOutputStream are created.
				  * All connected socket details are stored in this two array list. 
				  * */
			      PeerUploadServer peerUploadServer=new PeerUploadServer();
			      peerUploadServer.start();
			      
			      PeerServer peerServer = new PeerServer();
			      peerServer.start();
				   
				  totalPeer=Integer.parseInt(property.getProperty("TotalPeer"));	      	
					/* totalPeer =>	
					 * 		1.	As per instruction, we need to connect to total 8 peers.
					 * 			The value is fetched from "config.property" file.
					 * 		2.	If we changed value of this variable to 5,
					 * 			then it will connect to only 5 peers.
					 * 		3.  Depending on value of this variable, hashcode decides
					 * 			which server should select to PUT/GET/DELETE key.
							4. 	Following code is at line 147.
											
									long hashcode= new Client().ComputeHashCode(key2search);			          			     
							   		int ServerSelected=(int)(hashcode%totalPeer);
							   							        	
							     	Here 'hashcode' is gets compute based on key value.
							     	Then on this value, we perform '%' (mod) operation,
							     	to get specific server.
					 */
			        
			      for(int index=1;index<(totalPeer+1);index++){
			    	  runCode=true;
			    	  String ClientSocketAddress=property.getProperty("Client"+index);
			    	  /*ClientSocketAddress=> It retrives peer's IP and PORT address
			    	   * 					  for 'config.property' file.			    	   * 
			    	   */			    	  
				      String  clientIP=ClientSocketAddress.split(":")[0];
				      int  clientPort=Integer.parseInt(ClientSocketAddress.split(":")[1]);
				      Socket clientSocket=null;
			    	  try{
			          clientSocket = new Socket(clientIP,clientPort);
			    	  }catch(Exception e){
			    		  
			    		  /*Following conding are added to continously perform same
			    		   * operation untill gets connected to all peers.
			    		   **/
			    		  runCode=false;
			    		  index--;
			    		  try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			    	  }
			    	  if(runCode){			    		  
							/*
							 * Follwoing code establish connection with
							 * all peers listed in 'config.property' file.			    		  
							 */
			     		      OutputStream outToServer = clientSocket.getOutputStream();         
					          ObjectOutputStream oos=new ObjectOutputStream(outToServer);	
					          oos.writeObject("request");					          
					    	  InputStream is = clientSocket.getInputStream();
					    	  ObjectInputStream ois = new ObjectInputStream(is);
					    	  String returnResult=(String)ois.readObject();
     					      System.out.println("Connected to Server "+index);			    	  					    	  
					    	  ois_list.add(index-1,ois);
					    	  oos_list.add(index-1,oos);
			    	  }
			      }
		 	}
			/* After successful connection with all peers,
			 * following options will display :
			 * 
			 * 		1.Get
			 * 		2.Put
			 * 		3.Delete
			 *      4.BackUp_get
			 * 		Enter choice: 			      
			 */
			      
	         updateIndexfile();     

	         ////////////////////////////////////
	         StringBuffer searchkey;	         
	         int backupServer = 0;
	         StringBuffer backupData=new StringBuffer();
	         if(!filelist.isEmpty())
	        	 System.out.println("\nRegistered files are");
	         int k=1;
		       for(ConcurrentHashMap<String, String> filedetailsVALUE:filelist)
		       {
		    	   String filenameKEY=filedetailsVALUE.get(SocketConstants.FILENAME);
		    	   String COMPLETEADDRESS=filedetailsVALUE.get(SocketConstants.COMPLETEADDRESS);
		    	   searchkey=new StringBuffer(filenameKEY);		        	  
		    	   searchkey.append("@@"+COMPLETEADDRESS+"##"+"PUT");
		    	   backupData=new StringBuffer();
		    	   backupData.append(COMPLETEADDRESS+"##"+"DOWN");
		      		 StringBuffer returnResult = null;
					 if(searchkey!=null)
					 {
						  	long hashcode= new Client().ComputeHashCode(filenameKEY);			          			     
							int ServerSelected=(int)(hashcode%totalPeer);
						  	ServerSelected++;
							(oos_list.get(ServerSelected-1)).writeObject(searchkey.toString());
						  	returnResult=new StringBuffer((String)(ois_list.get(ServerSelected-1)).readObject());
	                        System.out.println((k++)+". "+filenameKEY);
							backupServer=ServerSelected-1;							
						  	if(backupServer==0)
						  			backupServer=totalPeer-1;
						  	else
						  		backupServer--;

							(oos_list.get(backupServer)).writeObject(backupData.toString());
						  	returnResult=new StringBuffer((String)(ois_list.get(backupServer)).readObject());		          		      		          		          		  						  	
					  }
		       }
		       
	         ///////////////////////////////////
		         
//   if(false)
   {	         
		       
		       Scanner sc=new Scanner(System.in);	
		       String key2search = null;
		       StringBuffer SearchKey = null,value=null,choice;
		       do
		       {
		          System.out.println("\n1.Obtain\n2.Register\n3.Search");
		          System.out.print("Enter choice : ");
		          int selection;
		          try{
		          choice=new StringBuffer(sc.next());
		          selection=Integer.parseInt(choice.toString());
		          }catch(Exception e){
		        	  selection=5;
		          }
		          
		          switch(selection)
		          {
			          case 1: System.out.print("Enter key : ");
			          		  key2search=sc.next();
			          		  SearchKey=new StringBuffer(key2search);		        	  
			          		  SearchKey.append("##"+"GET");
			          		  break;
			          case 2: System.out.print("Enter key : ");
			          		  key2search=sc.next();
			          		  SearchKey=new StringBuffer(key2search);		        	  
			    			  String FolderName="ServerFiles"; 
			    			  Path currentRelativePath = Paths.get("");
			    			  String s = currentRelativePath.toAbsolutePath().toString();
			    			  String foldername=s+"/"+FolderName;
			    			  String fileName=foldername+"/"+key2search;
			    			  System.out.println("path: "+fileName);
			    			  if(new File(fileName).exists()){
			    				   String ipPort=SocketConstants.localIPaddress+":"+UploadServerPort;
                                   String completeaddress=ipPort+"wwwww"+fileName+"qqqqq"+key2search+"rrrrr"+(new File(fileName)).length();
                                   StringBuffer skey=new StringBuffer(key2search);		        	  
       					    	   skey.append("@@"+completeaddress+"##"+"PUT");
						    	   backupData=new StringBuffer();
						    	   backupData.append(completeaddress+"##"+"DOWN");			    				 
								  	long hashcode= new Client().ComputeHashCode(key2search);
									int ServerSelected=(int)(hashcode%totalPeer);
								  	ServerSelected++;
									(oos_list.get(ServerSelected-1)).writeObject(skey.toString());
									StringBuffer returnResult=new StringBuffer((String)(ois_list.get(ServerSelected-1)).readObject());
								  	System.out.println("Result "+returnResult);
									backupServer=ServerSelected-1;							
								  	if(backupServer==0)
								  			backupServer=totalPeer-1;
								  	else
								  		backupServer--;
									(oos_list.get(backupServer)).writeObject(backupData.toString());
								  	returnResult=new StringBuffer((String)(ois_list.get(backupServer)).readObject());		          		      		          		          		  
			    			  }	
			    			  else 
			    				  System.out.println("File Not Found");
			        	  	  break;
			          case 3:  System.out.print("Enter key : ");
			          		  key2search=sc.next();
			          		  SearchKey=new StringBuffer(key2search);		        	  
			          		  SearchKey.append("##"+"GET");
			          		  break;
			          default:
			        	  System.out.println("Invalid choice !!! ");
			        	  	break;		        	  	
		          }
	        	 long startTime = System.nanoTime();    
	        	 StringBuffer returnResult = null;
	        	 if(((selection==1)||(selection==3))&&SearchKey!=null)
	        	 {
		        	  	long hashcode= new Client().ComputeHashCode(key2search);			          			     
		        		int ServerSelected=(int)(hashcode%totalPeer);
		        	  	ServerSelected++;
		       		  	(oos_list.get(ServerSelected-1)).writeObject(SearchKey.toString());			          
	        		  	returnResult=new StringBuffer((String)(ois_list.get(ServerSelected-1)).readObject());		          		      		          		          		        	 
		        	  	String res=returnResult.toString();
		        	  	if(selection==3)
		        	  	{
			        	  		if(!res.equals("NotFound"))
			        	  		{
					        	  		if(res.contains("xmxmxm"))
					        	  		{
					        	  			System.out.println("File is available in total "+res.split("xmxmxm").length+" Servers as follows : ");		        	  
					        	  			System.out.println("File Details:\n");
					        	  			for(int i=0;i<res.split("xmxmxm").length;i++){
					        	  			   print_file_name(res.split("xmxmxm")[i],(i+1));
					        	  			}
					        	  		}
					        	  		else{System.out.println("File Details:\n");
					        	  			print_file_name(res,1);}
			        	  		}
			        	  		else
			        	  			System.out.println("File Not Found");
		        	  	}
		        	  	else if(selection==1)
		        	  	{		        	  		
		        	  		if(!res.equals("NotFound"))
		        	  		{
				        	  		if(res.contains("xmxmxm"))
				        	  		{
				        	  			System.out.println("File is available total "+res.split("xmxmxm").length+" Servers as follows : ");		        	  
				        	  			for(int i=0;i<res.split("xmxmxm").length;i++)
				        	  			   System.out.println((i+1)+": "+res.split("xmxmxm")[i]+"\n");
				        	  			System.out.print("Enter your choice: ");
				        	  			StringBuffer selectedSer=new StringBuffer(sc.next());
				        	  			int index=Integer.parseInt(selectedSer.toString());
				        	  			returnResult=new StringBuffer(res.split("xmxmxm")[index-1]);
				        	  		}
									  String IPPORTA=(returnResult.toString()).split("wwwww")[0];
									  System.out.println("ppppp "+IPPORTA+" pppp");
									  String temp=(returnResult.toString()).split("wwwww")[1];
									  String fadd=temp.split("qqqqq")[0];
									  temp=temp.split("qqqqq")[1];
									  String fname=temp.split("rrrrr")[0];
									  String fsize=temp.split("rrrrr")[1];
									  System.out.println("IPport:"+IPPORTA+" Addr:"+fadd+" Filename:"+fname);
									  int fail=downloadFile("DownloadedFiles",IPPORTA.split(":")[0],IPPORTA.split(":")[1],fadd,fname,Integer.parseInt(fsize));
									  
									  if(fail==0){
					        	  			int backupSer=ServerSelected-1;
					        	  			if(backupSer==0)
					        	  				backupSer=totalPeer-1;
					        	  			else
					        	  				backupSer--;
					        	  			StringBuffer searchdata=new StringBuffer();
					        	  			searchdata.append(key2search+"##"+"BACKUP_GET");
							        	  	System.out.println("Backup SelectedServer: "+backupSer);
							        	  	System.out.println("Datasend "+searchdata);
							       		  	(oos_list.get(backupSer)).writeObject(searchdata.toString());			          
						        		  	returnResult=new StringBuffer((String)(ois_list.get(backupSer)).readObject());		          		      		          		          		        	 
							        	  	System.out.println(" Result : "+returnResult.toString());
							        	  	res=returnResult.toString();
							        	  	if(!res.equals("NotFound")){
												  IPPORTA=(returnResult.toString()).split("wwwww")[0];
												  System.out.println("ppppp "+IPPORTA+" pppp");
												  temp=(returnResult.toString()).split("wwwww")[1];
												  fadd=temp.split("qqqqq")[0];
												  temp=temp.split("qqqqq")[1];
												  fname=temp.split("rrrrr")[0];
												  fsize=temp.split("rrrrr")[1];
												  System.out.println("IPport:"+IPPORTA+" Addr:"+fadd+" Filename:"+fname);
												  fail=downloadFile("DownloadedFiles",IPPORTA.split(":")[0],IPPORTA.split(":")[1],fadd,fname,Integer.parseInt(fsize));
							        	  	}
							        	  	if(fail==0)
							        	  		System.out
														.println("File not found");
									  }
		        	  		}else{
		        	  			System.out.println("File not found");
		        	  		}
		        	  	}		          }   	 
		       }while(true);
   }       
	       } catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}			
	   }
	   
	   /*
	    * Following function calculates HashCode for given 'key'.
	    * This function, calculates hashcode based on ASCII value of charactes.
	    */   
	   long ComputeHashCode(String key)
	   {
		   long hashcode=0,multiplier=10;		   
		   for(int i=0;i<key.length();i++)
		   {
			   hashcode+=key.charAt(i)+Math.pow(multiplier, i);		
		   }
		   	return hashcode;
	   }
	   
	   public static void print_file_name(String returnResult,int index)
	   {
			String IPPORTA=(returnResult.toString()).split("wwwww")[0];
			  String temp=(returnResult.toString()).split("wwwww")[1];
			  String fadd=temp.split("qqqqq")[0];
			  temp=temp.split("qqqqq")[1];
			  String fname=temp.split("rrrrr")[0];
			  String fsize=temp.split("rrrrr")[1];
			  System.out.println(index+". \tFile-Name: "+fname+"\n\tServer Address: "+IPPORTA+"\n\tFile-path: "+fadd);		   
	   }
	   
	   
	   public static void updateIndexfile(){
			  
			  String FolderName="ServerFiles"; 
			  Path currentRelativePath = Paths.get("");
			  String s = currentRelativePath.toAbsolutePath().toString();
			  String foldername=s+"/"+FolderName;

				File severDirectory = new File(foldername);	
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
				    if(result) {    
				        System.out.println(FolderName+" directory created");  
				    }
				}
			  listFilesForFolder(severDirectory);  		  
		  }

	   public static void listFilesForFolder(final File folder) 
	   {
		   filelist.clear();		   
	       String ipPortAddress=new String(SocketConstants.localIPaddress+":"+UploadServerPort);
	       for (final File fileEntry : folder.listFiles()) {
	           if (fileEntry.isDirectory()) {
	               listFilesForFolder(fileEntry);
	           } else {
	         	ConcurrentHashMap<String, String> filedetail=new ConcurrentHashMap<String, String>();
	         	String completeaddress=ipPortAddress+"wwwww"+fileEntry.getPath()+"qqqqq"+fileEntry.getName()+"rrrrr"+fileEntry.length();
	         	filedetail.put(SocketConstants.COMPLETEADDRESS, completeaddress);
	        	filedetail.put(SocketConstants.FILENAME, fileEntry.getName());	         	
	     		filelist.add(filedetail);
	           }
	       }
	   }	   
	   
	   
		 public static int downloadFile(String foldername,String ip,String port,String filepath,String fname,int fsize){

			    Socket sock;
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
				  String pathName=s+"/"+FolderName+"/"+fname;
				  	
				  
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
					    if(result) {    
					        System.out.println(FolderName+" directory created");  
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
					
					
				    sock.setSoTimeout(1000);
				    InputStream is;
				    try{
				    is = sock.getInputStream();
				    }catch(Exception e){
				    	return 0;
				    }
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
				    {	System.out.println("FileSize: "+ fileSizeMB+" mb");
				    	showSizeInMB=true;
				    	divideConstant=1024*1024;	
				    	filesize=fileSizeMB;
				    }
				    else{
				    	showSizeInMB=false;
				    	System.out.println("FileSize: "+ fileSizeKB+" kb");
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
				    System.out.println("\nDownloaded Successfully");
				    bos.close();
				    sock.close();
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0;
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0;
				}
				return 1;
			 
		 }

}