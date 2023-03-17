package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.DamageSourceKeys;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;


@SuppressWarnings("unchecked")
public class DefaultDamageTagsProvider extends DamageTypeTagsProvider
{
    public DefaultDamageTagsProvider(
      @NotNull final PackOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(final HolderLookup.Provider p_256380_)
    {
        ModTags.init();     // apparently runData doesn't run work queued in common setup

        tag(DamageTypeTags.BYPASSES_ARMOR).add(DamageSourceKeys.WAKEY);
        tag(DamageTypeTags.IS_PROJECTILE).add(DamageSourceKeys.SPEAR);
    }
}
