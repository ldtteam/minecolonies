package com.minecolonies.api.colony.capability;

import com.minecolonies.api.colony.IColony;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Capability for the colony tag for chunks
 */
public interface IColonyManagerCapability
{
    /**
     * Create a colony and return it.
     *
     * @param w   the world the colony is in.
     * @param pos the position of the colony.
     * @return the created colony.
     */
    IColony createColony(@NotNull final Level w, @NotNull final BlockPos pos);

    /**
     * Delete a colony with a certain id.
     *
     * @param id the id of the colony.
     */
    void deleteColony(final int id);

    /**
     * Get a colony with a certain id.
     *
     * @param id the id of the colony.
     * @return the colony or null.
     */
    @Nullable
    IColony getColony(final int id);

    /**
     * Get a list of all colonies.
     *
     * @return a complete list.
     */
    List<IColony> getColonies();

    /**
     * add a new colony to the capability.
     *
     * @param colony the colony to add.
     */
    void addColony(IColony colony);

    /**
     * Get the top most id of all colonies.
     *
     * @return the top most id.
     */
    int getTopID();

    @Nullable
    static IColonyManagerCapability getCapability(final Level level)
    {
        final ColonyManagerCapability cap;
        if (level instanceof final ServerLevel serverLevel)
        {
            cap = serverLevel.getDataStorage().computeIfAbsent(ColonyManagerCapability.FACTORY, ColonyManagerCapability.NAME);
        }
        else if (level instanceof ClientLevel)
        {
            // TODO: client getter or throw
        }
        else
        {
            cap = null;
        }

        if (cap != null)
        {
            cap.processAfterLoadHook(level.dimension() == Level.OVERWORLD);
        }
        return cap;
    }
}
