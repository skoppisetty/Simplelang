package cop5555sp15;
import java.io.File;
import java.util.List;
/** This class illustrates calling execute twice after modifying 
 * the guard l3 (boolean) and fetches the list l1 and modifies by setting l1[2] = 123
 * expected output is given below.
else loop
3
[1, 2, 3]
[1, 2, 123]
if loop
3
*/
public class Example2 {
	public static void main(String[] args) throws Exception{
//		File file = new File("/home/suresh/workspace/compiler/sample.txt");
		String source = "class Example1{\n"
		+ "def l1: @[int];\n"
		+ "def l3 : boolean; \n"
		+ "l1 = @[1,2,3];"
		+ "if (l3){ print \"if loop\"; print size(l1);}\n"
		+ "else { print \"else loop\"; print size(l1);};\n"
		+ "}";
		Codelet codelet = CodeletBuilder.newInstance(source);
		codelet.execute();
		Boolean i1 = CodeletBuilder.getBoolean(codelet, "l3");
		CodeletBuilder.setBoolean(codelet, "l3", true);
		List l1 = CodeletBuilder.getList(codelet, "l1");
		System.out.println(l1);
		l1.set(2, 123);
		System.out.println(l1);
		codelet.execute();
	}
}
