package cop5555fa13.ast;

import static cop5555fa13.TokenStream.Kind.*;
import static cop5555fa13.TokenStream.Kind;

import java.util.HashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5555fa13.runtime.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {
	


	private ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
	private String progName;
	
	private int slot = 0;
	private int getSlot(String name){
		Integer s = slotMap.get(name);
		if (s != null) return s;
		else{
			slotMap.put(name, slot);
			return slot++;
		}		
	}


	HashMap<String,Integer> slotMap = new HashMap<String,Integer>();
	
	// map to look up JVM types correspondingHashMap<K, V> language
	static final HashMap<Kind, String> typeMap = new HashMap<Kind, String>();
	static {
		typeMap.put(_int, "I");
		typeMap.put(pixel, "I");
		typeMap.put(_boolean, "Z");
		typeMap.put(image, "Lcop5555fa13/runtime/PLPImage;");
	}

	@Override
	public Object visitDec(Dec dec, Object arg) throws Exception{ 
		MethodVisitor mv = (MethodVisitor)arg;
		//insert source line number info into classfile
		Label l = new Label();
		mv.visitLabel(l);
		mv.visitLineNumber(dec.ident.getLineNumber(),l);
		//get name and type
		String varName = dec.ident.getText();
		Kind t = dec.type;
		String jvmType = typeMap.get(t);
		Object initialValue = (t == _int || t==pixel || t== _boolean) ? Integer.valueOf(0) : null;
		//add static field to class file for this variable
		FieldVisitor fv = cw.visitField(ACC_STATIC, varName, jvmType, null,
				initialValue);
		fv.visitEnd();
		//if this is an image, generate code to create an empty image
		if (t == image){
			mv.visitTypeInsn(NEW, PLPImage.className);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, PLPImage.className, "<init>", "()V");
			mv.visitFieldInsn(PUTSTATIC, progName, varName, typeMap.get(image));
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String sourceFileName = (String) arg;
		progName = program.getProgName();
		String superClassName = "java/lang/Object";

		// visit the ClassWriter to set version, attributes, class name and
		// superclass name
		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, progName, null, superClassName,
				null);
		//Optionally, indicate the name of the source file
		cw.visitSource(sourceFileName, null);
		// initialize creation of main method
		String mainDesc = "([Ljava/lang/String;)V";
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", mainDesc, null, null);
		mv.visitCode();
		Label start = new Label();
		mv.visitLabel(start);
		mv.visitLineNumber(program.ident.getLineNumber(), start);		
		
		getSlot("x");
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, slotMap.get("x"));
		getSlot("y");
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, slotMap.get("y"));
		
		//visit children
		for(Dec dec : program.decList){
			dec.visit(this,mv);
		}
		for (Stmt stmt : program.stmtList){
			stmt.visit(this, mv);
		}
			
		//add a return statement to the main method
		mv.visitInsn(RETURN);
		
		//finish up
		Label end = new Label();
		mv.visitLabel(end);
		//visit local variables. The one is slot 0 is the formal parameter of the main method.
		mv.visitLocalVariable("args","[Ljava/lang/String;",null, start, end, getSlot("args"));
		mv.visitLocalVariable("x","I",null,start,end,slotMap.get("x"));
		mv.visitLocalVariable("y","I",null,start,end,slotMap.get("y"));

		//if there are any more local variables, visit them now.
		// ......
		
		//finish up method
		mv.visitMaxs(1,1);
		mv.visitEnd();
		//convert to bytearray and return 
		return cw.toByteArray();
	}

	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		//System.out.println("visiting unimplemented visit method"); 
		MethodVisitor mv = (MethodVisitor)arg;
		alternativeStmt.expr.visit(this, mv);
		
		Label endOfAlternativeLabel = new Label();
		Label ifLabel = new Label();
		Label elseLabel = new Label();
		
		mv.visitJumpInsn(IFEQ, elseLabel);
		mv.visitLabel(ifLabel);
		for (Stmt stmt : alternativeStmt.ifStmtList){
			stmt.visit(this, mv);
		}		
		mv.visitJumpInsn(GOTO, endOfAlternativeLabel);
		
		mv.visitLabel(elseLabel);
		for (Stmt stmt : alternativeStmt.elseStmtList){
			stmt.visit(this, mv);
		}
		
		mv.visitLabel(endOfAlternativeLabel);
		return null;
	}

	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		pauseStmt.expr.visit(this, mv);		
		mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "pause", "(I)V");	
		return null;
	}

	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg)
			throws Exception {
		//System.out.println("visiting unimplemented visit method");  
		MethodVisitor mv = (MethodVisitor)arg;		
		Label guardLabel = new Label();
		Label bodyLabel = new Label();
		
		mv.visitJumpInsn(GOTO, guardLabel);
		mv.visitLabel(bodyLabel);
		for (Stmt stmt : iterationStmt.stmtList){
			stmt.visit(this, mv);
		}
		mv.visitLabel(guardLabel);
		iterationStmt.expr.visit(this, mv);
		mv.visitJumpInsn(IFNE, bodyLabel);
		return null;
	}

	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		//System.out.println("visiting unimplemented visit method"); 
		MethodVisitor mv = (MethodVisitor)arg;
		
		if(assignPixelStmt.type==pixel){
			String varName = assignPixelStmt.lhsIdent.getText();		
			assignPixelStmt.pixel.visit(this, mv);
			mv.visitFieldInsn(PUTSTATIC, progName, varName, "I");			
		} else if(assignPixelStmt.type==image){
			Label xLabel = new Label();
			Label yLabel = new Label();
			Label startLabel = new Label();
			Label endLabel = new Label();
			String imageName = assignPixelStmt.lhsIdent.getText();
			
			mv.visitLdcInsn(0);
			mv.visitVarInsn(ISTORE,slotMap.get("x"));
			//get x and image width and compare them
			mv.visitLabel(startLabel);
			mv.visitVarInsn(ILOAD,slotMap.get("x"));
			mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
			mv.visitFieldInsn(GETFIELD, PLPImage.className, "width", "I");			
			mv.visitJumpInsn(IF_ICMPLT, xLabel);
				mv.visitJumpInsn(GOTO, endLabel);
			mv.visitLabel(xLabel);
				mv.visitVarInsn(ILOAD,slotMap.get("y"));
				mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
				mv.visitFieldInsn(GETFIELD, PLPImage.className, "height", "I");			
				mv.visitJumpInsn(IF_ICMPLT, yLabel);
					mv.visitLdcInsn(0);
					mv.visitVarInsn(ISTORE,slotMap.get("y"));
					mv.visitIincInsn(slotMap.get("x"), 1);
					mv.visitJumpInsn(GOTO, startLabel);
				mv.visitLabel(yLabel);
					mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
					mv.visitVarInsn(ILOAD,slotMap.get("x"));
					mv.visitVarInsn(ILOAD,slotMap.get("y"));
					assignPixelStmt.pixel.visit(this, mv);
					mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "setPixel", "(III)V");
					mv.visitIincInsn(slotMap.get("y"), 1);
					mv.visitJumpInsn(GOTO, xLabel);
				
			mv.visitLabel(endLabel);
			mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		}
		return null;
	}

	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		//System.out.println("visiting unimplemented visit method");  
		MethodVisitor mv = (MethodVisitor)arg;

		pixel.redExpr.visit(this,mv);
		pixel.greenExpr.visit(this,mv);
		pixel.blueExpr.visit(this,mv);
		
		mv.visitMethodInsn(INVOKESTATIC,"cop5555fa13/runtime/Pixel", "makePixel", "(III)I");
		
		return null;
	}

	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave image on top of stack
		String imageName = singlePixelAssignmentStmt.lhsIdent.getText();
		
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		mv.visitInsn(DUP);
		
		singlePixelAssignmentStmt.xExpr.visit(this, mv);
		singlePixelAssignmentStmt.yExpr.visit(this, mv);
		singlePixelAssignmentStmt.pixel.visit(this, mv);
		
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "setPixel", "(III)V");
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave image on top of stack
		String imageName = singleSampleAssignmentStmt.lhsIdent.getText();
		String colorName = singleSampleAssignmentStmt.color.getText();
		
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		mv.visitInsn(DUP);
		
		singleSampleAssignmentStmt.xExpr.visit(this, mv);
		singleSampleAssignmentStmt.yExpr.visit(this, mv);
		if(colorName.equals("red")){
			mv.visitLdcInsn(0);
		}else if(colorName.equals("green")){
			mv.visitLdcInsn(1);
		}else if(colorName.equals("blue")){
			mv.visitLdcInsn(2);
		}
		singleSampleAssignmentStmt.rhsExpr.visit(this, mv);

		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "setSample", "(IIII)V");
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave image on top of stack
		String imageName = screenLocationAssignmentStmt.lhsIdent.getText();
		
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		
		screenLocationAssignmentStmt.xScreenExpr.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "x_loc", "I");
				
		screenLocationAssignmentStmt.yScreenExpr.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "y_loc", "I");	

	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		return null;
		
	}

	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave image on top of stack
		String imageName = shapeAssignmentStmt.lhsIdent.getText();
		
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		
		shapeAssignmentStmt.width.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "width", "I");	
		
		shapeAssignmentStmt.height.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "height", "I");				
		
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateImageSize", PLPImage.updateFrameDesc);
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave image on top of stack
		String imageName = setVisibleAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		//duplicate address.  Will consume one for updating setVisible field
		//and one for invoking updateFrame.
		mv.visitInsn(DUP);
		//visit expr on rhs to leave its value on top of the stack
		setVisibleAssignmentStmt.expr.visit(this,mv);
		//set visible field
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "isVisible", 
				"Z");	
	    //generate code to update frame, consuming the second image address.
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object FileAssignStmt(cop5555fa13.ast.FileAssignStmt fileAssignStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave address of target image on top of stack
	    String image_name = fileAssignStmt.lhsIdent.getText();
	    mv.visitFieldInsn(GETSTATIC, progName, image_name, typeMap.get(image));
	    //generate code to duplicate this address.  We'll need it for loading
	    //the image and again for updating the frame.
	    mv.visitInsn(DUP);
		//generate code to leave address of String containing a filename or url
	    mv.visitLdcInsn(fileAssignStmt.fileName.getText().replace("\"", ""));
		//generate code to get the image by calling the loadImage method
	    mv.visitMethodInsn(INVOKEVIRTUAL, 
	    		PLPImage.className, "loadImage", PLPImage.loadImageDesc);
	    //generate code to update frame
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception {
		//System.out.println("visiting unimplemented visit method"); 
		MethodVisitor mv = (MethodVisitor)arg;
		conditionalExpr.condition.visit(this, mv);
		
		Label falseConditionLabel = new Label();
		Label endOfExprLable = new Label();
		
		mv.visitJumpInsn(IFEQ, falseConditionLabel);
		conditionalExpr.trueValue.visit(this, mv);
		mv.visitJumpInsn(GOTO, endOfExprLable);
		mv.visitLabel(falseConditionLabel);
		conditionalExpr.falseValue.visit(this, mv);
		mv.visitLabel(endOfExprLable);
		
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg)
			throws Exception {
		//System.out.println("visiting unimplemented visit method");  
		MethodVisitor mv = (MethodVisitor)arg;
		binaryExpr.e0.visit(this, mv);
		binaryExpr.e1.visit(this, mv);
		Kind t = binaryExpr.op.kind;
		switch (t) {
		case PLUS:
			mv.visitInsn(IADD);
			break;
		case MINUS:
			mv.visitInsn(ISUB);
			break;
		case TIMES:
			mv.visitInsn(IMUL);
			break;
		case DIV:
			mv.visitInsn(IDIV);
			break;
		case MOD:
			mv.visitInsn(IREM);
			break;
		case LSHIFT:
			mv.visitInsn(ISHL);
			break;
		case RSHIFT:
			mv.visitInsn(ISHR);
			break;
		case LT:
			Label lt0 = new Label();
			Label lt1 = new Label();
			Label lt2 = new Label();
			mv.visitJumpInsn(IF_ICMPLT, lt1);
			mv.visitLabel(lt0);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, lt2);
			mv.visitLabel(lt1);
			mv.visitLdcInsn(1);
			mv.visitLabel(lt2);
			break;
		case GT:
			Label gt0 = new Label();
			Label gt1 = new Label();
			Label gt2 = new Label();
			mv.visitJumpInsn(IF_ICMPGT, gt1);
			mv.visitLabel(gt0);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, gt2);
			mv.visitLabel(gt1);
			mv.visitLdcInsn(1);
			mv.visitLabel(gt2);
			break;
		case LEQ:
			Label leq0 = new Label();
			Label leq1 = new Label();
			Label leq2 = new Label();
			mv.visitJumpInsn(IF_ICMPLE, leq1);
			mv.visitLabel(leq0);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, leq2);
			mv.visitLabel(leq1);
			mv.visitLdcInsn(1);
			mv.visitLabel(leq2);
			break;
		case GEQ:
			Label geq0 = new Label();
			Label geq1 = new Label();
			Label geq2 = new Label();
			mv.visitJumpInsn(IF_ICMPGE, geq1);
			mv.visitLabel(geq0);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, geq2);
			mv.visitLabel(geq1);
			mv.visitLdcInsn(1);
			mv.visitLabel(geq2);
			break;
		case EQ:
			Label eq0 = new Label();
			Label eq1 = new Label();
			Label eq2 = new Label();
			mv.visitJumpInsn(IF_ICMPEQ, eq1);
			mv.visitLabel(eq0);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, eq2);
			mv.visitLabel(eq1);
			mv.visitLdcInsn(1);
			mv.visitLabel(eq2);
			break;
		case NEQ:
			Label neq0 = new Label();
			Label neq1 = new Label();
			Label neq2 = new Label();
			mv.visitJumpInsn(IF_ICMPNE, neq1);
			mv.visitLabel(neq0);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, neq2);
			mv.visitLabel(neq1);
			mv.visitLdcInsn(1);
			mv.visitLabel(neq2);
			break;
		case AND:
			mv.visitInsn(IAND);
			break;
		case OR:
			mv.visitInsn(IOR);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg)
			throws Exception {
		//System.out.println("visiting unimplemented visit method");
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = sampleExpr.ident.getText();
		String colorName = sampleExpr.color.getText();
		
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		sampleExpr.xLoc.visit(this,mv);
		sampleExpr.yLoc.visit(this,mv);
		if(colorName.equals("red")){
			mv.visitLdcInsn(0);
		}else if(colorName.equals("green")){
			mv.visitLdcInsn(1);
		}else if(colorName.equals("blue")){
			mv.visitLdcInsn(2);
		}
		
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getSample", "(III)I");
		
		return null;
	}

	@Override
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception {
		//System.out.println("visiting unimplemented visit method"); 
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = imageAttributeExpr.ident.getText();
		String selectorName = imageAttributeExpr.selector.getText();
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		mv.visitFieldInsn(GETFIELD, PLPImage.className, selectorName, "I");		
		return null;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg)
			throws Exception {
		//System.out.println("visiting unimplemented visit method"); 
		MethodVisitor mv = (MethodVisitor)arg;
		String identName = identExpr.ident.getText();
		Kind t = identExpr.type;
		mv.visitFieldInsn(GETSTATIC, progName,identName,typeMap.get(t));
		return null;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg)
			throws Exception {
		//System.out.println("visiting unimplemented visit method"); 
		MethodVisitor mv = (MethodVisitor)arg;
		String lit = intLitExpr.intLit.getText();
		int val = Integer.parseInt(lit);
		mv.visitLdcInsn(val);
		return null;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String lit = booleanLitExpr.booleanLit.getText();
		int val = lit.equals("true")? 1 : 0;
		mv.visitLdcInsn(val);
		return null;
	}

	@Override
	public Object visitPreDefExpr(PreDefExpr preDefExpr, Object arg)
			throws Exception {
		//System.out.println("visiting unimplemented visit method");
		MethodVisitor mv = (MethodVisitor)arg;
		String lit = preDefExpr.constantLit.getText();
		if (lit.equals("SCREEN_SIZE")) {
			mv.visitFieldInsn(GETSTATIC, PLPImage.className,"SCREENSIZE","I");
		} else if (lit.equals("Z")) {
			mv.visitLdcInsn(255);
		} else if (lit.equals("x")){
			mv.visitVarInsn(ILOAD,slotMap.get("x"));
		} else if (lit.equals("y")){
			mv.visitVarInsn(ILOAD, slotMap.get("y"));
		}
		return null;
	}

	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg)
			throws Exception {
		//System.out.println("visiting unimplemented visit method");  
		MethodVisitor mv = (MethodVisitor)arg;
		String varName = assignExprStmt.lhsIdent.getText();
		Kind t = assignExprStmt.type;
		
		assignExprStmt.expr.visit(this, mv);
		mv.visitFieldInsn(PUTSTATIC, progName, varName, typeMap.get(t));
		
		return null;
	}

}
