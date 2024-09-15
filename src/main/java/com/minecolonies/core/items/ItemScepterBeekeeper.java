package com.minecolonies.core.items;

import com.minecolonies.api.items.IBlockOverlayItem;
import com.minecolonies.api.items.component.BuildingId;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBeekeeper;
import com.minecolonies.core.network.messages.client.colony.ColonyViewBuildingViewMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
public class ItemScepterBeekeeper extends Item implements IBlockOverlayItem
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
        super(properties.stacksTo(1));
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

        if (!(BuildingId.readBuildingFromItemStack(scepter) instanceof final BuildingBeekeeper building))
        {
            return InteractionResult.FAIL;
        }

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
        if (BuildingId.readBuildingViewFromItemStack(stack) instanceof final BuildingBeekeeper.View hut)
        {
            final List<OverlayBox> overlays = new ArrayList<>();

            overlays.add(new OverlayBox(hut.getPosition(), RED_OVERLAY, 0.02f, true));

            for (final BlockPos hive : hut.getHives())
            {
                overlays.add(new OverlayBox(hive, YELLOW_OVERLAY, 0.04f, true));
            }

            return overlays;
        }

        return Collections.emptyList();
    }
}
