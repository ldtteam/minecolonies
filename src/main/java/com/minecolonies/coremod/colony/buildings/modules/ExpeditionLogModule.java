package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.coremod.colony.buildings.modules.expedition.ExpeditionLog;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Building module that stores an expedition log
 */
public class ExpeditionLogModule extends AbstractBuildingModule implements IPersistentModule
{
    private static final String TAG_LOG = "expedition";

    @NotNull private final ExpeditionLog log = new ExpeditionLog();
    @Nullable private final ResourceLocation research;

    public ExpeditionLogModule(@Nullable ResourceLocation research)
    {
        this.research = research;
    }

    @NotNull
    public ExpeditionLog getLog()
    {
        return this.log;
    }

    @Override
    public void serializeNBT(@NotNull final CompoundTag compound)
    {
        final CompoundTag log = new CompoundTag();
        this.log.serializeNBT(log);
        compound.put(TAG_LOG, log);
    }

    @Override
    public void deserializeNBT(@NotNull final CompoundTag compound)
    {
        final CompoundTag log = compound.getCompound(TAG_LOG);
        this.log.deserializeNBT(log);
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        final boolean unlocked = research == null || getBuilding().getColony().getResearchManager().getResearchEffects().getEffectStrength(research) > 0;

        buf.writeBoolean(unlocked);
        if (unlocked)
        {
            this.log.serialize(buf);
        }
    }

}
