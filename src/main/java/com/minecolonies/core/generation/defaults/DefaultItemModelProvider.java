package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.items.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Datagen for item models
 */
public class DefaultItemModelProvider extends ItemModelProvider
{
    /**
     * Constructor
     */
    public DefaultItemModelProvider(final DataGenerator generator, final ExistingFileHelper existingFileHelper)
    {
        super(generator, MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        final ResourceLocation disabledGoggles = modLoc("build_goggles_disabled");
        basicItem(disabledGoggles);
        basicItem(ModItems.buildGoggles)
                .override()
                    .predicate(new ResourceLocation("disabled"), 1.0F)
                    .model(getExistingFile(disabledGoggles))
                .end();
    }

    /**
     * Apparently MineColonies does not use the correct path for item textures...
     */
    @Override
    public ItemModelBuilder basicItem(final ResourceLocation item)
    {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", new ResourceLocation(item.getNamespace(), "items/" + item.getPath()));
    }
}
