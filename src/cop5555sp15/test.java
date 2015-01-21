package cop5555sp15;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import cop5555sp15.TokenStream;
import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;
import static cop5555sp15.TokenStream.Kind.*;

public class test {
	  public static void main(String[] args) {
			System.out.println("Test: noWhitespace");
			String input = "@#  *";
			System.out.println(input);
			TokenStream stream = new TokenStream(input);
			Scanner scanner = new Scanner(stream);
			scanner.scan();
			System.out.println(stream);
			assertEquals(4, stream.tokens.size()); // one each for @,#, and *, plus
													// the eof token
			assertEquals(AT, stream.nextToken().kind);
			assertEquals(ILLEGAL_CHAR, stream.nextToken().kind);
			assertEquals(TIMES, stream.nextToken().kind);
			assertEquals(EOF, stream.nextToken().kind);
		    
		  }
}
