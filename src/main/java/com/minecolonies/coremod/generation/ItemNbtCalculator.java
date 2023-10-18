package com.minecolonies.coremod.generation;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.minecolonies.api.items.CheckedNbtKey;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.Log;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
        final CreativeModeTab.ItemDisplayParameters tempDisplayParams = new CreativeModeTab.ItemDisplayParameters(FeatureFlags.REGISTRY.allFlags(), true, provider);

        final ImmutableList.Builder<ItemStack> listBuilder = new ImmutableList.Builder<>();

        for (final CreativeModeTab tab : CreativeModeTabs.allTabs())
        {
            if (tab.getType() == CreativeModeTab.Type.CATEGORY)
            {
                if (tab.getDisplayItems().isEmpty())
                {
                    try
                    {
                        tab.buildContents(tempDisplayParams);
                    }
                    catch (final Throwable ex)
                    {
                        Log.getLogger().warn("Error populating items for " + tab.getDisplayName().getString(), ex);
                    }
                }

                for (final ItemStack item : tab.getDisplayItems())
                {
                    if (item.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof IMateriallyTexturedBlock texturedBlock)
                    {
                        final CompoundTag tag = item.hasTag() ? item.getTag() : new CompoundTag();
                        final CompoundTag textureData = new CompoundTag();
                        for (final IMateriallyTexturedBlockComponent key : texturedBlock.getComponents())
                        {
                            textureData.putString(key.getId().toString(), key.getDefault().builtInRegistryHolder().key().location().toString());
                        }
                        tag.put("textureData", textureData);
                        final ItemStack copy = item.copy();
                        copy.setTag(tag);
                        listBuilder.add(copy);
                    }
                    else
                    {
                        listBuilder.add(item);
                    }
                }
            }
        }

        allStacks = listBuilder.build();

        final TreeMap<String, Set<CheckedNbtKey>> keyMapping = new TreeMap<>();
        for (final ItemStack stack : allStacks)
        {
            final ResourceLocation resourceLocation = stack.getItemHolder().unwrapKey().get().location();
            final CompoundTag tag = (stack.hasTag() && !stack.is(ModTags.ignoreNBT)) ? stack.getTag() : new CompoundTag();
            final Set<String> keys = tag.isEmpty() ? new HashSet<>() : new HashSet<>(tag.getAllKeys());

            if (stack.isEnchantable())
            {
                keys.add("Enchantments");
            }
            if (stack.isRepairable())
            {
                keys.add("RepairCost");
            }
            // We ignore damage in nbt.
            keys.remove("Damage");

            final Set<CheckedNbtKey> keyObjectList = new HashSet<>();
            for (String key : keys)
            {
                keyObjectList.add(createKeyFromNbt(key, tag));
            }

            if (keyMapping.containsKey(resourceLocation.toString()))
            {
                final Set<CheckedNbtKey> list = keyMapping.get(resourceLocation.toString());
                list.addAll(keyObjectList);
                keyMapping.put(resourceLocation.toString(), list);
            }
            else
            {
                keyMapping.put(resourceLocation.toString(), keyObjectList);
            }
        }

        final Path path = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "compatibility").file(new ResourceLocation(MOD_ID, "itemnbtmatching"), "json");
        final JsonArray jsonArray = new JsonArray();
        for (final Map.Entry<String, Set<CheckedNbtKey>> entry : keyMapping.entrySet())
        {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("item", entry.getKey());

            if (!entry.getValue().isEmpty())
            {
                final JsonArray subArray = new JsonArray();
                entry.getValue().forEach(key -> subArray.add(serializeKeyToJson(key)));
                jsonObject.add("checkednbtkeys", subArray);
            }

            jsonArray.add(jsonObject);
        }

        return DataProvider.saveStable(cache, jsonArray, path);
    }

    /**
     * Serialize a checked nbt key to json.
     * @param keyObject the key object to serialize.
     * @return the output json.
     */
    public static JsonObject serializeKeyToJson(final CheckedNbtKey keyObject)
    {
        final JsonObject obj = new JsonObject();
        obj.addProperty("key", keyObject.key);

        if (!keyObject.children.isEmpty())
        {
            final JsonArray jsonArray = new JsonArray();
            keyObject.children.forEach(child -> jsonArray.add(serializeKeyToJson(child)));
            obj.add("children", jsonArray);
        }
        return obj;
    }

    /**
     * Create a checked nbt key from nbt.
     * @param key the key to retrieve.
     * @param tag the tag to deserialize it from.
     * @return a new checked nbt key.
     */
    public static CheckedNbtKey createKeyFromNbt(final String key, final CompoundTag tag)
    {
        if (tag.get(key) instanceof CompoundTag)
        {
            final CompoundTag subTag = tag.getCompound(key);
            return new CheckedNbtKey(key, subTag.getAllKeys().stream().map(subKey -> createKeyFromNbt(subKey, subTag)).collect(Collectors.toSet()));
        }
        else
        {
            return new CheckedNbtKey(key, Collections.emptySet());
        }
    }

    /**
     * Create a checked nbt key from json.
     * @param jsonObject the object to serialize it from.
     * @return the output key.
     */
    public static CheckedNbtKey deserializeKeyFromJson(final JsonObject jsonObject)
    {
        final String key = jsonObject.get("key").getAsString();
        if (jsonObject.has("children"))
        {
            final Set<CheckedNbtKey> children = new HashSet<>();
            jsonObject.getAsJsonArray("children").forEach(child -> children.add(deserializeKeyFromJson(child.getAsJsonObject())));
            return new CheckedNbtKey(key, children);
        }
        else
        {
            return new CheckedNbtKey(key, Collections.emptySet());
        }
    }
}
