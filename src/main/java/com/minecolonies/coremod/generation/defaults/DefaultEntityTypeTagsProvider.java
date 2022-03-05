package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.items.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.EntityTypeTagsProvider;
import net.minecraft.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class DefaultEntityTypeTagsProvider extends EntityTypeTagsProvider
{
    public DefaultEntityTypeTagsProvider(@NotNull final DataGenerator generator,
                                         @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(generator, MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
        tag(ModTags.hostile).add(EntityType.SLIME);
        tag(ModTags.mobAttackBlacklist).add(EntityType.ENDERMAN, EntityType.LLAMA);
    }
}