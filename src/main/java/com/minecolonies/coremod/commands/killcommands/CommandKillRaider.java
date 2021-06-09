package com.minecolonies.coremod.commands.killcommands;

import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.StringTextComponent;

public class CommandKillRaider implements IMCOPCommand
{
    private int entitiesKilled = 0;

    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        entitiesKilled = 0;

        context.getSource().getLevel().getEntities().forEach(entity ->
        {
            if (entity instanceof AbstractEntityMinecoloniesMob)
            {
                final AbstractEntityMinecoloniesMob mob = (AbstractEntityMinecoloniesMob) entity;
                mob.die(new DamageSource("despawn"));
                mob.remove();

                final IColonyEvent event = mob.getColony().getEventManager().getEventByID(mob.getEventID());

                if (event != null)
                {
                    event.setStatus(EventStatus.DONE);
                }

                entitiesKilled++;
            }
        });
        context.getSource().sendSuccess(new StringTextComponent(entitiesKilled + " entities killed"), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "raider";
    }
}
