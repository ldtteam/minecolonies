package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;


@SuppressWarnings("unchecked")
public class DefaultDamageTagsProvider extends TagsProvider<DamageType>
{
    public DefaultDamageTagsProvider(
      @NotNull final PackOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider, final ExistingFileHelper helper)
    {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, Constants.MOD_ID, helper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider lookup)
    {
        tag(DamageTypeTags.BYPASSES_ARMOR).add(DamageSourceKeys.WAKEY, DamageSourceKeys.GUARD_PVP);
        tag(DamageTypeTags.IS_PROJECTILE).add(DamageSourceKeys.SPEAR);
    }
}
