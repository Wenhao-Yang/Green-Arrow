package www.william.com;
import java.util.Scanner;

public class AppMain {

	public static String testTriangle(int a, int b, int c){
		if (a<=0 || b<=0 || c<=0){
			return "Some sides are less than 0.";
		}
		else if(a+b<=c || a+c<=b || b+c<=a){
			return "It is not a triangle.";
		}
		else{
			if (a!=b && a!=c && b!=c){
				return "Scalene Triangle.";
			}
			else if (a==b || a==c || b==c){
				if (a==b && a==c){
					return "Equilateral Triangle.";
				}
				else
					return "Isosceles Triangle.";
			}
		}
		return "ERROR";
	}
}
