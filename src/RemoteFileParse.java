import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;
import java.nio.channels.FileLockInterruptionException;
import java.util.regex.Pattern;
import javax.xml.ws.http.HTTPException;

import org.json.JSONObject;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.HttpClientBuilder;
import java.time.*;
import org.apache.http.*;
import regEx.parser.*;

public class RemoteFileParse {

	private static List<Map.Entry<String, Integer>> headerOrdinals;
	//local logging to custom named Event Log is mandatory as well as the remote logging to us via our API
	private static final Logger logger = Logger.getLogger(RemoteFileParse.class.getName());
	
	public static void main(String[] args) {
		//Check incoming variables for the command line call.  We will send in 
		// some basic values like BuyerId, AlertEmail, Maybe REST-ful service name for remote logging, etc
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
		      public void uncaughtException(Thread t, Throwable e) {
		        logger.log(Level.SEVERE, t + " RemoteFileParse threw an exception: ", e);
		      };
		  });
		
		if (args != null && args.length > 3) {
			//Set the Buyer object Model with the incoming args provided in the command line args IE.  IPRemoteParse.exe 
			Models.Buyer buyerInfo = new Models.Buyer(Integer.parseInt(args[0]), args[1], args[2], args[3]);
			
			//check for a file.  Haven't decided how to engage the scheduling as of yet.
			parseFile(buyerInfo);
		}
		else { //just for testing stubbing out the buyer object. args would normally be sent in on a command line call or .bat file which will be created on install with all user options (or xml config file)
			Models.Buyer buyerInfo = new Models.Buyer(1926420, "intelligentpay-api.inworks.com", "C:\\Users\\grichter.EVIL_EMPIRE\\Desktop\\ParseTesting", "Payments_");
			parseFile(buyerInfo);
		}
	}
	 
	public static boolean parseFile(Models.Buyer buyerInfo) {		
        String csvFile = buyerInfo.getFilePath();
        BufferedReader br = null;
        String line = "";
		String[] columns; Boolean ret = true;
		List<Models.Payment> payments = new ArrayList<Models.Payment>();
		headerOrdinals = new ArrayList<Map.Entry<String, Integer>>();
		Boolean headerRow = true;
		
        try {
        	//Get all possible files in the configured directory that match our file mask and iterate, parse and send json payload for each
        	for (File file:findFileByMask(buyerInfo)) {
        	
	        	String[] columnHeaders = null;
	            br = new BufferedReader(new FileReader(file.getPath()));
	            while ((line = br.readLine()) != null) {
	                //split each line utilizing the RegEx pattern which will perfectly split CSV's and ignore commas within quotes or tics for a string field
	            	columns = new csvParser().parse(line);
	                            	
	                //for the first row, grab the column header names and match against our container object, each iteration thereafter, just stuff values in our object[] which will become a json payload
	                if (!headerRow) { 
	                	Models.Payment invoice = new Models.Payment();
	                	
	                	try {
	                		//match the column header key string in map to the property in our container object.  Then use ordinal in map to snatch correct value from the String[]
	                		Integer i = 0;
	                		for (Map.Entry<String, Integer> colMap:headerOrdinals) {
	                			Method methodProperty = invoice.getClass().getMethod(String.format("set%s", colMap.getKey()), new Class[] {String.class});
	                			
	                			try {
	                				//dynamically set the matching property in our payment container object by matching the previously fetched column headers
	                			    if (methodProperty !=  null) methodProperty.invoke(invoice, columns[colMap.getValue()]);
	                				i++;
	                			} catch (Exception ex) {
	                				//log any problems locally as well as remotely.  Need to make an object to hold all needed fields to POST to our API for logging all
	                				// of this activity out in the field
	                				//This here will be a log of a failure to stuff the column value into the property data type, which shouldn't happen at all really.
	                			}
	                			//Our payment object should now be populated from the parsed line, now add to the collection that we shall POST to API as JSON.
	                			payments.add(invoice);
	                		}
	                	
	                	} catch (Exception ex) {
	                		//log any exception mapping fields
	                	}
	                	
	                }
	                else {
	                	//we will take the 1st row in the csv file and do a sanity check of header column names against our container object utilizing reflection.  This will make for
	                	// only clean and matching data to be sent to our API via json.  
	                	columnHeaders = columns;
	                	matchHeadersAgainstModel(columnHeaders);
	                	headerRow = false;
	                }             
            	}
        	}
        } catch (FileNotFoundException e) {
			logger.log(Level.WARNING, String.format("No file found to process : %s", e.getMessage()), Thread.currentThread().getStackTrace());
            e.printStackTrace();
            //Log this locally in custom event log and send
        } catch (IOException e) {
			logger.log(Level.WARNING, String.format("IOException when attempting to access payment file for processing : %s", e.getMessage()), Thread.currentThread().getStackTrace());
            e.printStackTrace();
            //log both locally and send log to us via API call
        } finally {
            if (br != null) {
                try { 
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    //again, log locally and to us via API any problems here
                }
            }
        }
		//POST our beautifully constructed payment[] as JSON to our API
        try {
        	//HttpClient httpClient = HttpClientBuilder.create().build();
        	//HttpPost post = new HttpPost(buyerInfo.getApiLogUrl());        	
        	
        } catch (HTTPException ex) {
        	
        }
        
		//using gson to convert object List<> to json
        String json = new Gson().toJson(payments);
        //the above payload is the payload to send the payment batch to our API
		System.out.println(json);	
		return true;
	}
	
	//A little helper method to match the column header names against our payment object just for a sanity check.  This way we can log any discrepancies.
	//This entire method is really only a sanity check to enable verbose and concise logging on both sides, out in the field and to us via API
	public static void matchHeadersAgainstModel(String[] columnHeaders)
	{
		Integer discrepencyCount = 0;
		String errorMessage = "";
		Method methodName = null;
		Integer foundCount = 0; 
		Integer i = 0;
		 
		//Iterate through all the methods, identify setters then attempt to match the property name to the column header name
		 for (String column:columnHeaders) {
			 //Making a basic List of tuples to use as a mapping matrix to pluck out the values dynamically 
			 //when we actually populate a list of objects in the parseFile method 
			Map.Entry<String, Integer> headerOrdinal = new AbstractMap.SimpleEntry<String, Integer>(column, i);
			headerOrdinals.add(headerOrdinal); i++;
			  
			try {
				//Attempt to find the setter within our model container object.  If not we need report this to local log and remote log to us via API
				methodName = new Models.Payment().getClass().getMethod(String.format("set%s", column),  new Class[] {String.class});
			} catch (NoSuchMethodException e) {
				errorMessage += String.format("Unknown column header named: %s was found in payment file;  Cannot map this column to our invoice object. \n", column);
				logger.log(Level.WARNING, errorMessage, Thread.currentThread().getStackTrace());
				e.printStackTrace(); //log the stack trace to local log.  Probably don't need this info sent to us via API
			} catch (SecurityException e) {
				errorMessage += String.format("Current security context disallowed access to the invoice object, column header attempted to map: %s; \n", column);
				logger.log(Level.WARNING, errorMessage, Thread.currentThread().getStackTrace());
				e.printStackTrace();
			}
			  //some extra checks for some more concise and verbose logging
			  if (methodName != null) {
				  foundCount++;			  
			  }
			  else { //we didn't find a setter property for the column header name, which we should have
		    		errorMessage += String.format("Unknown column header named: %s was found in payment file; \n", column);	
					logger.log(Level.WARNING, errorMessage, Thread.currentThread().getStackTrace());
			  }
		 }
		
		if (foundCount < columnHeaders.length) { 
			discrepencyCount = columnHeaders.length - foundCount;
			errorMessage += String.format(" Not all column headers found were mappable.  There are %s extra column headers in file. ", discrepencyCount.toString());
			logger.log(Level.WARNING, errorMessage, Thread.currentThread().getStackTrace());
		}
		
		errorMessage = "File found mapped perfectly.  No file format errors found." + Instant.now();
		System.out.println(errorMessage);
		//log the results locally and send collected info to our API for remote logging
	}
	
	public static File[] findFileByMask(Models.Buyer buyerInfo) {
		File dir = new File(buyerInfo.getFilePath());
		File[] files = null;
		//Start Filter
		FilenameFilter fileMaskFilter = new FilenameFilter() {
			public boolean accept(File file, String name) {
				if (name.contains(buyerInfo.getFileMask())) {
					return true;
				} else {
					return false;
				}
			}
		};
		
		try {
				//find the files matching the user defined and pre-configured file mask pattern
				files = dir.listFiles(fileMaskFilter);			
			} catch (Exception e) {
				logger.log(Level.WARNING, "There was an error while attempting to access the configured directory for payment file processing", Thread.currentThread().getStackTrace());
				e.printStackTrace();
				//Log this locally in custom event log and send.  Log(e.printStackTrace());
			} 
			finally {
				String filesMessage = String.format("Payment files found to be processed on %s, files : %s", Instant.now(), Arrays.toString(files));
				logger.log(Level.FINE, filesMessage, Thread.currentThread().getStackTrace());     	
				System.out.println(filesMessage);
				//log the number and names of the found files.  Log via our API.  "Buyer ID = <> found" + files.count()  + " files on <TimeStamp> to process.  Files found : " + files.toString();
        }
		
		return files;
		}	
}
