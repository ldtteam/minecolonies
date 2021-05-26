package com.minecolonies.coremod.commands.killcommands;

import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.util.text.StringTextComponent;

public class CommandKillPhantom implements IMCOPCommand
{
    int entitiesKilled = 0;

    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        entitiesKilled = 0;

        context.getSource().getWorld().getEntities().forEach(entity ->
        {
            if (entity instanceof PhantomEntity)
            {
                entity.remove();
                entitiesKilled++;
            }
        });
        context.getSource().sendFeedback(new StringTextComponent(entitiesKilled + " Phantoms killed"), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "phantom";
    }
}
