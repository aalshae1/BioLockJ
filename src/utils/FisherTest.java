package utils;

public class FisherTest
{
	public static double choose(int n, int k) throws Exception
	{
		return Math.exp(Functions.lnfgamma(n + 1) - Functions.lnfgamma(k+1) - Functions.lnfgamma(n-k+1));
	}
	
	public static void main(String[] args) throws Exception
	{
		System.out.println(choose(5,2));
		
		System.out.println(choose(300,12));
		
		
	}
}
