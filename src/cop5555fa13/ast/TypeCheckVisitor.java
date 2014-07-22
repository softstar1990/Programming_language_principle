package cop5555fa13.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cop5555fa13.TokenStream.Kind;
import static cop5555fa13.TokenStream.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {
	
	List<ASTNode> errorNodeList;
	StringBuilder errorLog;
	HashMap<String,Kind> symbolTable;

	public TypeCheckVisitor() {
		symbolTable = new HashMap<String,Kind>();
		errorNodeList = new ArrayList<ASTNode>();
		errorLog = new StringBuilder();
	}
	
	public List<ASTNode> getErrorNodeList(){
		return errorNodeList;
	}
	
	public boolean isCorrect(){
		return errorNodeList.size()==0;
	}
	
	public String getLog(){
		return errorLog.toString();
	}
	
	public boolean check(boolean expr, ASTNode node, String str){
		if(!expr){
			errorNodeList.add(node);
			errorLog.append(str);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public Object visitDec(Dec dec, Object arg){
		if(!symbolTable.containsKey(dec.ident.getText())){
			symbolTable.put(dec.ident.getText(), dec.type);
		}else{
			errorNodeList.add(dec);
			errorLog.append(dec+" | there is an identifier has already been declared\n");
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		symbolTable.put(program.getProgName(), IDENT);
		for (Dec dec: program.decList){
			dec.visit(this,null);
		}
		for (Stmt stmt: program.stmtList){
			stmt.visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		Kind exprType = (Kind) alternativeStmt.expr.visit(this, null);
		check(exprType==_boolean,alternativeStmt," | Expt must be boolean\n");
		for (Stmt stmt: alternativeStmt.ifStmtList){ stmt.visit(this, null);}
		for (Stmt stmt: alternativeStmt.elseStmtList){stmt.visit(this, null);}
		return null;
	}

	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg)
			throws Exception {
		Kind exprType = (Kind) pauseStmt.expr.visit(this, null);
		check(exprType==_int,pauseStmt," | Expr must be int\n");
		return null;
	}

	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg)
			throws Exception {
		Kind exprType = (Kind) iterationStmt.expr.visit(this, null);
		check(exprType==_boolean,iterationStmt," | Expr must be boolean\n");
		for (Stmt stmt: iterationStmt.stmtList){stmt.visit(this, null);}
		return null;
	}

	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		String ident = assignPixelStmt.lhsIdent.getText();
		Kind type = symbolTable.get(ident);
		assignPixelStmt.type = type;
		
		check((type==pixel | type==image),assignPixelStmt,assignPixelStmt + " | must be pixel or image\n");
		
		Pixel p = assignPixelStmt.pixel;
		p.visit(this, null);
		
		return null;
	}

	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		Kind redType = (Kind) pixel.redExpr.visit(this, null);
		check(redType==_int,pixel," | Red Expr must be int\n");
		Kind greenType = (Kind) pixel.greenExpr.visit(this, null);
		check(greenType==_int,pixel," | Green Expr must be int\n");
		Kind blueType = (Kind) pixel.blueExpr.visit(this, null);
		check(blueType==_int,pixel," | Blue Expr must be int\n");
		return null;
	}

	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg)
			throws Exception {
		String ident = singlePixelAssignmentStmt.lhsIdent.getText();
		Kind type = symbolTable.get(ident);
		check(type==image,singlePixelAssignmentStmt,singlePixelAssignmentStmt + " | must be image\n");
		Kind xType = (Kind) singlePixelAssignmentStmt.xExpr.visit(this, null);
		check(xType==_int,singlePixelAssignmentStmt," | xExpr must be int\n");
		Kind yType = (Kind) singlePixelAssignmentStmt.yExpr.visit(this, null);
		check(yType==_int,singlePixelAssignmentStmt," | yExpr must be int\n");
		
		Pixel p = singlePixelAssignmentStmt.pixel;
		p.visit(this, null);
		
		return null;
	}

	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg)
			throws Exception {
		String ident = singleSampleAssignmentStmt.lhsIdent.getText();
		Kind type = symbolTable.get(ident);
		check(type==image,singleSampleAssignmentStmt,singleSampleAssignmentStmt + " | must be image\n");
		
		Kind xType = (Kind) singleSampleAssignmentStmt.xExpr.visit(this, null);
		check(xType == _int, singleSampleAssignmentStmt, " | xExpr must be int\n");
		Kind yType = (Kind) singleSampleAssignmentStmt.yExpr.visit(this, null);
		check(yType == _int, singleSampleAssignmentStmt, " | yExpr must be int\n");
		Kind rhsType = (Kind) singleSampleAssignmentStmt.rhsExpr.visit(this, null);
		check(rhsType == _int, singleSampleAssignmentStmt, " | rhsExpr must be int\n");
		return null;
	}

	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		String ident = screenLocationAssignmentStmt.lhsIdent.getText();
		Kind type = symbolTable.get(ident);
		check(type==image,screenLocationAssignmentStmt,screenLocationAssignmentStmt+" | must be image\n");
		
		Kind xType = (Kind) screenLocationAssignmentStmt.xScreenExpr.visit(this, null);
		check(xType==_int, screenLocationAssignmentStmt, " | xExpr must be int\n");
		
		Kind yType = (Kind) screenLocationAssignmentStmt.yScreenExpr.visit(this, null);
		check(yType==_int, screenLocationAssignmentStmt, " | yExpr must be int\n");
		return null;
	}

	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg)
			throws Exception {
		String ident = shapeAssignmentStmt.lhsIdent.getText();
		Kind type = symbolTable.get(ident);
		check(type==image,shapeAssignmentStmt,shapeAssignmentStmt+" | must be image\n");
		
		Kind widthType = (Kind) shapeAssignmentStmt.width.visit(this, null);
		check(widthType==_int,shapeAssignmentStmt," | widthExpr must be int\n");
		
		Kind heightType = (Kind) shapeAssignmentStmt.height.visit(this, null);
		check(heightType==_int,shapeAssignmentStmt," | heightExpr must be int\n");
		return null;
	}

	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg)
			throws Exception {
		String ident = setVisibleAssignmentStmt.lhsIdent.getText();
		Kind type = symbolTable.get(ident);
		check(type==image,setVisibleAssignmentStmt,setVisibleAssignmentStmt+" | must be image\n");
		
		Kind exprType = (Kind) setVisibleAssignmentStmt.expr.visit(this, null);
		check(exprType==_boolean,setVisibleAssignmentStmt," | Expr must be boolean\n");
		return null;
	}

	@Override
	public Object FileAssignStmt(cop5555fa13.ast.FileAssignStmt fileAssignStmt,
			Object arg) throws Exception {
		String ident = fileAssignStmt.lhsIdent.getText();
		Kind type = symbolTable.get(ident);
		check(type==image,fileAssignStmt,fileAssignStmt+" | must be image\n");		
		return null;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception {
		Kind conditionExpr = (Kind) conditionalExpr.condition.visit(this, null);
		check(conditionExpr==_boolean,conditionalExpr,conditionalExpr+" | must be boolean\n");
		
		Kind trueType = (Kind) conditionalExpr.trueValue.visit(this, null);
		Kind falseType = (Kind) conditionalExpr.falseValue.visit(this, null);
		check(trueType==falseType, conditionalExpr, " | TrueExpr and FalseExpr should have the same type\n");
		
		return trueType;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg)
			throws Exception {
		Kind e0Type = (Kind) binaryExpr.e0.visit(this, null);
		Kind e1Type = (Kind) binaryExpr.e1.visit(this, null);
		Kind opType = binaryExpr.op.kind;
		switch (opType) {
		case AND: case OR:
			check(e0Type==_boolean,binaryExpr," | e0 Expr must be boolean\n");
			check(e1Type==_boolean,binaryExpr," | e1 Expr must be boolean\n");
			return _boolean;
		case PLUS: case MINUS: case TIMES: case DIV: case MOD:
			check(e0Type==_int,binaryExpr," | e0 Expr must be int\n");
			check(e1Type==_int,binaryExpr," | e1 Expr must be int\n");
			return _int;
		case EQ: case NEQ:
			check(e0Type==e1Type,binaryExpr," | e0 Expr and e1 Expr should have the same type\n");
			return _boolean;
		case LSHIFT: case RSHIFT:
			check(e0Type==_int,binaryExpr," | eo Expr must be int\n");
			check(e1Type==_int,binaryExpr," | e1 Expr must be int\n");
			return _int;
		case LT: case GT: case LEQ: case GEQ:
			check(e0Type==_int,binaryExpr," | e0 Exp must be int\n");
			check(e1Type==_int,binaryExpr," | e1 Expr must be int\n");
			return _boolean;
		default:
			return null;
		}
	}

	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg)
			throws Exception {
		String identname = sampleExpr.ident.getText();
		Kind type = symbolTable.get(identname);
		check(type==image,sampleExpr,sampleExpr+" | must be image\n");
		
		Kind xlocType = (Kind) sampleExpr.xLoc.visit(this, null);
		check(xlocType==_int,sampleExpr," | xlocExpr must be int\n");
		
		Kind ylocType = (Kind) sampleExpr.yLoc.visit(this, null);
		check(ylocType==_int,sampleExpr," | ylocExpr must be int\n");	
		return _int;
	}

	@Override
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception {
		String identname = imageAttributeExpr.ident.getText();
		Kind type = symbolTable.get(identname);
		check(type==image,imageAttributeExpr,imageAttributeExpr + " | must be image\n");
		return _int;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg)
			throws Exception {
		String identname = identExpr.ident.getText();
		Kind type = symbolTable.get(identname);
		identExpr.type = symbolTable.get(identname);
		return type;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg)
			throws Exception {
		return _int;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg)
			throws Exception {
		return _boolean;
	}

	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)
			throws Exception {
		return _int;
	}

	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg)
			throws Exception {
		String ident = assignExprStmt.lhsIdent.getText();
		Kind type = symbolTable.get(ident);
		assignExprStmt.type = type;
		Kind exprType = (Kind) assignExprStmt.expr.visit(this, null);
		check(type==exprType,assignExprStmt,assignExprStmt+" | should have type consistancy\n");
		return null;
	}

}
