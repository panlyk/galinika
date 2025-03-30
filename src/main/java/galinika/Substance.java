package galinika;

public class Substance {
	private String substanceName;
	private float pricePerQuantity;
	
	public Substance (String name, float pricePerQ) {
		this.substanceName = name;
		this.pricePerQuantity = pricePerQ;
	}
	
	public String getName() {
		return substanceName;
	}
	
	public float getPricePerQuantity() {
		return pricePerQuantity;
	}
	
	@Override
	public String toString() {
	    return substanceName;
	}

}
