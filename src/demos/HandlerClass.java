package demos;

public class HandlerClass {

	//Arbitrary function that the RPC Handler calls
	public Integer sum(int a, int b, int c){
		System.out.println("Running Handler!");
		return new Integer(a+b+c);
	}

}
