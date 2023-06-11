package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.townhall.WindowTownHallColonyManage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.TranslationConstants.TOWNHALL_BREAKING_DONE_MESSAGE;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import java.util.ArrayList;
import java.util.List;

/**
 * Hut for the town hall. Sets the working range for the town hall in the constructor
 */
public class BlockHutTownHall extends AbstractBlockHut<BlockHutTownHall>
{
    public BlockHutTownHall()
    {
        super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(HARDNESS, RESISTANCE));
    }

    /**
     * Progress in % of breaking the townHall.
     */
    private int breakProgressOnTownHall = 0;

    /**
     * Ticks at which townhall breaking started.
     */
    private long lastTownHallBreakingTick = 0;

    /**
     * Detect if the town-hall break was valid.
     */
    private boolean validTownHallBreak = false;


    @Override
    public float getDestroyProgress(final BlockState state, @NotNull final Player player, @NotNull final BlockGetter blockReader, @NotNull final BlockPos pos)
    {
        if(MineColonies.getConfig().getServer().pvp_mode.get() && player.level instanceof ServerLevel)
        {
            final IBuilding building = IColonyManager.getInstance().getBuilding(player.level, pos);
            if (building != null && building.getColony().isCoordInColony(player.level, pos)
                  && building.getColony().getPermissions().getRank(player).isHostile())
            {
                final double localProgress = breakProgressOnTownHall;
                final double hardness = state.getDestroySpeed(player.level, pos) * 20.0 * 1.5;

                if (localProgress >= hardness / 10.0 * 9.0 && localProgress <= hardness / 10.0 * 9.0 + 1)
                {
                    MessageUtils.format(TOWNHALL_BREAKING_DONE_MESSAGE, player.getName(), 90).sendTo(building.getColony()).forAllPlayers();
                }
                if (localProgress >= hardness / 4.0 * 3.0 && localProgress <= hardness / 4.0 * 3.0 + 1)
                {
                    MessageUtils.format(TOWNHALL_BREAKING_DONE_MESSAGE, player.getName(), 75).sendTo(building.getColony()).forAllPlayers();
                }
                else if (localProgress >= hardness / 2.0 && localProgress <= hardness / 2.0 + 1)
                {
                    MessageUtils.format(TOWNHALL_BREAKING_DONE_MESSAGE, player.getName(), 50).sendTo(building.getColony()).forAllPlayers();
                }
                else if (localProgress >= hardness / 4.0 && localProgress <= hardness / 4.0 + 1)
                {
                    MessageUtils.format(TOWNHALL_BREAKING_DONE_MESSAGE, player.getName(), 25).sendTo(building.getColony()).forAllPlayers();
                }

                if (localProgress >= hardness - 1)
                {
                    validTownHallBreak = true;
                }

                if (player.level.getGameTime() - lastTownHallBreakingTick < 10)
                {
                    breakProgressOnTownHall++;
                }
                else
                {
                    MessageUtils.format(TOWNHALL_BREAKING_DONE_MESSAGE, player.getName(), 100).sendTo(building.getColony()).forAllPlayers();
                    breakProgressOnTownHall = 0;
                    validTownHallBreak = false;
                }
                lastTownHallBreakingTick = player.level.getGameTime();
            }
            else
            {
                validTownHallBreak = true;
            }
        }
        else if (!MineColonies.getConfig().getServer().pvp_mode.get())
        {
            validTownHallBreak = true;
        }
        final float def = super.getDestroyProgress(state, player, player.level, pos);
        return MineColonies.getConfig().getServer().pvp_mode.get() ? def / 12 : def;
    }

    @Override
    public List<MutableComponent> getRequirements(final ClientLevel level, final BlockPos pos, final LocalPlayer player)
    {
        final List<MutableComponent> requirements = new ArrayList<>();
        if (InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.getInventory()), this) == -1)
        {
            requirements.add(Component.translatable("com.minecolonies.coremod.hut.cost", Component.translatable("block." + Constants.MOD_ID + "." + getHutName())).setStyle((Style.EMPTY).withColor(
              ChatFormatting.RED)));
        }

        return requirements;
    }

    /**
     * Getter for the Block's state to breakable.
     * @return  True if the block is eligible for destruction
     */
    public boolean getValidBreak()
    {
        return validTownHallBreak;
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhuttownhall";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.townHall.get();
    }

    /**
     * Choose a different gui when no colony view, for colony overview and creation/deletion
     *
     * @param state   the blockstate.
     * @param worldIn the world.
     * @param pos     the position.
     * @param player  the player.
     * @param hand    the hand.
     * @param ray     the raytraceresult.
     * @return the result type.
     */
    @NotNull
    @Override
    public InteractionResult use(
      final BlockState state,
      final Level worldIn,
      final BlockPos pos,
      final Player player,
      final InteractionHand hand,
      final BlockHitResult ray)
    {
       /*
        If the world is client, open the gui of the building
         */
        if (worldIn.isClientSide)
        {
            @Nullable final IBuildingView building = IColonyManager.getInstance().getBuildingView(worldIn.dimension(), pos);

            if (building != null
                  && building.getColony() != null
                  && building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS))
            {
                building.openGui(player.isShiftKeyDown());
            }
            else
            {
                new WindowTownHallColonyManage(player, pos, worldIn).open();
            }
        }
        return InteractionResult.SUCCESS;
    }
}
