package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Loads and listens to free block data.
 */
public class FreeBlocksListener extends SimpleJsonResourceReloadListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Json constants
     */
    private static final String KEY_TYPE      = "type";
    private static final String TYPE_BLOCK    = "block";
    private static final String TYPE_POSITION = "position";
    private static final String KEY_BLOCK     = "block";
    private static final String KEY_POSITION  = "position";

    /**
     * The current list of free to interact blocks.
     */
    private static List<Block> FREE_BLOCKS = new ArrayList<>();

    /**
     * The current list of free to interact positions.
     */
    private static List<BlockPos> FREE_POSITIONS = new ArrayList<>();

    /**
     * Default constructor.
     */
    public FreeBlocksListener()
    {
        super(GSON, "free_blocks");
    }

    /**
     * Check if the block is configured to bypass the colony restrictions.
     *
     * @param block the block to check.
     * @return true if so.
     */
    public static boolean isFreeBlock(final Block block)
    {
        return FREE_BLOCKS.contains(block);
    }

    /**
     * Check if the position is configured to bypass the colony restrictions.
     *
     * @param block the position to check.
     * @return true if so.
     */
    public static boolean isFreePos(final BlockPos block)
    {
        return FREE_POSITIONS.contains(block);
    }

    @Override
    protected void apply(
      final @NotNull Map<ResourceLocation, JsonElement> jsonElementMap,
      final @NotNull ResourceManager resourceManager,
      final @NotNull ProfilerFiller profiler)
    {
        final List<Block> newBlocks = new ArrayList<>();
        final List<BlockPos> newPositions = new ArrayList<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            if (!entry.getValue().isJsonObject())
            {
                return;
            }

            final JsonObject object = entry.getValue().getAsJsonObject();

            final String type = GsonHelper.getAsString(object, KEY_TYPE);
            switch (type)
            {
                case TYPE_BLOCK:
                    newBlocks.add(BuiltInRegistries.BLOCK.get(new ResourceLocation(GsonHelper.getAsString(object, KEY_BLOCK))));
                    break;
                case TYPE_POSITION:
                    newPositions.add(BlockPosUtil.getBlockPosOfString(GsonHelper.getAsString(object, KEY_POSITION)));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown free block type: " + type);
            }
        }
        FREE_BLOCKS = Collections.unmodifiableList(newBlocks);
        FREE_POSITIONS = Collections.unmodifiableList(newPositions);
    }
}
