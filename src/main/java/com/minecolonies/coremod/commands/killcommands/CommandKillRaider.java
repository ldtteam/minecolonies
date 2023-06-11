package com.minecolonies.coremod.commands.killcommands;

import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class CommandKillRaider implements IMCOPCommand
{
    private int entitiesKilled = 0;

    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        entitiesKilled = 0;

        context.getSource().getLevel().getEntities().getAll().forEach(entity ->
        {
            if (entity instanceof AbstractEntityMinecoloniesMob)
            {
                final AbstractEntityMinecoloniesMob mob = (AbstractEntityMinecoloniesMob) entity;
                mob.die(context.getSource().getLevel().damageSources().source(DamageSourceKeys.CONSOLE));
                mob.remove(Entity.RemovalReason.DISCARDED);

                final IColonyEvent event = mob.getColony().getEventManager().getEventByID(mob.getEventID());

                if (event != null)
                {
                    event.setStatus(EventStatus.DONE);
                }

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
        return "raider";
    }
}
