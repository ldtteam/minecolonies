package com.minecolonies.core.generation;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.component.ModDataComponents;
import com.minecolonies.api.util.CraftingUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Automatically calculated vanilla level nbts of items.
 */
public class ItemNbtCalculator implements DataProvider
{
    private final PackOutput                               packOutput;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public ItemNbtCalculator(@NotNull final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        this.packOutput = packOutput;
        this.lookupProvider = lookupProvider;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "ItemNBTCalculator";
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        final List<ItemStack> allStacks;
        final HolderLookup.Provider provider = lookupProvider.join();
        final ImmutableList.Builder<ItemStack> listBuilder = new ImmutableList.Builder<>();
        final CreativeModeTab.ItemDisplayParameters tempDisplayParams = new CreativeModeTab.ItemDisplayParameters(FeatureFlags.REGISTRY.allFlags(), false, provider);

        CraftingUtils.forEachCreativeTabItems(tempDisplayParams, (tab, stacks) ->
        {
            for (final ItemStack item : stacks)
            {
                if (item.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof IMateriallyTexturedBlock texturedBlock)
                {
                    final Map<ResourceLocation, Block> texturedComponents = new HashMap<>();
                    for (final IMateriallyTexturedBlockComponent key : texturedBlock.getComponents())
                    {
                        texturedComponents.put(key.getId(), key.getDefault());
                    }
                    final ItemStack copy = item.copy();
                    copy.set(ModDataComponents.TEXTURE_DATA, new MaterialTextureData(texturedComponents));
                    listBuilder.add(copy);
                }
                else
                {
                    listBuilder.add(item);
                }
            }
        });

        allStacks = listBuilder.build();

        final TreeMap<String, Set<String>> keyMapping = new TreeMap<>();
        for (final ItemStack stack : allStacks)
        {
            final ResourceLocation resourceLocation = stack.getItemHolder().unwrapKey().get().location();
            final Set<String> keys = new HashSet<>();
            for (final TypedDataComponent<?> key : stack.getComponents())
            {
                keys.add(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(key.type()).toString());
            }

            if (stack.getItem() instanceof ArmorItem)
            {
                keys.add("minecraft:dyed_color");
            }
            if (stack.isEnchantable())
            {
                keys.add("minecraft:enchantments");
            }
            if (stack.isRepairable())
            {
                keys.add("minecraft:repair_cost");
            }
            // We ignore damage in nbt.
            keys.remove("minecraft:damage");


            if (keyMapping.containsKey(resourceLocation.toString()))
            {
                final Set<String> list = keyMapping.get(resourceLocation.toString());
                list.addAll(keys);
                keyMapping.put(resourceLocation.toString(), list);
            }
            else
            {
                keyMapping.put(resourceLocation.toString(), keys);
            }
        }

        final Path path = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "compatibility").file(new ResourceLocation(MOD_ID, "itemnbtmatching"), "json");
        final JsonArray jsonArray = new JsonArray();
        for (final Map.Entry<String, Set<String>> entry : keyMapping.entrySet())
        {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("item", entry.getKey());

            if (!entry.getValue().isEmpty())
            {
                final JsonArray subArray = new JsonArray();
                entry.getValue().forEach(subArray::add);
                jsonObject.add("checkednbtkeys", subArray);
            }

            jsonArray.add(jsonObject);
        }

        return DataProvider.saveStable(cache, jsonArray, path);
    }
}
