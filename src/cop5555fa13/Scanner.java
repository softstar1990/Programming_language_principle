package cop5555fa13;

import static cop5555fa13.TokenStream.Kind.*;

import java.io.IOException;
import java.util.HashMap;



import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.TokenStream.Token;

public class Scanner {
		//ADD METHODS AND FIELDS
	private enum State {
		START, GOT_EQUALS, GOT_NOT, GOT_LT, GOT_GT, GOT_DIV, GOT_COMMENT, IDENT_PART, DIGITS, STRING, EOF
	}
	
	final TokenStream stream;	//
	private State state; //state of the Scanner
	private int index; // points to the next char to process during scanning, or if none, past the end of the array
    private int ch; //the character we look at
	private int begOffset;
	private final int MAXINDEX;
	private HashMap<String, TokenStream.Kind> map;
	
	public Scanner(TokenStream stream) {
		//IMPLEMENT THE CONSTRUCTOR
		this.stream = stream;
		this.index = 0;
		try {
			this.ch = stream.inputChars[0];
		} catch (Exception e) {
			this.ch = -1;
		}
		this.MAXINDEX = stream.inputChars.length;
		this.map = new HashMap<String, TokenStream.Kind>();
	}

	private void getmap(){
		if(map.isEmpty()){
			map.put("image", image);
			map.put("int", _int);
			map.put("boolean", _boolean);
			map.put("pixel", pixel);
			map.put("pixels", pixels);
			map.put("blue", blue);
			map.put("red", red);
			map.put("green", green);
			map.put("Z", Z);
			map.put("shape", shape);
			map.put("width", width);
			map.put("height", height);
			map.put("location", location);
			map.put("x_loc", x_loc);
			map.put("y_loc", y_loc);
			map.put("SCREEN_SIZE", SCREEN_SIZE);
			map.put("visible", visible);
			map.put("x", x);
			map.put("y", y);
			map.put("pause", pause);
			map.put("while", _while);
			map.put("if", _if);
			map.put("else", _else);
		}
	}
	
	public void scan() throws LexicalException {
		//THIS IS PROBABLY COMPLETE
		Token t;
		do {
			t = next();
			if (t.kind.equals(COMMENT)) {
				stream.comments.add((Token) t);
			} else
				stream.tokens.add(t);
		} while (!t.kind.equals(EOF));
	}
	
	private void getch() throws LexicalException {
		//get the next char from the token stream and update index
		if (index >= MAXINDEX) {
			ch = -1;
		} else {
			ch = (int)stream.inputChars[index];							
		}
	}	

	private Token next() throws LexicalException{
        //COMPLETE THIS METHOD.  THIS IS THE FUN PART!
		state = State.START;
		Token t = null;
		do {
			switch (state) { 
                  /*in each state, check the next character.
                             either create a token or change state
                  */
		          case START:
		        	begOffset = index;
					switch (ch) {
						case -1:
					        t = stream.new Token(EOF, begOffset, ++index);
							break; // end of file
						case ' ':case '\t':case '\n':case '\f':case '\r':
							++index;
							break; // white space
						//seperators
						case '.':
							t = stream.new Token(DOT, begOffset, ++index);
							break;
						case ';':
							t = stream.new Token(SEMI, begOffset, ++index);
							break;	
						case ',':
							t = stream.new Token(COMMA, begOffset, ++index);
							break;
						case '(':
							t = stream.new Token(LPAREN, begOffset, ++index);
							break;
						case ')':
							t = stream.new Token(RPAREN, begOffset, ++index);
							break;
						case '[':
							t = stream.new Token(LSQUARE, begOffset, ++index);
							break;
						case ']':
							t = stream.new Token(RSQUARE, begOffset, ++index);
							break;
						case '{':
							t = stream.new Token(LBRACE, begOffset, ++index);
							break;
						case '}':
							t = stream.new Token(RBRACE, begOffset, ++index);
							break;
						case ':':
							t = stream.new Token(COLON, begOffset, ++index);
							break;
						case '?':
							t = stream.new Token(QUESTION, begOffset, ++index);
							break;
						//one char operator
						case '|':
							t = stream.new Token(OR, begOffset, ++index);
							break;		
						case '&':
							t = stream.new Token(AND, begOffset, ++index);
							break;	
						case '+':
							t = stream.new Token(PLUS, begOffset, ++index);
							break;
						case '-':
							t = stream.new Token(MINUS, begOffset, ++index);
							break;
						case '*':
							t = stream.new Token(TIMES, begOffset, ++index);
							break;	
						case '%':
							t = stream.new Token(MOD, begOffset, ++index);
							break;
						//other operator and comment
						case '=':
							state = State.GOT_EQUALS;
							++index;
							break;
						case '!':
							state = State.GOT_NOT;
							++index;
							break;
						case '<':
							state = State.GOT_LT;
							++index;
							break;
						case '>':
							state = State.GOT_GT;
							++index;
							break;
						case '/':
							state = State.GOT_DIV;
							++index;
							break;
						//literal string
						case '"':
							state =State.STRING;
							++index;
							break;
						//digit
						case '0':
							t = stream.new Token(INT_LIT, begOffset, ++index);
							break;
						default:
							if (Character.isDigit(ch) && ch != '0') {
								state = State.DIGITS;
								++index;
							} else if (Character.isJavaIdentifierStart(ch)) {
								state = State.IDENT_PART;
								++index;
							} else {
							    throw stream.new LexicalException(begOffset, "i cannot understand this input character");
							}
				  		}
					  getch();						
					  break; // end of state START	        	  		        	  
		        	
					  
		          case GOT_EQUALS:
		          	switch (ch) {
		          	case '=':
						t = stream.new Token(EQ, begOffset, ++index);
						break;
		          	default:
		          		t = stream.new Token(ASSIGN, begOffset, index);
		          		break;    
		          	}
					getch();
					break; // end of state GOT_EQUALS	
		          	
					
		          case GOT_NOT:
		        	switch (ch) {
					case '=':
						t = stream.new Token(NEQ, begOffset, ++index);
						break;
					default:
						t = stream.new Token(NOT, begOffset, index);
						break;
					}
					getch();
					break; // end of state GOT_NOT	
					
		          case GOT_LT:
		        	switch (ch) {
					case '=':
						t = stream.new Token(LEQ, begOffset, ++index);
						break;
					case '<':
						t = stream.new Token(LSHIFT, begOffset, ++index);
						break;
					default:
						t = stream.new Token(LT, begOffset, index);
						break;
					}
					getch();
					break; // end of state GOT_LT
					
		          case GOT_GT:
		        	switch (ch) {
					case '=':
						t = stream.new Token(GEQ, begOffset, ++index);
						break;
					case '>':
						t = stream.new Token(RSHIFT, begOffset, ++index);
						break;
					default:
						t = stream.new Token(GT, begOffset, index);
						break;
					}
					getch();
					break; // end of state GOT_NOT	
					
		          case GOT_DIV:
		        	switch (ch) {
					case '/':
						state = State.GOT_COMMENT;
						++index;
						break;
					default:
						t = stream.new Token(DIV, begOffset, index);
						break;
					}
					getch();
					break; // end of state GOT_NOT	
					
		          case GOT_COMMENT:
		        	switch (ch) {
					case '\t':case '\n':case '\r':case -1:
						t = stream.new Token(COMMENT, begOffset, ++index);
						break; // white space
					default:
						++index;
						break;
					}
					getch();
					break; // end of state GOT_NOT	
					
		          case STRING:
			          	switch (ch) {
			          	case '"':
							t = stream.new Token(STRING_LIT, begOffset, ++index);
							break;
			          	case -1:
			          		throw stream.new LexicalException(begOffset, "string start here but cannnot find the end") ;
			          	default:
			          		++index;
			          		break;    
			          	}
						getch();
						break; // end of state STRING
						
		          case DIGITS:
		        	if (Character.isDigit(ch)) {
						++index;
						getch();
					} else {
						t = stream.new Token(INT_LIT, begOffset, index);
					}
		        	getch();
		        	break; //end of state of DIGITS
		        	
		          case IDENT_PART:
		        	getmap();
		        	if (Character.isJavaIdentifierPart(ch)) {
						++index;
						getch();
					} else {
						String ts;
						ts = String.valueOf(stream.inputChars, begOffset, index - begOffset);
						if (ts.equals("true") || ts.equals("false")) {
							t = stream.new Token(BOOLEAN_LIT, begOffset, index);
						} else if (map.containsKey(ts)) {
							t = stream.new Token(map.get(ts), begOffset, index);
						} else {
							t = stream.new Token(IDENT, begOffset, index);
						}
					}
		        	getch();
		        	break; //end of state of IDENT
		        	
		          default:
		        	  assert false : "should not reach here";
		     }// end of switch(state)
		 }   while (t == null); // loop terminates when a token is created
		return t;
	}
}

