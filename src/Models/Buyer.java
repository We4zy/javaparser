package Models;

/**
 * @author rory richter
 *
 */
public class Buyer {

	private Integer _buyerId;
	private String _apiLogUrl;
	private String _filePath;
	private String _fileMask;
	
	//access modifiers (properties)
	public Integer getBuyerId()
	{ return _buyerId; }
	public void setBuyerId(Integer buyerId)
	{ _buyerId = buyerId; }
	
	public String getApiLogUrl()
	{ return _apiLogUrl; }
	public void setApiLogUrl(String apiLogUrl)
	{ _apiLogUrl = apiLogUrl; }
	
	public String getFilePath()
	{ return _filePath; }
	public void setFilePath(String filePath)
	{ _filePath = filePath; }
	
	public String getFileMask()
	{ return _fileMask; }
	public void setFileMask(String fileMask)
	{ _fileMask = fileMask; }
	
	public Buyer(Integer buyerId, String ApiLogUrl, String filePath, String fileMask) {
		_buyerId = buyerId;
		_apiLogUrl = ApiLogUrl;
		_filePath = filePath;
		_fileMask = fileMask;
	}

}
