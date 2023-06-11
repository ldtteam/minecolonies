package com.minecolonies.coremod.commands.killcommands;

import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;

public class CommandKillAnimal implements IMCOPCommand
{
    private int entitiesKilled = 0;

    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public final int onExecute(final CommandContext<CommandSourceStack> context)
    {
        entitiesKilled = 0;

        context.getSource().getLevel().getEntities().getAll().forEach(entity ->
        {
            if (entity instanceof Animal)
            {
                entity.remove(Entity.RemovalReason.DISCARDED);
                entitiesKilled++;
            }
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
        return "animals";
    }
}
