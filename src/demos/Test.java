package demos;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Test {
public static void main(String a[]){
	System.out.println();
	Path name = Paths.get("asdasd");
	Path child = Paths.get("asdds").resolve(name);
	System.out.println(child);
}
}
