package com.kodak.kodak_kioskconnect_n2r;

public class PrintProduct
{
	public static final String TYPE_QUICKBOOK = "QuickBook";
	public static final String TYPE_QUICKBOOKPAGE = "QuickBookPage";
	public static final String TYPE_PRINTS = "print";
	public static final String TYPE_DUPLEX_MY_GREETING = "DuplexMyGreeting";
	public static final String TYPE_GREETING_CARDS = "Greeting Cards";
	public static final String TYPE_COLLAGES = "Collages";
	
	
	private String name;
	private String shortName;
	private String type;
	private int width;
	private int height;
	private int quantityIncrement;
	private int minImageSizeLongDim;
	private String maxPrice;
	private String maxPriceStr;
	private String minPrice;
	private String minPriceStr;
	private String price;
	private String id;
	private String lgGlyphURL;
	private String smGlyphURL;
	private String htmlMarketing;
	private String htmlShortMarketing;
	public PrintProduct()
	{
	}

	public PrintProduct(String id, String name, String maxPrice, String minPrice,String width,String height)
	{
		this.id = id;
		this.name = name;
		this.maxPrice = maxPrice;
		this.minPrice = minPrice;
	}

	public String getMaxPrice()
	{
		return maxPrice;
	}

	public void setMaxPrice(String maxPrice)
	{
		this.maxPrice = maxPrice;
	}

	public String getMinPrice()
	{
		return minPrice;
	}

	public void setMinPrice(String minPrice)
	{
		this.minPrice = minPrice;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getShortName()
	{
		return shortName;
	}

	public void setShortName(String shortName)
	{
		this.shortName = shortName;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}
	
	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public String getPrice()
	{
		return price;
	}

	public void setPrice(String price)
	{
		this.price = price;
	}

	public String getLgGlyphURL() {
		return lgGlyphURL;
	}

	public void setLgGlyphURL(String lgGlyphURL) {
		this.lgGlyphURL = lgGlyphURL;
	}

	public String getSmGlyphURL() {
		return smGlyphURL;
	}

	public void setSmGlyphURL(String smGlyphURL) {
		this.smGlyphURL = smGlyphURL;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHtmlMarketing() {
		return htmlMarketing;
	}

	public void setHtmlMarketing(String htmlMarketing) {
		this.htmlMarketing = htmlMarketing;
	}

	public String getHtmlShortMarketing() {
		return htmlShortMarketing;
	}

	public void setHtmlShortMarketing(String htmlShortMarketing) {
		this.htmlShortMarketing = htmlShortMarketing;
	}

	public String getMaxPriceStr() {
		return maxPriceStr;
	}

	public void setMaxPriceStr(String maxPriceStr) {
		this.maxPriceStr = maxPriceStr;
	}

	public String getMinPriceStr() {
		return minPriceStr;
	}

	public void setMinPriceStr(String minPriceStr) {
		this.minPriceStr = minPriceStr;
	}

	public int getQuantityIncrement() {
		return quantityIncrement;
	}

	public void setQuantityIncrement(int quantityIncrement) {
		this.quantityIncrement = quantityIncrement;
	}

	public int getMinImageSizeLongDim() {
		return minImageSizeLongDim;
	}

	public void setMinImageSizeLongDim(int minImageSizeLongDim) {
		this.minImageSizeLongDim = minImageSizeLongDim;
	}	
	
}
