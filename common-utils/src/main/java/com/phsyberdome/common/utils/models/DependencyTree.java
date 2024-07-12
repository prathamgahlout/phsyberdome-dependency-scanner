

package com.phsyberdome.common.utils.models;

import com.phsyberdome.common.utils.CLIHelper;
import com.phsyberdome.common.utils.JSONHelper;
import org.fusesource.jansi.Ansi;
import static org.fusesource.jansi.Ansi.ansi;

/**
 *
 * @author Pratham Gahlout
 */
public class DependencyTree {
    
    private TreeNode rootNode;

    public DependencyTree(TreeNode rootNode) {
        this.rootNode = rootNode;
    }
    
    public DependencyTree(String json) {
        this.rootNode = createFromJson(json);
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(TreeNode rootNode) {
        this.rootNode = rootNode;
    }
    
    private TreeNode createFromJson(String json) {
       Module module = (Module) JSONHelper.convertToObj(Module.class, json);
       
       
       return createFromModuleObj(module);
        
    }
    
    private TreeNode createFromModuleObj(Module module) {
        if(module == null) {
            return null;
        }
        TreeNode rootNode = new TreeNode(module);
        
        for(Module child:module.getDependencies()) {
            TreeNode childNode = createFromModuleObj(child);
            rootNode.children.add(childNode);
        }
        
        return rootNode;
        
    }

    public void prettyPrintTree() {
        CLIHelper.updateCurrentLine("", Ansi.Color.YELLOW);
        CLIHelper.printLine(createTreeWithAnsiChars(rootNode, ""), Ansi.Color.YELLOW);
    }
    
    public String createTreeWithAnsiChars(TreeNode node, String prefix) {
        if(node == null) {
            return "";
        }
        String result = "";
        String nodeName = ansi().fg(Ansi.Color.YELLOW).a(node.getModule().getName() + "@" + node.getModule().getVersion()).reset().toString();
        if(node.getModule().getLicense() != null && node.getModule().getLicense().equals("null")==false) {
            nodeName += ansi().fg(Ansi.Color.GREEN).a("[" + node.getModule().getLicense() + "]");
        }
        result += nodeName + "\n";
        int NO_OF_CHILDREN = node.getChildren().size();
        for(int i=0;i<NO_OF_CHILDREN;i++) {
            TreeNode m = node.getChildren().get(i);
            result += ansi().fg(Ansi.Color.RED).a(prefix).reset();
            if(i==NO_OF_CHILDREN-1){
                result += ansi().fg(Ansi.Color.RED).a(CLIHelper.CORNER).reset();
                result += createTreeWithAnsiChars(m,prefix + CLIHelper.SPACE_2);
            }else{
                result += ansi().fg(Ansi.Color.RED).a(CLIHelper.CROSS).reset();
                result += createTreeWithAnsiChars(m,prefix + CLIHelper.VERTICAL);
            }
        }
        return result;
    }
    
     public String createTree(TreeNode node, String prefix) {
        if(node == null) {
            return "";
        }
        String result = "";
        String nodeName = node.getModule().getName() + "@" + node.getModule().getVersion();
        if(node.getModule().getLicense() != null && node.getModule().getLicense().equals("null")==false) {
            nodeName += "[" + node.getModule().getLicense() + "]";
        }
        result += nodeName + "\n";
        int NO_OF_CHILDREN = node.getChildren().size();
        for(int i=0;i<NO_OF_CHILDREN;i++) {
            TreeNode m = node.getChildren().get(i);
            result += prefix;
            if(i==NO_OF_CHILDREN-1){
                result += CLIHelper.CORNER;
                result += createTree(m,prefix + CLIHelper.SPACE_2);
            }else{
                result += CLIHelper.CROSS;
                result += createTree(m,prefix + CLIHelper.VERTICAL);
            }
        }
        return result;
    }
    
}
