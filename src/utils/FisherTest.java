package utils;

public class FisherTest
{
	
	public static double choose(int n, int k) 
	{
		return Math.exp(Functions.lnfgamma(n + 1) - Functions.lnfgamma(k+1) - Functions.lnfgamma(n-k+1));
	}
	
	public static double getFisherP( int allN, int allK, int n, int k )
	{
		return choose(allK,k) * choose(allN - allK, n - k) / choose(allN,n);
	}
	
	/*
	 * Probably not implemented correctly (doesn't consider both sides?)
	 */
	public static double getFisherPSum(int allN, int allK, int n, int k  )
	{
		double sum =0;
		
		for( int x = k; x <=n && x <= allK ; x++)
			sum += getFisherP( allN, allK, n, x);
		
		return sum;
	}
	
	public static void main(String[] args) throws Exception
	{
		LOG.info(choose(5,2));
		
		LOG.info(choose(300,12));
		
		LOG.info(choose(600,234));
		
		LOG.info( getFisherP(60, 20, 7, 3) );
		
		// see slide # 8 here http://afodor.github.io/classes/stats2016/Lecture07.pptx
		LOG.info( getFisherPSum(34, 16, 15, 13));
	}
}
