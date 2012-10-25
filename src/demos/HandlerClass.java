package demos;

public class HandlerClass {

	public Integer sum(int n){
		int result = 0;
		int temp = 0;
		while(n>0){
			temp= n%10;
			n=(n-temp)/10;
			result += temp;
		}
		return new Integer(result);
	}

}
