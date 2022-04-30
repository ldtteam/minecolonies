package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.items.ItemArcheologistLoot;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class DefaultItemModelProvider extends ItemModelProvider
{
    public DefaultItemModelProvider(final DataGenerator generator, final ExistingFileHelper existingFileHelper)
    {
        super(generator, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        ModItems.archeologistLootItems
          .stream()
          .filter(ItemArcheologistLoot.class::isInstance)
          .map(ItemArcheologistLoot.class::cast)
          .forEach(this::actOnItem);
    }

    private void actOnItem(
      final ItemArcheologistLoot item
      )
    {
        final ItemModelBuilder builder = singleTexture(
          Objects.requireNonNull(item.getRegistryName()).toString(),
          new ResourceLocation("generated"),
          "layer0",
         new ResourceLocation(Constants.MOD_ID, "items/" + item.getRegistryName().getPath()));
    }
}
