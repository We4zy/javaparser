package RemoteFileParse;
import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;
import java.nio.channels.FileLockInterruptionException;
import javax.xml.ws.http.HTTPException;
import org.json.JSONObject;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import com.google.gson.Gson;

import Models.Payment;
import Models.PaymentPost;

import java.io.*;
//import org.apache.http.impl.client.HttpClientBuilder;
import java.time.*;
import org.apache.http.*;
import regEx.parser.*;

/**
 * @author rory richter
 *
 */
public class RemoteFileParse {

	private static List<Map.Entry<String, Integer>> headerOrdinals;
	//local logging to custom named Event Log is mandatory as well as the remote logging to us via our API
	private static final Logger logger = Logger.getLogger(RemoteFileParse.class.getName());
	private static PaymentPost<Payment> paymentPost;
	
	public static void main(String[] args) {
		//Check incoming variables for the command line call.  We will send in 
		// some basic values like BuyerId, AlertEmail, Maybe REST-ful service url root for remote logging, etc
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
		      public void uncaughtException(Thread t, Throwable e) {
		        logger.log(Level.SEVERE, t + " RemoteFileParse threw an exception: ", e);
		      };
		  });
		
		if (args != null && args.length >= 3) {
			//Set the Buyer object Model with the incoming args provided in the command line args IE.  IPRemoteParse.exe 
			Models.Buyer buyerInfo = new Models.Buyer(Integer.parseInt(args[0]), args[1], args[2], args[3]);
			
			//check for a file.  Haven't decided how to engage the scheduling as of yet.
			parseFiles(buyerInfo);
		}
		else { //just for testing stubbing out the buyer object. args would normally be sent in on a command line call or .bat file which will be created on install with all user options (or xml config file)
			Models.Buyer buyerInfo = new Models.Buyer(1926420, "intelligentpay-api.inworks.com", "C:\\Users\\grichter.EVIL_EMPIRE\\Desktop\\ParseTesting", "Payments_");
			parseFiles(buyerInfo);
		}
	}
	 
	public static String parseFiles(Models.Buyer buyerInfo) {		
        BufferedReader br = null;
        String line = "";
		String[] columns;
		List<Payment> payments = null;
		Boolean headerRow = true;
		
        	//Get all possible files in the configured directory that match our file mask and iterate, parse and send json payload for each
        for (File file:findFileByMask(buyerInfo)) {	
    		headerOrdinals = new ArrayList<Map.Entry<String, Integer>>();
    		paymentPost = new PaymentPost<Payment>();
        	try {
        		headerRow = true;
        		payments = new ArrayList<Payment>();
	        	String[] columnHeaders = null;
	            br = new BufferedReader(new FileReader(file.getPath()));
	            csvParser parser = new csvParser();
	            while ((line = br.readLine()) != null) {
	                //split each line utilizing the RegEx pattern which will perfectly split CSV's and ignore commas within quotes or tics for a string field
	            	columns = parser.parse(line);
	                            	
	                //for the first row, grab the column header names and match against our container object, each iteration thereafter, just stuff values in our object[] which will become a json payload
	                if (!headerRow) { 
	                	Payment invoice = new Payment();
	                	
	                	try {
	                		//match the column header key string in map to the property in our container object.  Then use ordinal in map to snatch correct value from the String[]
	                		Method methodProperty;
	                		for (Map.Entry<String, Integer> colMap:headerOrdinals) {
	                			methodProperty = invoice.getClass().getMethod(String.format("set%s", colMap.getKey()), new Class[] {String.class});
	                			
	                			//dynamically set the matching property in our payment container object by matching the previously fetched column headers
	                			if (methodProperty !=  null) methodProperty.invoke(invoice, columns[colMap.getValue()]);
	                		}
	                			//Our payment object should now be populated from the parsed line, now add to the collection that we shall POST to API as JSON.
	                			payments.add(invoice);	                			                		
	                	} catch (Exception ex) {
	                		//log any exception mapping fields
            				//log any problems locally as well as remotely.  Need to make an object to hold all needed fields to POST to our API for logging all
            				// of this activity out in the field
            				//This here will be a log of a failure to stuff the column value into the property data type, which shouldn't happen at all really.	                		
	                	}	                	
	                }
	                else {
	                	//we will take the 1st row in the csv file and do a sanity check of header column names against our container object utilizing reflection.  This will make for
	                	// only clean and matching data to be sent to our API via json.  
	                	columnHeaders = columns;
	                	if (!matchHeadersAgainstModel(columnHeaders, file.getName())) {
	    					logger.log(Level.WARNING, String.format("No columns matched.  It would appear there is no column header row in file %s.", file.getName()), Thread.currentThread().getStackTrace());	   
	    					//send this message to us via api
	    					//we could proceed with a generic JObject push here if we really wanted to still see the data and just dump it to us.
	                	}
	                	headerRow = false;
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
		            	double invoiceTotal = 0.0;
		                try { 
		                    br.close();
		                    for (Models.Payment payment:payments) invoiceTotal += Double.parseDouble(payment.getDocAmount());
		                    
		                	String successMessage = String.format("Buyer %s: File %s parsed without errors on %s.  Invoice count %s.  Payment file total $%s", buyerInfo.getBuyerId(), file.getName(), Instant.now(), payments.size(), invoiceTotal);
		                	logger.log(Level.INFO, successMessage, new Object[] {});
		                	paymentPost.addAMessage(successMessage);
		                } catch (IOException e) {
		                	logger.log(Level.WARNING, String.format("IOException when attempting to access payment file for processing : %s", e.getMessage()), Thread.currentThread().getStackTrace());
		                    e.printStackTrace();
		                    //again, log locally and to us via API any problems here
		                }
		            }
		        }
		
		        String json = new Gson().toJson(payments);
		        //the above payload is the payload to send the payment batch to our API
				logger.log(Level.INFO, "JSON Payload to send " + json, new Object[] {});
				
				//POST our beautifully constructed invoice[] as JSON to our API
		        try {
		        	//paymentPost.setPayments((Payment[])payments.toArray());
		        	//We want the payment batch to be processed as a batch for good batch logging, so each file's payload should be posted to the API separately as json for adequate batch processing
		        	
		        	//HttpClient httpClient = HttpClientBuilder.create().build();
		        	//HttpPost post = new HttpPost(buyerInfo.getApiLogUrl());        	
		        	// post.invoke(new Gson().toJson(payments));
		        } catch (HTTPException ex) {
		        	
		        }
        }
        
		return new Gson().toJson(payments);
	}
	
	//for the visible file parse test on initial install
	public static List<Models.Payment> parseFile(Models.Buyer buyerInfo) {
		
        BufferedReader br = null;
        String line = "";
		String[] columns;
		List<Models.Payment> payments = null;		

		Boolean headerRow = true;
		
    	//Get all possible files in the configured directory that match our file mask and iterate, parse and send json payload for each
		headerOrdinals = new ArrayList<Map.Entry<String, Integer>>();
    	try {
    		headerRow = true;
    		payments = new ArrayList<Models.Payment>();
        	String[] columnHeaders = null;
        	
        	File file = findFileByMask(buyerInfo)[0];
            br = new BufferedReader(new FileReader(file.getPath()));
            csvParser parser = new csvParser();
            while ((line = br.readLine()) != null) {
                //split each line utilizing the RegEx pattern which will perfectly split CSV's and ignore commas within quotes or tics for a string field
            	columns = parser.parse(line);
                            	
                //for the first row, grab the column header names and match against our container object, each iteration thereafter, just stuff values in our object[] which will become a json payload
                if (!headerRow) { 
                	Models.Payment invoice = new Models.Payment();
                	
                	try {
                		//match the column header key string in map to the property in our container object.  Then use ordinal in map to snatch correct value from the String[]
                		for (Map.Entry<String, Integer> colMap:headerOrdinals) {
                			Method methodProperty = invoice.getClass().getMethod(String.format("set%s", colMap.getKey()), new Class[] {String.class});
                			
                			//dynamically set the matching property in our payment container object by matching the previously fetched column headers
                			if (methodProperty !=  null) methodProperty.invoke(invoice, columns[colMap.getValue()]);
                		}
                			//Our payment object should now be populated from the parsed line, now add to the collection that we shall POST to API as JSON.
                			payments.add(invoice);                	
                	} catch (Exception ex) {
                		//log any exception mapping fields
        				//log any problems locally as well as remotely.  Need to make an object to hold all needed fields to POST to our API for logging all
        				// of this activity out in the field
        				//This here will be a log of a failure to stuff the column value into the property data type, which shouldn't happen at all really.                		
                	}	                	
                }
                else {
                	//we will take the 1st row in the csv file and do a sanity check of header column names against our container object utilizing reflection.  This will make for
                	// only clean and matching data to be sent to our API via json.  
                	columnHeaders = columns;
                	if (!matchHeadersAgainstModel(columnHeaders, file.getName())) {
    					logger.log(Level.WARNING, String.format("No columns matched.  It would appear there is no column header row in file %s.", file.getName()), Thread.currentThread().getStackTrace());	   
    					//send this message to us via api
    					//we could proceed with a generic JObject push here if we really wanted to still see the data and just dump it to us.
                	}
                	headerRow = false;
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
	
	        String json = new Gson().toJson(payments);
	        //the above payload is the payload to send the payment batch to our API	
			logger.log(Level.INFO, "Json Payload to send: " + json, new Object[] {});
			
			//POST our beautifully constructed invoice[] as JSON to our API
	        try {
	        	//We want the payment batch to be processed as a batch for good batch logging, so each file's payload should be posted to the API separately as json for adequate batch processing
	        	
	        	//HttpClient httpClient = HttpClientBuilder.create().build();
	        	//HttpPost post = new HttpPost(buyerInfo.getApiLogUrl());        	
	        	// post.invoke(new Gson().toJson(payments));
	        } catch (HTTPException ex) {
	        	
	        }
    
    
	        return payments;
	}	
	
	//A little helper method to match the column header names against our payment object just for a sanity check.  This way we can log any discrepancies.
	//This entire method is really only a sanity check to enable verbose and concise logging on both sides, out in the field and to us via API
	public static boolean matchHeadersAgainstModel(String[] columnHeaders, String fileName)
	{
		Integer discrepencyCount = 0;
		String errorMessage = "";
		Method methodName = null;
		Integer foundCount = 0; 
		Integer i = 0;
		 
		Models.Payment payment = new Models.Payment();
		//Iterate through all the methods, identify setters then attempt to match the property name to the column header name
		 for (String column:columnHeaders) {
			 //Making a basic List of tuples to use as a mapping matrix to pluck out the values dynamically 
			 //when we actually populate a list of objects in the parseFile method 
			Map.Entry<String, Integer> headerOrdinal = new AbstractMap.SimpleEntry<String, Integer>(column, i);
			headerOrdinals.add(headerOrdinal); i++;
			  
			try {
				//Attempt to find the setter within our model container object.  If not we need report this to local log and remote log to us via API
				methodName = payment.getClass().getMethod(String.format("set%s", column),  new Class[] {String.class});
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
		else {
		//errorMessage = String.format("File %s mapped perfectly.  No file format errors found on %s", fileName, Instant.now());
		logger.log(Level.INFO, String.format("File %s mapped perfectly.  No file format errors found on %s", fileName, Instant.now()), new Object[] {});
		}
		//log the results locally and send collected info to our API for remote logging
		if (foundCount == 0) return false;
		else return true;
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
				logger.log(Level.SEVERE, "There was an error while attempting to access the configured directory for payment file processing", Thread.currentThread().getStackTrace());
				e.printStackTrace();
				//Log this locally in custom event log and send.  Log(e.printStackTrace());
			} 
			finally {
				String filesMessage = String.format("Payment files found to be processed on %s, files : %s", Instant.now(), Arrays.toString(files));
				logger.log(Level.INFO, filesMessage, Thread.currentThread().getStackTrace());     	
				//log the number and names of the found files.  Log via our API.  "Buyer ID = <> found" + files.count()  + " files on <TimeStamp> to process.  Files found : " + files.toString();
        }
		
		return files;
		}	
}
