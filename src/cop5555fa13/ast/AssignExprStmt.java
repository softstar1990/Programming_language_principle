package cop5555fa13.ast;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.Token;

public class AssignExprStmt extends AssignStmt {
	final Token lhsIdent;
	final Expr expr;
	Kind type;
	public AssignExprStmt(Token lhsIdent, Expr expr) {
		super();
		this.lhsIdent = lhsIdent;
		this.expr = expr;
		this.type = null;
	}
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitAssignExprStmt(this, arg);
	}

}
