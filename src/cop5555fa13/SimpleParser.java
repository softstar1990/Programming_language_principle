package cop5555fa13;

import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.TokenStream.Token;
import cop5555fa13.TokenStream.Kind;

public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String msg) {
			super(msg);
			this.t = t;
		}

		public String toString() {
			return super.toString() + "\n" + t.toString();
		}

		public Kind getKind(){
			return t.kind;
		}
	}

	TokenStream stream;
	int i = 0;
	Token t;
	/* You will need additional fields */

	/** creates a simple parser.  
	 * 
	 * @param initialized_stream  a TokenStream that has already been initialized by the Scanner 
	 */
	public SimpleParser(TokenStream initialized_stream) {
		this.stream = initialized_stream;
		Scanner s = new Scanner(stream);
		try {
			s.scan();
		} catch (LexicalException e) {
			System.out.println(e);
		}
		consume();
		/* You probably want to do more here */
	}

	/* This method parses the input from the given token stream.  If the input is correct according to the phrase
	 * structure of the language, it returns normally.  Otherwise it throws a SyntaxException containing
	 * the Token where the error was detected and an appropriate error message.  The contents of your
	 * error message will not be graded, but the "kind" of the token will be.
	 */
	public void parse() throws SyntaxException {
		/* You definitely need to do more here */
		program();
	}

	/* You will need to add more methods*/
	private void consume() {
		t = stream.getToken(i++);
	}

	/* Java hint -- Methods with a variable number of parameters may be useful.  
	 * For example, this method takes a token and variable number of "kinds", and indicates whether the
	 * kind of the given token is among them.  The Java compiler creates an array holding the given parameters.
	 */
	private boolean isKind(Token t, Kind... kinds) {
		Kind k = t.kind;
		for (int i = 0; i != kinds.length; ++i) {
			if (k==kinds[i]) return true;
		}
		return false;
	}

	private void match(Kind kind) throws SyntaxException  {
		if (isKind(t, kind)) {
			consume();
		} else {
			int line = t.getLineNumber();
			error("Line "+ line + ": i need to find a " + kind);
			//error("expected " + kind);
		}
	}

	private void error(String msg) throws SyntaxException {
		throw new SyntaxException(t, msg);
	}

	void program() throws SyntaxException {
		match(Kind.IDENT);
		match(Kind.LBRACE);
		while (isKind(t,Kind.image,Kind.pixel,Kind._int,Kind._boolean)) {
			dec();
		}
		while(isKind(t,Kind.SEMI,Kind.IDENT,Kind.pause,Kind._while,Kind._if)) {
			stmt();
		}
		match(Kind.RBRACE); 
		match(Kind.EOF);
	}

	void dec() throws SyntaxException{
		type();
		match(Kind.IDENT);
		match(Kind.SEMI);
	}

	void type() throws SyntaxException{
		if(isKind(t,Kind.image,Kind.pixel,Kind._int,Kind._boolean)){
			consume();
		} else {
			throw new SyntaxException(t, t.getLineNumber() + ", i cannot find a type! ");
			//error("i cannot find a type!");
		}
	}

	void pixel() throws SyntaxException{
		match(Kind.LBRACE);
		match(Kind.LBRACE);
		expr();
		match(Kind.COMMA);
		expr();
		match(Kind.COMMA);
		expr();
		match(Kind.RBRACE);
		match(Kind.RBRACE);
	}

	void stmt() throws SyntaxException{
		if(isKind(t,Kind.SEMI)){
			consume();
		}else if (isKind(t, Kind.IDENT)) {
			assignstmt();
		}else if (isKind(t, Kind.pause)) {
			pausestmt();
		}else if (isKind(t, Kind._while)) {
			iterationstmt();
		}else if (isKind(t, Kind._if)) {
			alternativestmt();
		}else {
			throw new SyntaxException(t, t.getLineNumber() + ", i cannot construct the statement! ");
			//error("i cannot construct the statement!");
		}
	}

	void pausestmt() throws SyntaxException{
		match(Kind.pause);
		expr();
		match(Kind.SEMI);
	}

	void iterationstmt() throws SyntaxException{
		match(Kind._while);
		match(Kind.LPAREN);
		expr();
		match(Kind.RPAREN);
		match(Kind.LBRACE);
		while (isKind(t, Kind.SEMI,Kind.IDENT,Kind.pause,Kind._while,Kind._if)) {
			stmt();
		}
		match(Kind.RBRACE);
	}

	void alternativestmt() throws SyntaxException{
		match(Kind._if);
		match(Kind.LPAREN);
		expr();
		match(Kind.RPAREN);
		match(Kind.LBRACE);
		while (isKind(t, Kind.SEMI,Kind.IDENT,Kind.pause,Kind._while,Kind._if)) {
			stmt();
		}
		match(Kind.RBRACE);
		if (isKind(t, Kind._else)) {
			consume();
			match(Kind.LBRACE);
			while (isKind(t, Kind.SEMI,Kind.IDENT,Kind.pause,Kind._while,Kind._if)) {
				stmt();
			}
			match(Kind.RBRACE);
		}
	}

	void assignstmt() throws SyntaxException{
		match(Kind.IDENT);
		if(isKind(t, Kind.ASSIGN)){
			consume();
			if (isKind(t, Kind.IDENT, Kind.INT_LIT,Kind.BOOLEAN_LIT, 
					Kind.x, Kind.y, Kind.Z, Kind.SCREEN_SIZE,Kind.LPAREN)){
				expr();
			}
			else if(isKind(t, Kind.LBRACE))
				pixel();
			else if(isKind(t, Kind.STRING_LIT))
				consume();
			else
				error("Unexpected token");
		}
		else if(isKind(t, Kind.DOT)){
			consume();
			switch(t.kind){
			case pixels:
				consume();
				match(Kind.LSQUARE);
				expr();
				match(Kind.COMMA);
				expr();
				match(Kind.RSQUARE);
				if(isKind(t, Kind.ASSIGN)){
					consume();
					pixel();
				}
				else if(isKind(t, Kind.red, Kind.blue, Kind.green)){
					consume();
					match(Kind.ASSIGN);
					expr();
				}
				else error("unexpected token here");
				break;
			case shape:	case location:
				consume();
				match(Kind.ASSIGN);
				match(Kind.LSQUARE);
				expr();
				match(Kind.COMMA);
				expr();
				match(Kind.RSQUARE);
				break;

			case visible:
				consume();
				match(Kind.ASSIGN);
				expr();
				break;
			default:
				error("Unexpected token");
				break;
			}
		}
		else if(!isKind(t, Kind.ASSIGN, Kind.DOT)){
			error("Expected assign or dot");
		}
		match(Kind.SEMI);
	}

	void expr() throws SyntaxException{
		orexpr();
		if (isKind(t, Kind.QUESTION)) {
			consume();
			expr();
			match(Kind.COLON);
			expr();
		}
	}

	void orexpr() throws SyntaxException{
		andexpr();
		while (isKind(t, Kind.OR)) {
			consume();
			andexpr();
		}
	}

	void andexpr() throws SyntaxException{
		equalityexpr();
		while(isKind(t, Kind.AND)){
			consume();
			equalityexpr();
		}
	}

	void equalityexpr() throws SyntaxException{
		relexpr();
		while(isKind(t, Kind.EQ,Kind.NEQ)){
			consume();
			relexpr();
		}
	}

	void relexpr() throws SyntaxException{
		shiftexpr();
		while(isKind(t, Kind.LT,Kind.GT,Kind.LEQ,Kind.GEQ)){
			consume();
			shiftexpr();
		}
	}

	void shiftexpr() throws SyntaxException{
		addexpr();
		while (isKind(t, Kind.LSHIFT,Kind.RSHIFT)) {
			consume();
			addexpr();
		}
	}

	void addexpr() throws SyntaxException{
		multexpr();
		while(isKind(t, Kind.PLUS,Kind.MINUS)){
			consume();
			multexpr();
		}
	}

	void multexpr() throws SyntaxException{
		primaryexpr();
		while(isKind(t, Kind.TIMES,Kind.DIV,Kind.MOD)){
			consume();
			primaryexpr();
		}
	}

	void primaryexpr() throws SyntaxException{
		switch (t.kind) {
		case INT_LIT:case BOOLEAN_LIT:case x:case y:case Z:case SCREEN_SIZE:
			consume();
			break;
		case IDENT:
			consume();
			if(isKind(t, Kind.LSQUARE)){
				consume();
				expr();
				match(Kind.COMMA);
				expr();
				match(Kind.RSQUARE);
				if(isKind(t, Kind.red,Kind.blue,Kind.green)){
					consume();
				}
				else error("expected color.");
			}
			else if(isKind(t, Kind.DOT)){
				consume();
				if(isKind(t, Kind.width, Kind.x_loc,Kind.y_loc, Kind.height)){
					consume();
				}
				else error("Missing parameter");
			}
			break;
		case LPAREN:
			consume();
			expr();
			match(Kind.RPAREN);
			break;
		default:
			error("Expected primary expression.");
			break;
		}
	}	
}
