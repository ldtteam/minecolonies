package com.minecolonies.core.commands.killcommands;

import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class CommandKillPig implements IMCOPCommand
{
    int entitiesKilled = 0;

    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        entitiesKilled = 0;

        context.getSource().getLevel().getEntities(EntityType.PIG, entity -> true).forEach(entity ->
        {
            entity.remove(Entity.RemovalReason.DISCARDED);
            entitiesKilled++;
        });
        context.getSource().sendSuccess(() -> Component.literal(entitiesKilled + " entities killed"), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "pig";
    }
}
