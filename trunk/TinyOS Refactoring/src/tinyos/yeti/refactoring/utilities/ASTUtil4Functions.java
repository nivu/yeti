package tinyos.yeti.refactoring.utilities;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclaratorList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.PointerDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.FunctionCall;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.selection.NescFunctionSelectionIdentifier;

public class ASTUtil4Functions {
	
	private ASTUtil astUtil;
	
	public ASTUtil4Functions(){
		astUtil=new ASTUtil();
	}
	
	public ASTUtil4Functions(ASTUtil astUtil) {
		super();
		this.astUtil = astUtil;
	}
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionDeclarationAncestorSequence=new Class[]{
		DeclaratorName.class,
		FunctionDeclarator.class,
		InitDeclarator.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionDeclarationPointerReturntypeAncestorSequence=new Class[]{
		DeclaratorName.class,
		FunctionDeclarator.class,
		PointerDeclarator.class,
		InitDeclarator.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionDefinitionPointerReturntypeAncestorSequence=new Class[]{
		DeclaratorName.class,
		FunctionDeclarator.class,
		PointerDeclarator.class,
		FunctionDefinition.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionDefinitionAncestorSequence=new Class[]{
		DeclaratorName.class,
		FunctionDeclarator.class,
		FunctionDefinition.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionCallAncestorSequence=new Class[]{
		IdentifierExpression.class,
		FunctionCall.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionDefinitionIdentifierSuccessorSequence=new Class[]{
		FunctionDefinition.class,
		FunctionDeclarator.class,
		DeclaratorName.class,
		Identifier.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] declarationIdentifierSuccessorSequence=new Class[]{
		Declaration.class,
		InitDeclaratorList.class,
		InitDeclarator.class,
		FunctionDeclarator.class,
		DeclaratorName.class,
		Identifier.class
	};
	
	/**
	 * Tests if the given identifier is the name of the function, in a function declaration.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function declaration. False otherwise.
	 */
	public boolean isFunctionDeclaration(Identifier identifier){
		return astUtil.checkAncestorSequence(identifier,functionDeclarationAncestorSequence);
	}
	
	/**
	 * Same as isFunctionDeclaration but for pointer returntype.
	 */
	public boolean isFunctionDeclarationPointerReturntype(Identifier identifier){
		return astUtil.checkAncestorSequence(identifier,functionDeclarationPointerReturntypeAncestorSequence);
	}
	
	/**
	 * Tests if the given identifier is the name of the function, in a function definition.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function definition. False otherwise.
	 */
	public boolean isFunctionDefinition(Identifier identifier){
		return astUtil.checkAncestorSequence(identifier,functionDefinitionAncestorSequence);
	}
	
	/**
	 * Same as isFunctionDefinition but for pointer returntype.
	 */
	public boolean isFunctionDefinitionPointerReturntype(Identifier identifier){
		return astUtil.checkAncestorSequence(identifier,functionDefinitionPointerReturntypeAncestorSequence);
	}
	
	/**
	 * Tests if the given identifier is the name of the function, in a function call.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function call. False otherwise.
	 */
	public boolean isFunctionCall(Identifier identifier){
		return astUtil.checkAncestorSequence(identifier,functionCallAncestorSequence);	
	}
	
	/**
	 * Evaluates the FunctionPartType of which this Identifier relates. 
	 * @param identifier
	 * @return
	 */
	public FunctionPart identifyFunctionPart(Identifier identifier){
		if(isFunctionDeclaration(identifier)){
			return FunctionPart.DECLARATION;
		}else if(isFunctionDefinition(identifier)){
			return FunctionPart.DEFINITION;
		}else if(isFunctionCall(identifier)){
			return FunctionPart.CALL;
		}else if(isFunctionDeclarationPointerReturntype(identifier)){
				return FunctionPart.DECLARATION;
		}else if(isFunctionDefinitionPointerReturntype(identifier)){
			return FunctionPart.DEFINITION;
		}
		return FunctionPart.NO_FUNCTION_PART;
	}
	
	/**
	 * Tests if the given identifier is the name of a global function.
	 * @param identifier The identifier which is in question.
	 * @return True if the given identifier is the name of a function. False otherwise and especially if the given identifier is NULL. 
	 */
	public boolean isGlobalFunction(Identifier identifier){
		NescFunctionSelectionIdentifier selection=new NescFunctionSelectionIdentifier(identifier);
		if(selection.isNescFunction(identifier)){
			return false;
		}
		if(identifier==null){
			return false;
		}
		if(isLocalFunction(identifier)){
			return false;
		}
		
		return 	identifyFunctionPart(identifier)!=FunctionPart.NO_FUNCTION_PART;
	}
	
	
	/**
	 * Checks if the given Identifier is part of a local function definiton.
	 * This is done by checking if the identifier is in an implementation scope, 
	 * and if so, if there is a function definition in this scope for the name of
	 * the given identifier.
	 * @param identifier
	 * @return True if this identifier is part of a local function inside an implementation scope.
	 */
	public boolean isLocalFunction(Identifier identifier){
		return getLocalFunctionDefinitionIdentifier(identifier)!=null;
	}
	
	/**
	 * Returns the identifier which is part of the local function definition, if the 
	 * given identifier is part of the same function. I.e. the given identifier is
	 * a function call of the function definition.
	 * @param identifier
	 * @return Null if the given identifier is not part of a local function.
	 */
	public Identifier getLocalFunctionDefinitionIdentifier(Identifier identifier){
		String targetName=identifier.getName();
		//Get the root node for the local implementation of this module.
		ASTNode root=astUtil.getModuleImplementationNodeIfInside(identifier);
		//Test if the identifier is located inside a implementation.
		if(root==null){
			return null;
		}
		//Try to find the functionDefinition with the name of the identifier.
		for(FunctionDefinition definition:astUtil.getChildsOfType(root, FunctionDefinition.class)){
			Identifier id=getIdentifierOfFunctionDefinition(definition);
			if(id!=null&&targetName.equals(id.getName())){
				return id;
			}
		}
		return null;
	}
	
	/**
	 * Returns the identifier which is part of the local function declaration, if the 
	 * given identifier is part of the same function. I.e. the given identifier is
	 * a function call of the function declaration.
	 * @param identifier
	 * @return Null if the given identifier is not part of a local function.
	 */
	public Identifier getLocalFunctionDeclarationIdentifier(Identifier identifier){
		String targetName=identifier.getName();
		//Get the root node for the local implementation of this module.
		ASTNode root=astUtil.getModuleImplementationNodeIfInside(identifier);
		//Test if the identifier is located inside a implementation.
		if(root==null){
			return null;
		}
		//Try to find the functionDeclaration with the name of the identifier.
		for(Declaration declaration:astUtil.getChildsOfType(root, Declaration.class)){
			Identifier id=getIdentifierOfFunctionDeclaration(declaration);
			if(id!=null&&targetName.equals(id.getName())){
				return id;
			}
		}
		return null;
	}
	
	/**
	 * Pulls the identifier out of a functionDefinition.
	 * @param definition
	 * @return	
	 */
	public Identifier getIdentifierOfFunctionDefinition(FunctionDefinition definition){
		return (Identifier)astUtil.checkSuccessorSequence(definition, functionDefinitionIdentifierSuccessorSequence);
	}
	
	/**
	 * Pulls the identifier out of a declaration, if this declaration declares a function.
	 * @param definition
	 * @return	The identifier of the functionDeclaration, null if the given declaration doesn't contain a function declaration.
	 */
	public Identifier getIdentifierOfFunctionDeclaration(Declaration declaration){
		return (Identifier)astUtil.checkSuccessorSequence(declaration, declarationIdentifierSuccessorSequence);
	}
	
	
	/**
	 * Returns the FunctionDefinition of which the given identifier is part.
	 * @param id
	 * @return The ancestor FunctionDefinition. Null if this id is not part of a FunctionDefinition.
	 */
	public FunctionDefinition identifierToFunctionDefinition(Identifier id){
		if(!astUtil.checkAncestorSequence(id, functionDefinitionAncestorSequence)){
			return null;
		}
		return (FunctionDefinition)id.getParent().getParent().getParent();
		
	}
	
	public static enum FunctionPart {
		DECLARATION,
		DEFINITION,
		CALL,
		NO_FUNCTION_PART
}
}
