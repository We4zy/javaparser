package Models;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rory richter
 * This class is just a little wrapper class to hold that array of payments object (generic) and the invoice count and total in the wrapper just for less processing and ease of use at API side
 * More properties may get added to this object.  Things like a message collection of processing steps and/or errors might be prudent 
 */
public class PaymentPost<T> {
	private int invoiceCount;
	private double invoiceTotal;
	private T[] payments;
	private List<String> messages = new ArrayList<String>();
	
	public int getInvoiceCount() {
		return invoiceCount;
	}	
	public void setInvoiceCount(int invoiceCount) {
		this.invoiceCount = invoiceCount;
	}
	
	public double getInvoiceTotal() {
		return invoiceTotal;
	}
	public void setInvoiceTotal(double invoiceTotal) {
		this.invoiceTotal = invoiceTotal;
	}
	
	public T[] getPayments() {
		return payments;
	}
	public void setPayments(T[] payments) {
		this.payments = payments;
	}
	
	public String[] getMessages() {
		return (String[])messages.toArray();
	}
	public void addMessages(List<String> messages) {
		this.messages = messages;
	}
	public void addAMessage(String message) {
		messages.add(message);
	}
	
	public PaymentPost() {
	}
	public PaymentPost(int invoiceCount, double invoiceTotal, T[] payments) {
		this.invoiceCount = invoiceCount;
		this.invoiceTotal = invoiceTotal;
		this.payments = payments;
	}
}
