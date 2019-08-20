package com.minecolonies.coremod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;

public class TestCMD implements IMCCommand
{
    private final static String name = "TestCommand";

    @Override
    public int onExecute(final CommandContext<CommandSource> command)
    {
        return 0;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName()).executes(this::onExecute);
    }
}
