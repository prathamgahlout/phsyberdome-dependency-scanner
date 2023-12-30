

package com.phsyberdome.drona.Models;

import com.phsyberdome.drona.CLIHelper;
import com.phsyberdome.drona.Utils.JSONHelper;
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
        printNode(rootNode, "");
    }
    
    private void printNode(TreeNode node, String prefix) {
        if(node == null) {
            return;
        }
        String nodeName = node.getModule().getName() + "@" + node.getModule().getVersion();
        if(node.getModule().getLicense() != null && node.getModule().getLicense().equals("null")==false) {
            nodeName += ansi().fg(Ansi.Color.GREEN).a("[" + node.getModule().getLicense() + "]");
        }
        
        CLIHelper.printLine(nodeName, Ansi.Color.YELLOW);
        int NO_OF_CHILDREN = node.getChildren().size();
        for(int i=0;i<NO_OF_CHILDREN;i++) {
            TreeNode m = node.getChildren().get(i);
            CLIHelper.print(prefix, Ansi.Color.RED);
            if(i==NO_OF_CHILDREN-1){
                CLIHelper.print(CLIHelper.CORNER, Ansi.Color.RED);
                printNode(m, prefix + CLIHelper.SPACE_2);
            }else{
                CLIHelper.print(CLIHelper.CROSS, Ansi.Color.RED);
                printNode(m, prefix + CLIHelper.VERTICAL);
            }
        }
    }
    
    
    
}
