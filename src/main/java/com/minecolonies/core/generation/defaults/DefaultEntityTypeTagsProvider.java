package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.items.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import java.util.Objects;

import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class DefaultEntityTypeTagsProvider extends TagsProvider<EntityType<?>>
{
    public DefaultEntityTypeTagsProvider(final PackOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider,
      final CompletableFuture<HolderLookup.Provider> unusedLookupProvider)
    {
        super(output, Registries.ENTITY_TYPE, lookupProvider, MOD_ID);
        Objects.requireNonNull(unusedLookupProvider);
    }

    @Override
    protected void addTags(final HolderLookup.Provider holder)
    {
        final TagAppender<EntityType<?>> hostile = tag(ModTags.hostile);
        BuiltInRegistries.ENTITY_TYPE.getResourceKey(EntityTypes.SLIME).ifPresent(hostile::add);

        final TagAppender<EntityType<?>> attackBlacklist = tag(ModTags.mobAttackBlacklist);
        BuiltInRegistries.ENTITY_TYPE.getResourceKey(EntityTypes.ENDERMAN).ifPresent(attackBlacklist::add);
        BuiltInRegistries.ENTITY_TYPE.getResourceKey(EntityTypes.LLAMA).ifPresent(attackBlacklist::add);

        final TagAppender<EntityType<?>> freeToInteractWith = tag(ModTags.freeToInteractWith);
        freeToInteractWith.addOptional(ResourceKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath("corpse", "corpse")));

        final TagAppender<EntityType<?>> raiderTagAppender = tag(ModTags.raiders);
        ModEntities.getRaiders().forEach(raiderType -> raiderTagAppender.add(TagEntry.element(EntityType.getKey(raiderType))));

        tag(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("dynamictrees", "falling_tree_damage_immune")))
                .add(TagEntry.element(EntityType.getKey(ModEntities.CITIZEN)))
                .add(TagEntry.element(EntityType.getKey(ModEntities.VISITOR)));
}
}
