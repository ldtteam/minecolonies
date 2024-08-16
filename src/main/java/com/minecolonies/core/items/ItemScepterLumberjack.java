package com.minecolonies.core.items;

import com.ldtteam.structurize.component.ModDataComponents;
import com.ldtteam.structurize.items.AbstractItemWithPosSelector;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.items.IBlockOverlayItem;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.core.entity.ai.workers.production.EntityAIWorkLumberjack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import java.util.Optional;

import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Lumberjack Scepter Item class. Used to give tasks to Lumberjacks.
 */
public class ItemScepterLumberjack extends AbstractItemMinecolonies implements IBlockOverlayItem
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
        super("scepterlumberjack", properties.stacksTo(1));
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

        final AbstractItemWithPosSelector.PosSelection component = scepter.getOrDefault(ModDataComponents.POS_SELECTION, AbstractItemWithPosSelector.PosSelection.EMPTY);
        scepter.set(ModDataComponents.POS_SELECTION, new AbstractItemWithPosSelector.PosSelection(Optional.of(context.getClickedPos()), component.endPos()));
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
            final AbstractItemWithPosSelector.PosSelection component = scepter.getOrDefault(ModDataComponents.POS_SELECTION, AbstractItemWithPosSelector.PosSelection.EMPTY);
            scepter.set(ModDataComponents.POS_SELECTION, new AbstractItemWithPosSelector.PosSelection(component.startPos(), Optional.of(pos)));
            storeRestrictedArea(player, scepter, world);
        }

        return false;
    }

    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 3.4028235E38F;
    }

    private void storeRestrictedArea(final Player player, final ItemStack scepter, final Level worldIn)
    {
        final AbstractItemWithPosSelector.PosSelection component = scepter.getOrDefault(ModDataComponents.POS_SELECTION, AbstractItemWithPosSelector.PosSelection.EMPTY);
        final Box box = getBox(worldIn, scepter, component.startPos(), component.endPos());

        if (box.anchor() == null || box.corners() == null)
        {
            return;
        }
        assert box.corners().getA() != null && box.corners().getB() != null;

        // Check restricted area isn't too large
        final int minX = Math.min(box.corners().getA().getX(), box.corners().getB().getX());
        final int minY = Math.min(box.corners().getA().getY(), box.corners().getB().getY());
        final int minZ = Math.min(box.corners().getA().getZ(), box.corners().getB().getZ());
        final int maxX = Math.max(box.corners().getA().getX(), box.corners().getB().getX());
        final int maxY = Math.max(box.corners().getA().getY(), box.corners().getB().getY());
        final int maxZ = Math.max(box.corners().getA().getZ(), box.corners().getB().getZ());

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

        final com.minecolonies.api.items.ModDataComponents.ColonyId colonyId = scepter.get(com.minecolonies.api.items.ModDataComponents.COLONY_ID_COMPONENT);
        if (colonyId == null)
        {
            return;
        }
        MessageUtils.format(TOOL_LUMBERJACK_SCEPTER_AREA_SET, minX, maxX, minY, maxY, minZ, maxZ, volume, maxVolume).sendTo(player);

        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId.id(), colonyId.dimension());
        final BuildingLumberjack hut = colony.getBuildingManager().getBuilding(box.anchor(), BuildingLumberjack.class);
        if (hut == null)
        {
            return;
        }

        hut.setRestrictedArea(box.corners().getA(), box.corners().getB());
    }

    @NotNull
    @Override
    public List<OverlayBox> getOverlayBoxes(@NotNull final Level world, @NotNull final Player player, @NotNull ItemStack stack)
    {
        final AbstractItemWithPosSelector.PosSelection component = stack.getOrDefault(ModDataComponents.POS_SELECTION, AbstractItemWithPosSelector.PosSelection.EMPTY);
        final Box box = getBox(world, stack, component.startPos(), component.endPos());

        if (box.anchor() != null)
        {
            final OverlayBox anchorBox = new OverlayBox(box.anchor(), RED_OVERLAY, 0.02f, true);

            if (box.corners() != null)
            {
                assert box.corners().getA() != null && box.corners().getB() != null;
                final AABB bounds = AABB.encapsulatingFullBlocks(box.corners().getA(), box.corners().getB().offset(1, 1, 1)).inflate(1);
                // inflate(1) is due to implementation of BlockPosUtil.isInArea

                return List.of(anchorBox, new OverlayBox(bounds, GREEN_OVERLAY, 0.02f, true));
            }

            return Collections.singletonList(anchorBox);
        }

        return Collections.emptyList();
    }

    private record Box(@Nullable BlockPos anchor, @Nullable Tuple<BlockPos, BlockPos> corners) { }

    @NotNull
    private Box getBox(@NotNull final Level world, final ItemStack stack, final Optional<BlockPos> startPos, final Optional<BlockPos> endPos)
    {
        final com.minecolonies.api.items.ModDataComponents.ColonyId colonyId = stack.get(com.minecolonies.api.items.ModDataComponents.COLONY_ID_COMPONENT);
        if (colonyId == null)
        {
            return new Box(null, null);
        }
        final com.minecolonies.api.items.ModDataComponents.Pos posComponent = stack.get(com.minecolonies.api.items.ModDataComponents.POS_COMPONENT);
        final BlockPos start = startPos.orElse(null);
        final BlockPos end = endPos.orElse(null);

        if (world.isClientSide())
        {
            return new Box(posComponent.pos(), new Tuple<>(start, end));
        }

        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId.id(), colonyId.dimension());
        if (colony != null && colony.getBuildingManager().getBuilding(posComponent.pos()) instanceof final BuildingLumberjack hut)
        {
            final BlockPos startRestriction = start != null ? start : Objects.requireNonNullElse(hut.getStartRestriction(), BlockPos.ZERO);
            final BlockPos endRestriction = end != null ? end : Objects.requireNonNullElse(hut.getEndRestriction(), BlockPos.ZERO);
            if (!startRestriction.equals(BlockPos.ZERO) && !endRestriction.equals(BlockPos.ZERO))
            {
                return new Box(posComponent.pos(), new Tuple<>(startRestriction, endRestriction));
            }
            return new Box(posComponent.pos(), null);
        }

        return new Box(null, null);
    }
}
