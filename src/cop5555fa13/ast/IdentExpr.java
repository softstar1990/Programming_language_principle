package cop5555fa13.ast;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.Token;

public class IdentExpr extends Expr {	
	Kind type;
	final Token ident;

	public IdentExpr(Token ident) {
		super();
		this.ident = ident;
		this.type = null;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentExpr(this, arg);
	}
	
}
