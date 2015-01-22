package cop5555sp15;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import cop5555sp15.TokenStream;
import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;
import cop5555sp15.TestScanner.*;
import static cop5555sp15.TokenStream.Kind.*;

public class test {
	  public static void main(String[] args) {
			System.out.println("onlyComment");
			String input = "/**/";
			System.out.println(input);
			TokenStream stream = new TokenStream(input);
			Scanner scanner = new Scanner(stream);
			scanner.scan();
			System.out.println(stream);
			Kind[] expectedKinds = { EOF };
			String[] expectedTexts = { "" }; // need empty string for eof
			//assertArrayEquals(expectedKinds, makeKindArray(stream));
			//assertArrayEquals(expectedTexts, makeTokenTextArray(stream));
		    
			
		  }
}
