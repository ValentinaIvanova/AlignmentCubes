package cubix.helper;

public class Map {
	
	
	public static double map(
			double domainValue, 
			double domainMin, 
			double domainMax, 
			double targetMin,
			double targetMax)
	{
		try{
			return targetMin + (((domainValue-domainMin) / (domainMax-domainMin)) * (targetMax-targetMin));
		} catch(Exception e){
			return 0;
		}
	}
	
	
	public static float mapLog(
			double domainValue, 
			double domainMin, 
			double domainMax, 
			double targetMin,
			double targetMax)

	{
		try{
			float x = (float) (((domainValue - domainMin) / (domainMax - domainMin)) * (domainMax -1) +1);
			float y = (float) (Math.log(x) / Math.log(domainMax));
			return (float) (y * (targetMax - targetMin) - targetMin);
		} catch(Exception e){
			return 0;
		}
	}


	public static double mapSqrt(
			double domainValue, 
			double domainMin, 
			double domainMax, 
			double targetMin,
			double targetMax)
	{
		try{
			return targetMin + (((domainValue-domainMin) / (domainMax-domainMin)) * (targetMax-targetMin));
		} catch(Exception e){
			return 0;
		}
	}

}
