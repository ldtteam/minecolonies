package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;


@SuppressWarnings("unchecked")
public class DefaultDamageTagsProvider extends TagsProvider<DamageType>
{
    public DefaultDamageTagsProvider(
      @NotNull final PackOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider,
      final CompletableFuture<HolderLookup.Provider> unusedLookupProvider)
    {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, Constants.MOD_ID);
        Objects.requireNonNull(unusedLookupProvider);
    }

    @Override
    protected void addTags(final HolderLookup.Provider lookup)
    {
        // Damage types are emitted by the JSON codec provider rather than the
        // built-in registry lookup used by TagsProvider.  Mark these entries
        // optional so MC 26.2's datagen validator does not reject valid
        // cross-provider references; the entries still resolve at runtime.
        tag(DamageTypeTags.BYPASSES_ARMOR)
            .addOptional(DamageSourceKeys.WAKEY)
            .addOptional(DamageSourceKeys.GUARD_PVP)
            .addOptional(DamageSourceKeys.PIERCE);
        tag(DamageTypeTags.IS_PROJECTILE)
            .addOptional(DamageSourceKeys.SPEAR)
            .addOptional(DamageSourceKeys.PIERCE);
        tag(DamageTypeTags.BYPASSES_SHIELD).addOptional(DamageSourceKeys.PIERCE);
    }
}
