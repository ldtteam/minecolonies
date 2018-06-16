package com.minecolonies.coremod.commands;

import org.jetbrains.annotations.NotNull;

public final class ActionMenuHolder
{
    @NotNull private final TreeNode<IMenu> treeNode;
    @NotNull private final ActionArgument actionArgument;
    private Object value;

    ActionMenuHolder(@NotNull final TreeNode<IMenu> treeNode, @NotNull final ActionArgument actionArgument)
    {
        super();
        this.treeNode = treeNode;
        this.actionArgument = actionArgument;
    }
    public TreeNode<IMenu> getTreeNode()
    {
        return treeNode;
    }
    public ActionArgument getActionArgument()
    {
        return actionArgument;
    }
}
