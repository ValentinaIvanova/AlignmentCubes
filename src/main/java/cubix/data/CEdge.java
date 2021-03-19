package cubix.data;

import java.util.HashMap;

public class CEdge {
	
	private String id;
	float weight = 0;
	float weight2 = 1;
	boolean weight2Valid = false;
	
	float accumulatedWeight = 0;
	
	public CEdge(String id){
		this.id = id;
	}
	
	public String getID(){ return this.id; }
	public void setWeight(float w){ weight = w; }
	public float getWeight(){ return weight; }
	public void setWeight2(float w){ weight2 = w; weight2Valid = true;}
	public float getWeight2(){ return weight2; }
	public boolean isWeight2Valid(){return this.weight2Valid;}

	public float getAccumulatedWeight() {
		return accumulatedWeight;
	}

	public void setAccumulatedWeight(float accumulatedWeight) {
		this.accumulatedWeight = accumulatedWeight;
	}
	
}
