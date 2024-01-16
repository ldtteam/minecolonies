package com.minecolonies.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.List;

public class CommandTree
{
    /**
     * Tree root node
     */
    private final LiteralArgumentBuilder<CommandSourceStack> rootNode;
    /**
     * List of child trees, commands are directly baked into rootNode
     */
    private final List<CommandTree>                     childNodes;

    /**
     * Creates new command tree.
     *
     * @param commandName root vertex name
     */
    protected CommandTree(final String commandName)
    {
        rootNode = LiteralArgumentBuilder.literal(commandName);
        childNodes = new ArrayList<>();
    }

    /**
     * Adds new tree as leaf into this tree.
     *
     * @param tree new tree to add
     * @return this
     */
    protected CommandTree addNode(final CommandTree tree)
    {
        childNodes.add(tree);
        return this;
    }

    /**
     * Adds new command as leaf into this tree.
     *
     * @param command new commnad to add
     * @return this
     */
    protected CommandTree addNode(final LiteralArgumentBuilder<CommandSourceStack> command)
    {
        rootNode.then(command.build());
        return this;
    }

    /**
     * Builds whole tree for dispatcher.
     *
     * @return tree as command node
     */
    protected LiteralArgumentBuilder<CommandSourceStack> build()
    {
        for (final CommandTree ct : childNodes)
        {
            addNode(ct.build());
        }
        return rootNode;
    }
}
