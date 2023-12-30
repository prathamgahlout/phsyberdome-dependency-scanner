

package com.phsyberdome.drona.Models;

import java.util.ArrayList;

/**
 *
 * @author Pratham Gahlout
 */
public class TreeNode {
    private Module module;
    
    ArrayList<TreeNode> children;

    public TreeNode(Module module, ArrayList<TreeNode> children) {
        this.module = module;
        this.children = children;
    }

    public TreeNode(Module module) {
        this.module = module;
        this.children = new ArrayList<>();
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public ArrayList<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<TreeNode> children) {
        this.children = children;
    }
    
    
    
}
