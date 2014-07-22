package cop5555fa13.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import cop5555fa13.Parser;
import cop5555fa13.Scanner;
import cop5555fa13.SimpleParser.SyntaxException;
import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.ast.Program;
import cop5555fa13.ast.ToStringVisitor;
import cop5555fa13.ast.TypeCheckVisitor;



public class TestVisitor {

	private void parseCorrectInput (String program)  {
		TokenStream stream = new TokenStream(program);
		Scanner s = new Scanner(stream);
		Program prog = null;
		try{
		s.scan();
		Parser p = new Parser(stream);
		prog = p.parse();	
		assertTrue("expected p.getErrorList().isEmpty()", p.getErrorList().isEmpty());

			ToStringVisitor visitor= new ToStringVisitor();
			try {
				prog.visit(visitor, "");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.print(visitor.getString());	
			System.out.println("--------------------------");//add by myself
			
			TypeCheckVisitor checker = new TypeCheckVisitor();
			try {
				prog.visit(checker, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("isCorrect:" + checker.isCorrect()+"\n");	
			System.out.println(checker.getLog());
		}
		
		catch(LexicalException e){
			System.out.println("Lexical error parsing program: ");
			System.out.println(program);
			System.out.println(e.toString());
			System.out.println("---------");
			fail();
		}
	}
	

	@Test
	public void test1() throws LexicalException, SyntaxException  {
		String input = "test{ pixel y1; pixel d; d = {{10, 12, y1}}; }";
		parseCorrectInput(input);
	}
	
}
