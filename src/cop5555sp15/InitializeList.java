package cop5555sp15;
import java.util.List;
public class InitializeList {
	public static void main(String[] args) throws Exception{
		String source = "class ListInit{\n"
		+ "def l1: @[int];\n"
		+ "def l2: @[string];\n"
		+ "def i1: int;\n"
		+ "l1 = @[300,400,500];\n"
		+ "l2 = @[\"go\", \"gators\"];\n"
		+ "i1 = 42;\n"
		+ "}";
		Codelet codelet = CodeletBuilder.newInstance(source);
		codelet.execute();
		@SuppressWarnings("rawtypes")
		List l1 = CodeletBuilder.getList(codelet, "l1");
		System.out.println(l1.size());
		System.out.println(l1.get(0));
		int i1 = CodeletBuilder.getInt(codelet, "i1");
		System.out.println(i1);
	}
}