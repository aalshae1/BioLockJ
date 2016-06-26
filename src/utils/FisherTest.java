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
	
	public static double getFisherPSum(int allN, int allK, int n, int k  )
	{
		double sum =0;
		
		for( int x = k; x <=n ; x++)
			sum += getFisherP( allN, allK, n, x);
		
		return sum;
	}
	
	public static void main(String[] args) throws Exception
	{
		System.out.println(choose(5,2));
		
		System.out.println(choose(300,12));
		
		System.out.println(choose(600,234));
		
		System.out.println( getFisherP(60, 20, 7, 3) );
		
		System.out.println( getFisherPSum(34, 16, 15, 13));
	}
}
