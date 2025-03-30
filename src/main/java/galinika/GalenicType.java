package galinika;

public class GalenicType {
	private String galenicTypeName;
	private float minimumQuantity;
	private float divisionQuantity;
	private float minimumCost;
	private float costIncrement;
	
	public GalenicType (String galenicTypeName, float minimumQuantity, float divisionQuantity, 
			float minimumCost, float costIncrement) {
		this.galenicTypeName = galenicTypeName;
		this.minimumQuantity = minimumQuantity;
		this.divisionQuantity = divisionQuantity;
		this.minimumCost = minimumCost;
		this.costIncrement = costIncrement;
	} 
	
	public float[] getgalenicTypeValues () {
		return new float[] {minimumQuantity,divisionQuantity,minimumCost,costIncrement};
	}
	
	public String getGalenicTypeName() {
	    return galenicTypeName;
	}

	public float getMinimumQuantity() {
	    return minimumQuantity;
	}

	public float getDivisionQuantity() {
	    return divisionQuantity;
	}

	public float getMinimumCost() {
	    return minimumCost;
	}

	public float getCostIncrement() {
	    return costIncrement;
	}

	
	
	@Override
	public String toString() {
	    return galenicTypeName;
	}

}
