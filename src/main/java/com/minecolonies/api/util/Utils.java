package com.minecolonies.api.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General purpose utilities class. todo: split up into logically distinct parts
 */
public final class Utils
{
    private static final NavigableMap<Long, String> suffixes = new TreeMap<> ();
    
    static
    {
    suffixes.put(1_000L, "k");
    suffixes.put(1_000_000L, "M");
    suffixes.put(1_000_000_000L, "G");
    suffixes.put(1_000_000_000_000L, "T");
    suffixes.put(1_000_000_000_000_000L, "P");
    suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    /**
     * Private constructor to hide the implicit public one.
     */
    private Utils()
    {
    }

    /**
     * Searches a block in a custom range.
     *
     * @param world World instance.
     * @param block searched Block.
     * @param posX  X-coordinate.
     * @param posY  Y-coordinate.
     * @param posZ  Z-coordinate.
     * @param range the range to check around the point.
     * @return true if he found the block.
     */
    public static boolean isBlockInRange(@NotNull final Level world, final Block block, final int posX, final int posY, final int posZ, final int range)
    {
        for (int x = posX - range; x < posX + range; x++)
        {
            for (int z = posZ - range; z < posZ + range; z++)
            {
                for (int y = posY - range; y < posY + range; y++)
                {
                    if (Objects.equals(world.getBlockState(new BlockPos(x, y, z)).getBlock(), block))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Split path util.
     * @param path the path string.
     * @return the array.
     */
    public static String[] splitPath(final String path)
    {
        if (path.contains("\\"))
        {
            return path.split("\\\\");
        }
        return path.split("/");
    }

    /**
     * Resolve a path with a sub path.
     * @param path the path.
     * @param subPath the sub path.
     * @return the appended path.
     */
    public static Path resolvePath(final Path path, final String subPath)
    {
        Path resultPath = path;
        for (final String sub : splitPath(subPath))
        {
            resultPath = resultPath.resolve(sub);
        }
        return resultPath;
    }

    /**
     * Checks if the flag is set in the data. E.G. - Flag: 000101. - Data: 100101. - All Flags are set in data, so returns true. Some more flags are set, but not take into account
     *
     * @param data Data to check flag in.
     * @param flag Flag to check whether it is set or not.
     * @return True if flag is set, otherwise false.
     */
    public static boolean testFlag(final long data, final long flag)
    {
        return mask(data, flag) == flag;
    }

    /**
     * Returns what flags are set, and given in mask. E.G. - Flag: 000101. - Mask: 100101. - The 4th and 6th bit are set, so only those will be returned.
     *
     * @param data Data to check.
     * @param mask Mask to check.
     * @return Byte in which both data bits and mask bits are set.
     */
    public static long mask(final long data, final long mask)
    {
        return data & mask;
    }

    /**
     * Sets a flag in in the data. E.G. - Flag: 000101 - Mask: 100001 - The 4th bit will now be set, both the 1st and 6th bit are maintained.
     *
     * @param data Data to set flag in.
     * @param flag Flag to set.
     * @return Data with flags set.
     */
    public static long setFlag(final long data, final long flag)
    {
        return data | flag;
    }

    /**
     * Unsets a flag. E.G. - Flag: 000101 - Mask: 100101 - The 4th and 6th bit will be unset, the 1st bit is maintained.
     *
     * @param data Data to remove flag from.
     * @param flag Flag to remove.
     * @return Data with flag unset.
     */
    public static long unsetFlag(final long data, final long flag)
    {
        return data & ~flag;
    }

    /**
     * Toggles flags. E.G. - Flag: 000101 - Mask: 100101 - The 4th and 6th will be toggled, the 1st bit is maintained.
     *
     * @param data Data to toggle flag in.
     * @param flag Flag to toggle.
     * @return Data with flag toggled.
     */
    public static long toggleFlag(final long data, final long flag)
    {
        return data ^ flag;
    }

    /**
     * Checks if directory exists, else creates it.
     *
     * @param directory the directory to check.
     */
    public static void checkDirectory(@NotNull final File directory)
    {
        if (!directory.exists() && !directory.mkdirs())
        {
            Log.getLogger().error("Directory doesn't exist and failed to be created: " + directory.toString());
        }
    }
    
    /**
     * Formats a long value into a abbreviated string, ie: 1000 to 1k, 1200 to 1.2k, 13000 to 13k
     * @param value to format
     * @return string version of the value
     */
    public static String format(long value)
    {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    /**
     * Get the level of this blueprint from the name
     * @param schematicName the name of the blueprint.
     * @return the level or -1 if it doesn't have one.
     */
    public static int getBlueprintLevel(final String schematicName)
    {
        Matcher matcher = Pattern.compile("[0-9]$").matcher(schematicName.replace(".blueprint", ""));
        if (matcher.find())
        {
            final String string = matcher.group();
            if (!string.isEmpty())
            {
                try
                {
                    return Integer.parseInt(string);
                }
                catch (final NumberFormatException ex)
                {
                    // noop
                }
            }
        }

        return -1;
    }

    public static <T extends Object> Holder<T> getRegistryValue(final ResourceKey<T> resourceKey, final Level level)
    {
        return level.holderOrThrow(resourceKey);
    }

    public static <T extends Object> Tag serializeCodecMess(final Codec<T> codec, HolderLookup.Provider provider, final T obj)
    {
        return codec.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), obj).getOrThrow();
    }

    public static <T extends Object> T deserializeCodecMess(final Codec<T> codec, HolderLookup.Provider provider, final Tag tag)
    {
        return codec.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).resultOrPartial((res) -> {
            Log.getLogger().error("Failed to parse thing: '{}'", res);
        }).get();
    }

    public static <T extends Object> JsonElement serializeCodecMessToJson(final Codec<T> codec, HolderLookup.Provider provider, final T obj)
    {
        return codec.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), obj).getOrThrow();
    }

    public static <T extends Object> T deserializeCodecMessFromJson(final Codec<T> codec, HolderLookup.Provider provider, final JsonElement tag)
    {
        return codec.parse(provider.createSerializationContext(JsonOps.INSTANCE), tag).resultOrPartial((res) -> {
            Log.getLogger().error("Failed to parse thing: '{}'", res);
        }).get();
    }

    public static <T extends Object> void serializeCodecMess(final StreamCodec<RegistryFriendlyByteBuf, T> codec, RegistryFriendlyByteBuf buf, final T obj)
    {
        codec.encode(buf, obj);
    }

    public static <T extends Object> T deserializeCodecMess(final StreamCodec<RegistryFriendlyByteBuf, T> codec, final RegistryFriendlyByteBuf buf)
    {
        return codec.decode(buf);
    }

    public static void serializeCodecMess(RegistryFriendlyByteBuf buf, final ItemStack obj)
    {
        ItemStack.STREAM_CODEC.encode(buf, obj);
    }

    public static ItemStack deserializeCodecMess(final RegistryFriendlyByteBuf buf)
    {
        return ItemStack.STREAM_CODEC.decode(buf);
    }
}
