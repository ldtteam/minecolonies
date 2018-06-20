package com.minecolonies.coremod.commands;

import org.jetbrains.annotations.NotNull;

public final class ActionMenuHolder
{
    @NotNull private final TreeNode<IMenu> treeNode;
    @NotNull private final ActionArgument actionArgument;

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
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actionArgument == null) ? 0 : actionArgument.hashCode());
        result = prime * result + ((treeNode == null) ? 0 : treeNode.hashCode());
        return result;
    }
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ActionMenuHolder other = (ActionMenuHolder) obj;
        if (actionArgument == null)
        {
            if (other.actionArgument != null)
            {
                return false;
            }
        }
        else if (!actionArgument.equals(other.actionArgument))
        {
            return false;
        }
        if (treeNode == null)
        {
            if (other.treeNode != null)
            {
                return false;
            }
        }
        else if (!treeNode.equals(other.treeNode))
        {
            return false;
        }
        return true;
    }
    @Override
    public String toString()
    {
        return "ActionMenuHolder [treeNode=" + treeNode + ", actionArgument=" + actionArgument + "]";
    }
}
