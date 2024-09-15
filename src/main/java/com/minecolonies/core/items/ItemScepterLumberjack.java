package com.minecolonies.core.items;

import com.ldtteam.structurize.component.ModDataComponents;
import com.ldtteam.structurize.items.AbstractItemWithPosSelector.PosSelection;
import com.minecolonies.api.items.IBlockOverlayItem;
import com.minecolonies.api.items.component.BuildingId;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.core.entity.ai.workers.production.EntityAIWorkLumberjack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Lumberjack Scepter Item class. Used to give tasks to Lumberjacks.
 */
public class ItemScepterLumberjack extends Item implements IBlockOverlayItem
{
    private static final int RED_OVERLAY = 0xFFFF0000;
    private static final int GREEN_OVERLAY = 0xFF00FF00;

    /**
     * LumberjackScepter constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemScepterLumberjack(final Properties properties)
    {
        super(properties.stacksTo(1).component(ModDataComponents.POS_SELECTION, PosSelection.EMPTY));
    }

    @NotNull
    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        if (context.getLevel().isClientSide)
        {
            return InteractionResult.FAIL;
        }

        final ItemStack scepter = context.getPlayer().getItemInHand(context.getHand());
        MessageUtils.format(TOOL_LUMBERJACK_SCEPTER_POSITION_B_SET).sendTo(context.getPlayer());

        PosSelection.updateItemStack(scepter, selection -> selection.setStartPos(context.getClickedPos()));
        storeRestrictedArea(context.getPlayer(), scepter, context.getLevel());
        return InteractionResult.FAIL;
    }

    @Override
    public boolean canAttackBlock(@NotNull final BlockState state, @NotNull final Level world, @NotNull final BlockPos pos, @NotNull final Player player)
    {
        if (!world.isClientSide)
        {
            final ItemStack scepter = player.getMainHandItem();
            MessageUtils.format(TOOL_LUMBERJACK_SCEPTER_POSITION_A_SET).sendTo(player);
            PosSelection.updateItemStack(scepter, selection -> selection.setStartPos(pos));
            storeRestrictedArea(player, scepter, world);
        }

        return false;
    }

    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 3.4028235E38F;
    }

    private void storeRestrictedArea(final Player player, final ItemStack scepter, final Level worldIn)
    {
        final PosSelection component = PosSelection.readFromItemStack(scepter);
        final Tuple<BlockPos, BlockPos> box = getBox(worldIn, scepter, component);

        if (box == null)
        {
            return;
        }
        assert box.getA() != null && box.getB() != null;

        // Check restricted area isn't too large
        final int minX = Math.min(box.getA().getX(), box.getB().getX());
        final int minY = Math.min(box.getA().getY(), box.getB().getY());
        final int minZ = Math.min(box.getA().getZ(), box.getB().getZ());
        final int maxX = Math.max(box.getA().getX(), box.getB().getX());
        final int maxY = Math.max(box.getA().getY(), box.getB().getY());
        final int maxZ = Math.max(box.getA().getZ(), box.getB().getZ());

        final int distX = maxX - minX;
        final int distY = maxY - minY;
        final int distZ = maxZ - minZ;

        final int volume = distX * distY * distZ;
        final int maxVolume = (int) Math.floor(2 * Math.pow(EntityAIWorkLumberjack.SEARCH_RANGE, 3));

        if (volume > maxVolume)
        {
            MessageUtils.format(TOOL_LUMBERJACK_SCEPTER_AREA_TOO_BIG, volume, maxVolume).sendTo(player);
            return;
        }

        if (!(BuildingId.readBuildingFromItemStack(scepter) instanceof final BuildingLumberjack hut))
        {
            return;
        }

        MessageUtils.format(TOOL_LUMBERJACK_SCEPTER_AREA_SET, minX, maxX, minY, maxY, minZ, maxZ, volume, maxVolume).sendTo(player);
        hut.setRestrictedArea(box.getA(), box.getB());
    }

    @NotNull
    @Override
    public List<OverlayBox> getOverlayBoxes(@NotNull final Level world, @NotNull final Player player, @NotNull ItemStack stack)
    {
        final PosSelection component = PosSelection.readFromItemStack(stack);
        final BuildingId buildingId = BuildingId.readFromItemStack(stack);
        final Tuple<BlockPos, BlockPos> box = getBox(world, stack, component);

        if (buildingId.hasId())
        {
            final OverlayBox anchorBox = new OverlayBox(buildingId.id(), RED_OVERLAY, 0.02f, true);

            if (box != null && box.getA() != null && box.getB() != null)
            {
                final AABB bounds = AABB.encapsulatingFullBlocks(box.getA(), box.getB().offset(1, 1, 1)).inflate(1);
                // inflate(1) is due to implementation of BlockPosUtil.isInArea

                return List.of(anchorBox, new OverlayBox(bounds, GREEN_OVERLAY, 0.02f, true));
            }

            return Collections.singletonList(anchorBox);
        }

        return Collections.emptyList();
    }

    @Nullable
    private Tuple<BlockPos, BlockPos> getBox(@NotNull final Level world, final ItemStack stack, final PosSelection selection)
    {
        final BlockPos start = selection.startPos().orElse(null);
        final BlockPos end = selection.endPos().orElse(null);

        if (world.isClientSide())
        {
            return new Tuple<>(start, end);
        }

        if (BuildingId.readBuildingFromItemStack(stack) instanceof final BuildingLumberjack hut)
        {
            final BlockPos startRestriction = start != null ? start : Objects.requireNonNullElse(hut.getStartRestriction(), BlockPos.ZERO);
            final BlockPos endRestriction = end != null ? end : Objects.requireNonNullElse(hut.getEndRestriction(), BlockPos.ZERO);
            if (!startRestriction.equals(BlockPos.ZERO) && !endRestriction.equals(BlockPos.ZERO))
            {
                return new Tuple<>(startRestriction, endRestriction);
            }
        }

        return null;
    }
}
