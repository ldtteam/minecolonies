package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.items.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class DefaultEntityTypeTagsProvider extends EntityTypeTagsProvider
{
    public DefaultEntityTypeTagsProvider(final PackOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider holder)
    {
        tag(ModTags.hostile).add(EntityType.SLIME);
        tag(ModTags.mobAttackBlacklist).add(EntityType.ENDERMAN, EntityType.LLAMA);
        tag(ModTags.freeToInteractWith).addOptional(new ResourceLocation("corpse", "corpse"));
        tag(ModTags.beekeeperAnimals).add(EntityType.BEE);
        tag(ModTags.chickenHerderAnimals).add(EntityType.CHICKEN);
        tag(ModTags.cowboyMilkableAnimals).add(EntityType.COW, EntityType.GOAT);
        tag(ModTags.cowboyStewableAnimals).add(EntityType.MOOSHROOM);
        tag(ModTags.cowboyAnimals).addTag(ModTags.cowboyMilkableAnimals).addTag(ModTags.cowboyStewableAnimals);
        tag(ModTags.rabbitHerderAnimals).add(EntityType.RABBIT);
        tag(ModTags.shepherdShearableAnimals).add(EntityType.SHEEP);
        tag(ModTags.shepherdAnimals).addTag(ModTags.shepherdShearableAnimals);
        tag(ModTags.stablemasterAnimals).add(EntityType.HORSE);
        tag(ModTags.swineHerderAnimals).add(EntityType.PIG);

        final TagAppender<EntityType<?>> raiderTagAppender = tag(ModTags.raiders);
        ModEntities.getRaiders().forEach(raiderType -> raiderTagAppender.add(TagEntry.element(EntityType.getKey(raiderType))));
    }
}
