package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.items.IBlockOverlayItem;
import com.minecolonies.api.items.ModDataComponents;
import com.minecolonies.api.items.ModDataComponents.ColonyId;
import com.minecolonies.api.items.ModDataComponents.Pos;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBeekeeper;
import com.minecolonies.core.network.messages.client.colony.ColonyViewBuildingViewMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Beekeeper Scepter Item class. Used to give tasks to Beekeeper.
 */
public class ItemScepterBeekeeper extends AbstractItemMinecolonies implements IBlockOverlayItem
{
    private static final int RED_OVERLAY = 0xFFFF0000;
    private static final int YELLOW_OVERLAY = 0xFFFFFF00;

    /**
     * BeekeeperScepter constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemScepterBeekeeper(final Properties properties)
    {
        super("scepterbeekeeper", properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(final UseOnContext useContext)
    {
        // if server world, do nothing
        if (useContext.getLevel().isClientSide)
        {
            return InteractionResult.FAIL;
        }

        final Player player = useContext.getPlayer();

        final ItemStack scepter = useContext.getPlayer().getItemInHand(useContext.getHand());
        final IColony colony = ColonyId.readColonyFromItemStack(scepter);
        final ModDataComponents.Pos posComponent = Pos.readFromItemStack(scepter);

        if (colony == null || posComponent == null)
        {
            return InteractionResult.FAIL;
        }

        final BlockPos hutPos = posComponent.pos();

        final IBuilding hut = colony.getBuildingManager().getBuilding(hutPos);
        final BuildingBeekeeper building = (BuildingBeekeeper) hut;

        if (useContext.getLevel().getBlockState(useContext.getClickedPos()).getBlock() instanceof BeehiveBlock)
        {
            final Collection<BlockPos> positions = building.getHives();

            final BlockPos pos = useContext.getClickedPos();
            if (positions.contains(pos))
            {
                MessageUtils.format(TOOL_BEEHIVE_SCEPTER_REMOVE_HIVE).sendTo(useContext.getPlayer());
                building.removeHive(pos);
                SoundUtils.playSoundForPlayer((ServerPlayer) player, SoundEvents.NOTE_BLOCK_BELL.value(), (float) SoundUtils.VOLUME * 2, 0.5f);
                new ColonyViewBuildingViewMessage(building).sendToPlayer((ServerPlayer) player);
            }
            else
            {
                if (positions.size() < building.getMaximumHives())
                {
                    MessageUtils.format(TOOL_BEEHIVE_SCEPTER_ADD_HIVE).sendTo(useContext.getPlayer());
                    building.addHive(pos);
                    SoundUtils.playSuccessSound(player, player.blockPosition());
                    new ColonyViewBuildingViewMessage(building).sendToPlayer((ServerPlayer) player);
                }
                if (positions.size() >= building.getMaximumHives())
                {
                    MessageUtils.format(TOOL_BEEHIVE_SCEPTER_MAX_HIVES).sendTo(useContext.getPlayer());
                    player.getInventory().removeItemNoUpdate(player.getInventory().selected);
                }
            }
        }
        else
        {
            player.getInventory().removeItemNoUpdate(player.getInventory().selected);
        }

        return super.useOn(useContext);
    }

    @NotNull
    @Override
    public List<OverlayBox> getOverlayBoxes(@NotNull final Level world, @NotNull final Player player, @NotNull ItemStack stack)
    {
        final IColonyView colony = ColonyId.readColonyViewFromItemStack(stack);
        final ModDataComponents.Pos posComponent = Pos.readFromItemStack(stack);

        if (colony == null || posComponent == null)
        {
            return Collections.emptyList();
        }

        final BlockPos pos = posComponent.pos();

        if (colony != null && colony.getBuilding(pos) instanceof final BuildingBeekeeper.View hut)
        {
            final List<OverlayBox> overlays = new ArrayList<>();

            overlays.add(new OverlayBox(pos, RED_OVERLAY, 0.02f, true));

            for (final BlockPos hive : hut.getHives())
            {
                overlays.add(new OverlayBox(hive, YELLOW_OVERLAY, 0.04f, true));
            }

            return overlays;
        }

        return Collections.emptyList();
    }
}
