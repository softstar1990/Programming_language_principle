package cop5555fa13;

import static cop5555fa13.TokenStream.Kind.AND;
import static cop5555fa13.TokenStream.Kind.ASSIGN;
import static cop5555fa13.TokenStream.Kind.BOOLEAN_LIT;
import static cop5555fa13.TokenStream.Kind.COLON;
import static cop5555fa13.TokenStream.Kind.COMMA;
import static cop5555fa13.TokenStream.Kind.DIV;
import static cop5555fa13.TokenStream.Kind.DOT;
import static cop5555fa13.TokenStream.Kind.EOF;
import static cop5555fa13.TokenStream.Kind.EQ;
import static cop5555fa13.TokenStream.Kind.GEQ;
import static cop5555fa13.TokenStream.Kind.GT;
import static cop5555fa13.TokenStream.Kind.IDENT;
import static cop5555fa13.TokenStream.Kind.INT_LIT;
import static cop5555fa13.TokenStream.Kind.LBRACE;
import static cop5555fa13.TokenStream.Kind.LEQ;
import static cop5555fa13.TokenStream.Kind.LPAREN;
import static cop5555fa13.TokenStream.Kind.LSHIFT;
import static cop5555fa13.TokenStream.Kind.LSQUARE;
import static cop5555fa13.TokenStream.Kind.LT;
import static cop5555fa13.TokenStream.Kind.MINUS;
import static cop5555fa13.TokenStream.Kind.MOD;
import static cop5555fa13.TokenStream.Kind.NEQ;
import static cop5555fa13.TokenStream.Kind.OR;
import static cop5555fa13.TokenStream.Kind.PLUS;
import static cop5555fa13.TokenStream.Kind.QUESTION;
import static cop5555fa13.TokenStream.Kind.RBRACE;
import static cop5555fa13.TokenStream.Kind.RPAREN;
import static cop5555fa13.TokenStream.Kind.RSHIFT;
import static cop5555fa13.TokenStream.Kind.RSQUARE;
import static cop5555fa13.TokenStream.Kind.SCREEN_SIZE;
import static cop5555fa13.TokenStream.Kind.SEMI;
import static cop5555fa13.TokenStream.Kind.STRING_LIT;
import static cop5555fa13.TokenStream.Kind.TIMES;
import static cop5555fa13.TokenStream.Kind.Z;
import static cop5555fa13.TokenStream.Kind._boolean;
import static cop5555fa13.TokenStream.Kind._else;
import static cop5555fa13.TokenStream.Kind._if;
import static cop5555fa13.TokenStream.Kind._int;
import static cop5555fa13.TokenStream.Kind._while;
import static cop5555fa13.TokenStream.Kind.blue;
import static cop5555fa13.TokenStream.Kind.green;
import static cop5555fa13.TokenStream.Kind.height;
import static cop5555fa13.TokenStream.Kind.image;
import static cop5555fa13.TokenStream.Kind.pause;
import static cop5555fa13.TokenStream.Kind.pixel;
import static cop5555fa13.TokenStream.Kind.red;
import static cop5555fa13.TokenStream.Kind.width;
import static cop5555fa13.TokenStream.Kind.x;
import static cop5555fa13.TokenStream.Kind.x_loc;
import static cop5555fa13.TokenStream.Kind.y;
import static cop5555fa13.TokenStream.Kind.y_loc;

import java.util.ArrayList;
import java.util.List;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.TokenStream.Token;
import cop5555fa13.ast.AlternativeStmt;
import cop5555fa13.ast.AssignExprStmt;
import cop5555fa13.ast.AssignPixelStmt;
import cop5555fa13.ast.AssignStmt;
import cop5555fa13.ast.BinaryExpr;
import cop5555fa13.ast.BooleanLitExpr;
import cop5555fa13.ast.ConditionalExpr;
import cop5555fa13.ast.Dec;
import cop5555fa13.ast.Expr;
import cop5555fa13.ast.FileAssignStmt;
import cop5555fa13.ast.IdentExpr;
import cop5555fa13.ast.ImageAttributeExpr;
import cop5555fa13.ast.IntLitExpr;
import cop5555fa13.ast.IterationStmt;
import cop5555fa13.ast.PauseStmt;
import cop5555fa13.ast.Pixel;
import cop5555fa13.ast.PreDefExpr;
import cop5555fa13.ast.Program;
import cop5555fa13.ast.SampleExpr;
import cop5555fa13.ast.ScreenLocationAssignmentStmt;
import cop5555fa13.ast.SetVisibleAssignmentStmt;
import cop5555fa13.ast.ShapeAssignmentStmt;
import cop5555fa13.ast.SinglePixelAssignmentStmt;
import cop5555fa13.ast.SingleSampleAssignmentStmt;
import cop5555fa13.ast.Stmt;

public class Parser {


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
	Token progName;  //keep the program name in case you don't generate an AST
	public List<SyntaxException> errorList;  //save the error for grading purposes

	/** creates a simple parser.  
	 * 
	 * @param initialized_stream  a TokenStream that has already been initialized by the Scanner 
	 */
	public Parser(TokenStream initialized_stream) {
		this.stream = initialized_stream;
		errorList = new ArrayList<SyntaxException>();
		Scanner s = new Scanner(stream);
		try {
			s.scan();
		} catch (LexicalException e) {
			System.out.println(e);
		}
		consume();
		/* You probably want to do more here */
	}

	//THIS IS THE MAIN PUBLIC parse method.  Note that it does not throw exceptions.  
	//If any make it to this level without having been caught, the exception is added to the list.
	//If the program parsed correctly, return its AST. Otherwise return null.
	public Program parse() {
		Program p = null;
		try{
			p = parseProgram();
			match(EOF);
		}
		catch(SyntaxException e){
			errorList.add(e);
		}
		if (errorList.isEmpty()){
			return p;
		}
		else 
			return null;
	}

	public List<SyntaxException> getErrorList(){
		return errorList;
	}

	public String getProgName(){
		return (progName != null ?  progName.getText() : "no program name");
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

	private void error(String msg) throws SyntaxException {
		throw new SyntaxException(t, msg);
	}

	private Token match(Kind kind) throws SyntaxException  {
		if (isKind(t, kind)) {
			Token name = t;
			consume();
			return name;
		} else {
			error("i need to find a " + kind);
			return null;
		}
	}


	/**
	 * Program ::= ident { Dec* Stmt* }
	 * 
	 * @throws SyntaxException
	 */
	private Program parseProgram() throws SyntaxException {
		try {
			progName = match(IDENT);
		} catch (SyntaxException e) {
			errorList.add(e);
			while (!isKind(t,IDENT,EOF,LBRACE)){ consume(); }
			if(isKind(t,IDENT)) {consume();}
		}

		try {
			match(LBRACE);
		} catch (SyntaxException e) {
			errorList.add(e);
			while (!isKind(t,LBRACE,SEMI,image,_int,_boolean,pixel,EOF)){ consume(); }
			if(isKind(t,LBRACE)) {consume();}
		}

		List<Dec> decList = new ArrayList<Dec>();
		while (isKind(t,image,pixel,_int,_boolean)) {
			try{
				decList.add(parseDec());
			}
			catch(SyntaxException e){
				errorList.add(e);
				//skip tokens until next semi, consume it, then continue parsing
				while (!isKind(t,SEMI,image,_int,_boolean,pixel, EOF)){ consume(); }
				if (isKind(t,SEMI)){consume();}  //IF A SEMI, CONSUME IT BEFORE CONTINUING
			}
		}

		List<Stmt> stmtList = new ArrayList<Stmt>();
		while (isKind(t,SEMI,IDENT,pause,_while,_if)) {
			try{
				Stmt temp = parseStmt();
				if(temp != null){
					stmtList.add(temp);
				}
			}
			catch(SyntaxException e){
				errorList.add(e);
				while (!isKind(t,SEMI,IDENT,pause,_while,_if,EOF)){ consume(); }
				if (isKind(t,SEMI)){consume();} 
			}
		}

		try {
			match(RBRACE);
		} catch (SyntaxException e) {
			errorList.add(e);
			while (!isKind(t,RBRACE,EOF)){ consume(); }
			if(isKind(t,RBRACE)){consume();}
		} 

		//		return new Program(progName, decList, stmtList);		
		if (errorList.isEmpty()) return new Program(progName, decList, stmtList);
		System.out.println("error" + (errorList.size()>1?"s parsing program ":" parsing program ") + getProgName());
		for(SyntaxException e: errorList){		
			System.out.println(e.getMessage() + " at line" + e.t.getLineNumber());
		}
		return null; 
	}

	private Dec parseDec() throws SyntaxException{
		Kind type = parseType();
		Token ident = match(IDENT);
		match(SEMI);
		return new Dec(type, ident);
	}

	private Kind parseType() throws SyntaxException{
		if(isKind(t,image,pixel,_int,_boolean)){
			Kind temp = t.kind;
			consume();
			return temp;
		} else {
			error("i cannot find a type in the declaretion!");
			return null;
		}
	}		

	private Stmt parseStmt() throws SyntaxException{
		Stmt stmt = null;		
		if(isKind(t,SEMI)){
			consume();
			return null;
		}
		else if (isKind(t, IDENT)) {
			try {
				stmt = parseAssignStmt();
			} catch (SyntaxException e) {
				errorList.add(e);
				while (!isKind(t,SEMI,IDENT,pause,_while,_if,EOF)){ consume(); }
				if (isKind(t,SEMI)){consume();} 
			}
		}else if (isKind(t, pause)) {
			try {
				stmt = parsePauseStmt();
			} catch (SyntaxException e) {
				errorList.add(e);
				while (!isKind(t,SEMI,IDENT,pause,_while,_if,EOF)){ consume(); }
				if (isKind(t,SEMI)){consume();}
			}
		}else if (isKind(t, _while)) {
			try {
				stmt = parseIterationStmt();
			} catch (SyntaxException e) {
				errorList.add(e);
				while (!isKind(t,SEMI,IDENT,pause,_while,_if,EOF)){ consume(); }
				if (isKind(t,SEMI)){consume();}
			}
		}else if (isKind(t, _if)) {
			try {
				stmt = parseAlternativeStmt();
			} catch (SyntaxException e) {
				errorList.add(e);
				while (!isKind(t,SEMI,IDENT,pause,_while,_if,EOF)){ consume(); }
				if (isKind(t,SEMI)){consume();}
			}
		}else {
			error("i cannot construct the statement!");
		}
		return stmt;
	}

	private PauseStmt parsePauseStmt() throws SyntaxException{
		match(pause);
		Expr expr = parseExpr();
		match(SEMI);

		if (errorList.isEmpty()) return new PauseStmt(expr);
		return null;
	}

	private IterationStmt parseIterationStmt() throws SyntaxException{
		match(_while);
		match(LPAREN);
		Expr expr = parseExpr();
		match(RPAREN);
		match(LBRACE);
		List<Stmt> stmtList = new ArrayList<Stmt>();
		while (isKind(t, SEMI,IDENT,pause,_while,_if)) {
			try {
				Stmt temp = parseStmt();
				if(temp != null){
					stmtList.add(temp);
				}
			} catch (SyntaxException e) {
				errorList.add(e);
				while (!isKind(t,SEMI,IDENT,pause,_while,_if,EOF)){ consume(); }
				if (isKind(t,SEMI)){consume();}
			}
		}
		match(RBRACE);

		return new IterationStmt(expr, stmtList);
	}

	private AlternativeStmt parseAlternativeStmt() throws SyntaxException{
		match(_if);
		match(LPAREN);
		Expr expr = parseExpr();
		match(RPAREN);
		match(LBRACE);
		List<Stmt> ifStmtList = new ArrayList<Stmt>();
		while (isKind(t, SEMI,IDENT,pause,_while,_if)) {
			try {
				Stmt temp = parseStmt();
				if(temp != null){
					ifStmtList.add(temp);
				}
			} catch (SyntaxException e) {
				errorList.add(e);
				while (!isKind(t,SEMI,IDENT,pause,_while,_if,EOF)){ consume(); }
				if (isKind(t,SEMI)){consume();}
			}
		}
		match(RBRACE);
		List<Stmt> elseStmtList = new ArrayList<Stmt>();
		if (isKind(t, _else)) {
			consume();
			match(LBRACE);
			while (isKind(t, SEMI,IDENT,pause,_while,_if)) {
				try {
					Stmt temp = parseStmt();
					if(temp != null){
						elseStmtList.add(temp);
					}
				} catch (SyntaxException e) {
					errorList.add(e);
					while (!isKind(t,SEMI,IDENT,pause,_while,_if,EOF)){ consume(); }
					if (isKind(t,SEMI)){consume();}
				}
			}
			match(RBRACE);
		}

		return new AlternativeStmt(expr, ifStmtList, elseStmtList);
	}

	private AssignStmt parseAssignStmt() throws SyntaxException{
		AssignStmt assignstmt = null;
		Token lhsIdent = match(IDENT);
		if(isKind(t, ASSIGN)){
			consume();
			if (isKind(t, IDENT, INT_LIT,BOOLEAN_LIT, x, y, Z, SCREEN_SIZE,LPAREN)){
				Expr expr = parseExpr();
				assignstmt = new AssignExprStmt(lhsIdent, expr);
			}
			else if(isKind(t, LBRACE)){
				Pixel pixel = parsePixel();
				assignstmt = new AssignPixelStmt(lhsIdent, pixel);
			}
			else if(isKind(t, STRING_LIT)){
				Token fileName = t;
				consume();
				assignstmt =  new FileAssignStmt(lhsIdent, fileName);
			}
			else{
				error("Unexpected token");
			}
		}
		else if(isKind(t, DOT)){
			consume();
			switch(t.kind){
			case pixels:
				consume();
				match(LSQUARE);
				Expr xExpr = parseExpr();
				match(COMMA);
				Expr yExpr = parseExpr();
				match(RSQUARE);
				if(isKind(t, ASSIGN)){
					consume();
					Pixel pixel = parsePixel();
					assignstmt =  new SinglePixelAssignmentStmt(lhsIdent, xExpr, yExpr, pixel);
				}
				else if(isKind(t, red, blue, green)){
					Token color = t;
					consume();
					match(ASSIGN);
					Expr rhsExpr = parseExpr();
					assignstmt =  new SingleSampleAssignmentStmt(lhsIdent, xExpr, yExpr, color, rhsExpr);
				}
				else{ 
					error("unexpected token here");
				}
				break;
			case shape:	
				consume();
				match(ASSIGN);
				match(LSQUARE);
				Expr width = parseExpr();
				match(COMMA);
				Expr height = parseExpr();
				match(RSQUARE);
				assignstmt = new ShapeAssignmentStmt(lhsIdent, width, height);
				break;			
			case location:
				consume();
				match(ASSIGN);
				match(LSQUARE);
				Expr xScreenExpr = parseExpr();
				match(COMMA);
				Expr yScreenExpr = parseExpr();
				match(RSQUARE);
				assignstmt = new ScreenLocationAssignmentStmt(lhsIdent, xScreenExpr, yScreenExpr);
				break;
			case visible:
				consume();
				match(ASSIGN);
				Expr expr = parseExpr();
				assignstmt = new SetVisibleAssignmentStmt(lhsIdent, expr);
				break;
			default:
				error("Unexpected token");
				break;
			}
		}
		//else if(!isKind(t,ASSIGN,DOT))
		else{
			error("i cannot construct the assignment statement");
		}
		match(SEMI);

		return assignstmt;
	}

	private Pixel parsePixel() throws SyntaxException{
		match(LBRACE);
		match(LBRACE);
		Expr redExpr = parseExpr();
		match(COMMA);
		Expr greenExpr = parseExpr();
		match(COMMA);
		Expr blueExpr = parseExpr();
		match(RBRACE);
		match(RBRACE);

		return new Pixel(redExpr, greenExpr, blueExpr);
	}

	private Expr parseExpr() throws SyntaxException{
		Expr condition = parseOrexpr();
		if (isKind(t, QUESTION)) {
			consume();
			Expr trueValue = parseExpr();
			match(COLON);
			Expr falseValue = parseExpr();
			return new ConditionalExpr(condition, trueValue, falseValue);
		}
		return condition;
	}

	private Expr parseOrexpr() throws SyntaxException{
		Expr e0 = null;
		Expr e1 = null;
		e0 = parseAndExpr();
		while (isKind(t, OR)) {
			Token op = t;
			consume();
			e1 = parseAndExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr parseAndExpr() throws SyntaxException{
		Expr e0 = null;
		Expr e1 = null;
		e0 = parseEqualityExpr();
		while(isKind(t, AND)){
			Token op = t;
			consume();
			e1 = parseEqualityExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr parseEqualityExpr() throws SyntaxException{
		Expr e0 = null;
		Expr e1 = null;
		e0 = parseRelExpr();
		while(isKind(t, EQ,NEQ)){
			Token op = t;
			consume();
			e1 = parseRelExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr parseRelExpr() throws SyntaxException{
		Expr e0 = null;
		Expr e1 = null;
		e0 = parseShiftExpr();
		while(isKind(t, LT,GT,LEQ,GEQ)){
			Token op = t;
			consume();
			e1 = parseShiftExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr parseShiftExpr() throws SyntaxException{
		Expr e0 = null;
		Expr e1 = null;
		e0 = parseAddExpr();
		while (isKind(t,LSHIFT,RSHIFT)) {
			Token op = t;
			consume();
			e1 = parseAddExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr parseAddExpr() throws SyntaxException{
		Expr e0 = null;
		Expr e1 = null;
		e0 = parseMultExpr();
		while(isKind(t, PLUS,MINUS)){
			Token op = t;
			consume();
			e1 = parseMultExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr parseMultExpr() throws SyntaxException{
		Expr e0 = null;
		Expr e1 = null;
		e0 = parsePrimaryExpr();
		while(isKind(t,TIMES,DIV,MOD)){
			Token op = t;
			consume();
			e1 = parsePrimaryExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr parsePrimaryExpr() throws SyntaxException{
		Expr e = null;
		switch (t.kind) {
		case INT_LIT:
			Token intLit = t;
			consume();
			e = new IntLitExpr(intLit);
			break;
		case BOOLEAN_LIT:
			Token booleanLit = t;
			consume();
			e = new BooleanLitExpr(booleanLit);
			break;	
		case x:case y:case Z:case SCREEN_SIZE:
			Token constantLit = t;
			consume();
			e = new PreDefExpr(constantLit);
			break;
		case IDENT:
			Token ident = t;
			e = new IdentExpr(ident);
			consume();
			if(isKind(t, LSQUARE)){
				consume();
				Expr xLoc = parseExpr();
				match(COMMA);
				Expr yLoc = parseExpr();
				match(RSQUARE);
				if(isKind(t, red,blue,green)){
					Token color = t;
					consume();
					e = new SampleExpr(ident, xLoc, yLoc, color);
				}
				else error("i need to find a color here.");
			}
			else if(isKind(t, DOT)){
				consume();
				if(isKind(t, width,x_loc,y_loc,height)){
					Token selector = t;
					consume();
					e = new ImageAttributeExpr(ident, selector);
				}
				else error("i need to find a image attribute here");
			}
			break;
		case LPAREN:
			consume();
			e = parseExpr();
			match(Kind.RPAREN);
			break;
		default:
			error("i cannot construct the primary expression.");
			break;
		}
		return e;
	}	
}
