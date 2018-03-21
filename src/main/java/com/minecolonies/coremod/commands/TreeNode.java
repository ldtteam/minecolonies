package com.minecolonies.coremod.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// From https://stackoverflow.com/a/40622616/1394993

public class TreeNode<T>
{
    private T data = null;
    private final List<TreeNode<T>> children = new ArrayList<>();
    private TreeNode<T> parent = null;

    public TreeNode(final T data)
    {
        this.data = data;
    }

    public void addChild(final TreeNode<T> child)
    {
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(final T childData)
    {
        final TreeNode<T> newChild = new TreeNode<>(childData);
        newChild.setParent(this);
        children.add(newChild);
    }

    public void addChildren(final List<TreeNode<T>> moreChildren)
    {
        for (final TreeNode<T> t : moreChildren)
        {
            t.setParent(this);
        }
        this.children.addAll(moreChildren);
    }

    public List<TreeNode<T>> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    public T getData()
    {
        return data;
    }

    public void setData(final T data)
    {
        this.data = data;
    }

    private void setParent(final TreeNode<T> parent)
    {
        this.parent = parent;
    }

    public TreeNode<T> getParent()
    {
        return parent;
    }

    public boolean hasChildren()
    {
        return !children.isEmpty();
    }
}
