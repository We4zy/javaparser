package Models;

/**
 * @author rory richter
 *
 */
public class Payment {
	private String clientId;
	private String supplierId;
	private String supplierName;
	private String supplierAddressLine1;
	private String supplierAddressLine2;
	private String supplierCity;
	private String supplierState;
	private String supplierZip;
	private String supplierEmail;
	private String docNumber;
	private String docDate;
	private String docType;
	private String docAmount;
	private String reference;
	private String bankAccountId;
	private String pmtRef;
	private String _void;
	
	//access modifiers
	public String getClientID() {
		return clientId;
	}

	@ColumnAliases(aliases={"clientId", "clientID"})
	public void setClientID(String clientId) {
		this.clientId = clientId;
	}

	public String getSupplierID() {
		return supplierId;
	}

	@ColumnAliases(aliases={"supplierId", "supplierID"})
	public void setSupplierID(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getSupplierName() {
		return supplierName;
	}

	@ColumnAliases(aliases={"supplierName"})	
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getSupplierAddressLine1() {
		return supplierAddressLine1;
	}

	@ColumnAliases(aliases={"supplierAddressLine1, supplierAddressLine"})		
	public void setSupplierAddressLine1(String supplierAddressLine1) {
		this.supplierAddressLine1 = supplierAddressLine1;
	}

	public String getSupplierAddressLine2() {
		return supplierAddressLine2;
	}

	@ColumnAliases(aliases={"supplierAddressLine2"})		
	public void setSupplierAddressLine2(String supplierAddressLine2) {
		this.supplierAddressLine2 = supplierAddressLine2;
	}

	public String getSupplierCity() {
		return supplierCity;
	}

	@ColumnAliases(aliases={"supplierCity"})	
	public void setSupplierCity(String supplierCity) {
		this.supplierCity = supplierCity;
	}

	public String getSupplierState() {
		return supplierState;
	}

	@ColumnAliases(aliases={"supplierState"})	
	public void setSupplierState(String supplierState) {
		this.supplierState = supplierState;
	}

	public String getSupplierZip() {
		return supplierZip;
	}

	@ColumnAliases(aliases={"supplierZip"})	
	public void setSupplierZip(String supplierZip) {
		this.supplierZip = supplierZip;
	}

	public String getSupplierEmail() {
		return supplierEmail;
	}

	@ColumnAliases(aliases={"supplierEmail"})		
	public void setSupplierEmail(String supplierEmail) {
		this.supplierEmail = supplierEmail;
	}

	public String getDocNumber() {
		return docNumber;
	}

	@ColumnAliases(aliases={"docNumber"})	
	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
	}

	public String getDocDate() {
		return docDate;
	}

	@ColumnAliases(aliases={"docDate"})	
	public void setDocDate(String docDate) {
		this.docDate = docDate;
	}
	
	public String getDocType() {
		return docType;
	}

	@ColumnAliases(aliases={"docType"})		
	public void setDocType(String docType) {
	    this.docType = docType;
	}

	public String getDocAmount() {
		return docAmount;
	}

	@ColumnAliases(aliases={"docAmount"})		
	public void setDocAmount(String docAmount) {
		this.docAmount = docAmount;
	}

	public String getReference() {
		return reference;
	}

	@ColumnAliases(aliases={"reference"})	
	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getBankAccountID() {
		return bankAccountId;
	}

	@ColumnAliases(aliases={"bankAccountId", "bankAccountID", "BankAccountId"})		
	public void setBankAccountID(String bankAccountId) {
		this.bankAccountId = bankAccountId;
	}

	public String getPmtRef() {
		return pmtRef;
	}

	@ColumnAliases(aliases={"pmtRef"})		
	public void setPmtRef(String pmtRef) {
		this.pmtRef = pmtRef;
	}

	public String getVoid() {
		return _void;
	}

	@ColumnAliases(aliases={"void"})			
	public void setVoid(String _void) {
		this._void = _void;
	}
	
	public Payment() {}
	
	public Payment(String clientId, String supplierId, String supplierName, String supplierAddressLine1, String supplierAddressLine2,
					String supplierCity, String supplierState, String supplierZip, String supplierEmail, String docNumber, String docDate,
					String docType, String docAmount, String reference, String bankAccountId, String pmtRef, String _void) {
	this.clientId = clientId;
	this.supplierId = supplierId;
	this.supplierName = supplierName;
	this.supplierAddressLine1 = supplierAddressLine1;
	this.supplierAddressLine2 = supplierAddressLine2;
	this.supplierCity = supplierCity;
	this.supplierState = supplierState;
	this.supplierZip = supplierZip;
	this.supplierEmail = supplierEmail;
	this.docNumber = docNumber;
	this.docDate = docDate;
	this.docType = docType;
	this.docAmount = docAmount;
	this.reference = reference;
	this.bankAccountId = bankAccountId;
	this.pmtRef = pmtRef;
	this._void = _void;
	}
}
