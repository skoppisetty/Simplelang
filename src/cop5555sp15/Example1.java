package cop5555sp15;
/** This class illustrates calling execute twice after modifying the int variable
*  and a string variable in the codelet. The expected output is given below.
*  "null" is default string value and the "Second" is set using the codelet class

null
0
2
Second

*/
public class Example1 {
	public static void main(String[] args) throws Exception{
		String source = "class Example1{\n"
		+ "def i1: int;\n"
		+ "def k : string;\n"
		+ "if ((i1 == 0) & (k == \"initial\")){print k;}\n"
		+ "else {print k;};\n"
		+ "}";
		Codelet codelet = CodeletBuilder.newInstance(source);
		codelet.execute();
		int i1 = CodeletBuilder.getInt(codelet, "i1");
		System.out.println(i1);
		CodeletBuilder.setInt(codelet, "i1", i1+2);
		CodeletBuilder.setString(codelet, "k", "Second");
		System.out.println(CodeletBuilder.getInt(codelet, "i1"));
		codelet.execute();
	}
}