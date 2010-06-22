package tinyos.yeti.refactoring;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.ui.IEditorPart;

import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.parser.INesCAST;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.preprocessor.PreprocessorReader;

/**
 * Conglomeration of Methods that are useful while working whit the AST in a Refactoring Plugin
 */
public class ASTUtil {
	
	private NesC12AST ast;
	private PreprocessorReader reader;
	
	/**
	 * Finds the editor by itself. Attention, this Constructor works only if the Editor has the Focus. 
	 * As soon as an other Window opens, this is no longer the case.
	 * @throws NullPointerException If the AST is not yet initialized.
	 * @throws IllegalStateException If the found AST or Editor is not of the expected type.
	 */
	public ASTUtil(){
		IEditorPart editorPart = 
			RefactoringPlugin.
				getDefault().
				getWorkbench().
				getActiveWorkbenchWindow().
				getActivePage().
				getActiveEditor();
		
		NesCEditor editor = null;
		if(editorPart instanceof NesCEditor){
			editor = (NesCEditor)editorPart;
		} else if (editorPart instanceof MultiPageNesCEditor) {
			editor = ((MultiPageNesCEditor) editorPart).getNesCEditor();
		} else {
			throw new IllegalStateException("Found editor was not a NesCEditor but a " + editorPart.getClass().getCanonicalName());
		}
		INesCAST ast = editor.getAST();
		if(ast instanceof NesC12AST){
			init((NesC12AST) ast);
		} else if(ast == null){
			throw new NullPointerException("The AST must not be NULL");
		} else {
			(new Exception()).printStackTrace();
			throw new IllegalStateException("The AST of the Editor has to be a NesC12AST but was "+ ast.getClass().getCanonicalName());
		}
	}
	
	/**
	 * 
	 * @param ast The AST to be used. Must not be NULL
	 * @throws NullPointerException if the ast is NULL
	 */
	public ASTUtil(NesC12AST ast){
		if(ast == null){
			throw new NullPointerException("The given AST must not be NULL");
		}
		init(ast);
	}
	
	
	private void init(NesC12AST ast){
		this.ast=ast;
		reader=ast.getReader();
	}
	/**
	 * Method returns the AST-Leaf that relates to the Position specified in pos in the not preprocessed input file. 
	 * @param pos Position in the original Input File
	 * @return The AST Leaf that covers this Position, or null if the Position is not covered by a leaf.
	 */
	public  ASTNode getASTLeafAtPos(int pos){
		ASTNode root = ast.getRoot();
		
		boolean foundChild=true;
		while(root.getChildrenCount() > 0 && foundChild){
			foundChild=false;
			for(int i=0; i < root.getChildrenCount()&& !foundChild; i++){
				ASTNode child = root.getChild(i);
				
				// It happend to us that we got null values
				if(child!=null){
					if(end(child) >= pos){
						foundChild=true;
						root=root.getChild(i);
					}
				}
			}	
		}
		
		// Cause it's only checked if end(child) >= pos the start has to be checked too.  
		if(foundChild && pos >= start(root)){
			return root;
		} else {
			// Happens for example if the Cursor is at a blank position
			return null;
		}
	}
	
/**
 * 
 * @param <T> The type which the Leaf is you are looking for. 
 * @param pos The Position where the leaf you are looking for is.
 * @param type	The type which the Leaf is you are looking for. 
 * @throws ClassCastException If the Leaf at Position pos is not of Type T
 * @return
 */
	@SuppressWarnings("unchecked") // Eclipse thinks that we have a unchecked Class cast. But it's not unchecked.
	public <T extends ASTNode> T getASTLeafAtPos(int pos,Class<T> type) throws ClassCastException{
		ASTNode currentlySelected = this.getASTLeafAtPos(pos);
		
		if(type.isInstance(currentlySelected)){
			return (T) currentlySelected;
		} else {
			throw new ClassCastException("The Element at Poition "+pos+
					" is not a "+type.getName() + " but of Type " + 
					currentlySelected.getClass().getName());
		}
	}
	
	/**
	 * Returns the next higher AST node of type
	 * @param child The AST node to start from
	 * @param type The type of the ASTNode we are looking for
	 * @return null if no parent matches, else matching Parent.
	 */
	public static ASTNode getParentForName(ASTNode child,Class<? extends ASTNode> type){
		ASTNode parent=child.getParent();
		if(parent==null){
			return null;
		}
		if(parent.getClass().equals(type)){
			return parent;
		}
		return getParentForName(parent, type);
	}
	
	/**
	 * 
	 * @param root ASTNode which child's are checked for being Identifier with name indentifierName 
	 * @param identifierName Name of the Identifier you are looking for
	 * @param stopClass 
	 * @return A list with all occurrences of Identifiers below the root parameter in the AST
	 */
	public static <T> Collection<Identifier> getIncludedIdentifiers(ASTNode root, String identifierName,Class<T> stopClass){
		LinkedList<Identifier> ret = new LinkedList<Identifier>();
		getIncludedIdentifiers_sub(root, identifierName, ret,stopClass);
		return ret;
	}
	
	private static <T> void getIncludedIdentifiers_sub(ASTNode root,String identifierName,Collection<Identifier> result,Class<T> stopClass){
		ASTNode child=null;
		Identifier identifier=null;
		for(int i=0;i<root.getChildrenCount();++i){
			child=root.getChild(i);
			if(child!=null){
				if(child instanceof Identifier){
					identifier=(Identifier)child;
					if(identifier.getName().equals(identifierName)){
						result.add(identifier);
					}
				} else if(!child.getClass().equals(stopClass)){
					getIncludedIdentifiers_sub(child, identifierName, result,stopClass);
				}
			}
		}
	}
	
	/**
	 * Returns the Begin-Offset of node in the not preprocessed input file
	 * @param node The node you wan't to know the offset of.
	 * @return	Well, the offset.
	 */
	public int start(ASTNode node){
		return reader.inputLocation(ast.getOffsetAtBegin(node).getPreprocessedOffset(), true);
	}

	/**
	 * As start, just the end.
	 */
	public int end(ASTNode node){
		return reader.inputLocation(ast.getOffsetAtEnd(node).getPreprocessedOffset(), true);
	}
	
	/**
	 * Returns the Children of a node as Collection of ASTNode
	 * @param node
	 * @return
	 */
	public static Collection<ASTNode> getChilds(ASTNode node){
		Collection<ASTNode> ret = new LinkedList<ASTNode>();
		for(int i = 0; i < node.getChildrenCount(); i++){
			ret.add(node.getChild(i));
		}
		return ret;
	}

}
