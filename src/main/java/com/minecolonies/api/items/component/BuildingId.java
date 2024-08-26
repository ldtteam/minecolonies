package com.minecolonies.api.items.component;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.BlockPosUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.function.UnaryOperator;

/**
 * Saves reference to hut
 */
public record BuildingId(BlockPos id)
{
    public static final BuildingId EMPTY = new BuildingId(BlockPosUtil.SAFE_ZERO);

    public static final Codec<BuildingId> CODEC = RecordCodecBuilder
        .create(builder -> builder.group(BlockPos.CODEC.fieldOf("id").forGetter(BuildingId::id)).apply(builder, BuildingId::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuildingId> STREAM_CODEC =
        StreamCodec.composite(BlockPos.STREAM_CODEC, BuildingId::id, BuildingId::new);

    public boolean hasId()
    {
        return !id.equals(EMPTY.id);
    }

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.HUT_ID_COMPONENT, this);
    }

    public static BuildingId readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.HUT_ID_COMPONENT, BuildingId.EMPTY);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<BuildingId> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }

    @Nullable
    public static IBuilding readBuildingFromItemStack(final ItemStack itemStack)
    {
        final IColony colony = ColonyId.readColonyFromItemStack(itemStack);
        final BuildingId buildingId = readFromItemStack(itemStack);
        return colony == null || buildingId == EMPTY ? null : colony.getBuildingManager().getBuilding(buildingId.id);
    }

    @Nullable
    public static IBuildingView readBuildingViewFromItemStack(final ItemStack itemStack)
    {
        final IColonyView colony = ColonyId.readColonyViewFromItemStack(itemStack);
        final BuildingId buildingId = readFromItemStack(itemStack);
        return colony == null || buildingId == EMPTY ? null : colony.getBuilding(buildingId.id);
    }
}
