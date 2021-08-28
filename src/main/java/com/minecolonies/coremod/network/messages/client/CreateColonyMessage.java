package com.minecolonies.coremod.network.messages.client;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN;
import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN;

/**
 * Message for trying to create a new colony.
 */
public class CreateColonyMessage implements IMessage
{
    /**
     * Townhall position to create building on
     */
    BlockPos townHall;

    public CreateColonyMessage()
    {
        super();
    }

    public CreateColonyMessage(final BlockPos townHall)
    {
        this.townHall = townHall;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(townHall);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        townHall = buf.readBlockPos();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer sender = ctxIn.getSender();
        final Level world = ctxIn.getSender().level;

        if (sender == null)
        {
            return;
        }

        if (sender.getStats().getValue(Stats.ITEM_USED.get(ModItems.supplyChest)) <= 0 && !sender.isCreative())
        {
            LanguageHandler.sendPlayerMessage(sender, "com.minecolonies.coremod.supplyneed");
            return;
        }

        final IColony colony = IColonyManager.getInstance().getClosestColony(world, townHall);

        String style = Constants.DEFAULT_STYLE;
        final BlockEntity tileEntity = world.getBlockEntity(townHall);

        if (!(tileEntity instanceof TileEntityColonyBuilding))
        {
            LanguageHandler.sendPlayerMessage(sender, "com.minecolonies.coremod.gui.colony.create.notileentity");
            return;
        }

        if (!((AbstractTileEntityColonyBuilding) tileEntity).getStyle().isEmpty())
        {
            style = ((AbstractTileEntityColonyBuilding) tileEntity).getStyle();
        }

        if (MineColonies.getConfig().getServer().restrictColonyPlacement.get())
        {
            final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(townHall, ((ServerLevel) world).getSharedSpawnPos()));
            if (spawnDistance < MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get())
            {
                if (!world.isClientSide)
                {
                    LanguageHandler.sendPlayerMessage(sender, CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN, MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get());
                }
                return;
            }
            else if (spawnDistance > MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get())
            {
                if (!world.isClientSide)
                {
                    LanguageHandler.sendPlayerMessage(sender, CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN, MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get());
                }
                return;
            }
        }

        if (colony != null && !IColonyManager.getInstance().isFarEnoughFromColonies(world, townHall))
        {
            LanguageHandler.sendPlayerMessage(sender, "com.minecolonies.coremod.gui.colony.denied.tooclose", colony.getName());
            return;
        }

            final IColony ownedColony = IColonyManager.getInstance().getIColonyByOwner(world, sender);

            if (ownedColony == null)
            {
                IColonyManager.getInstance().createColony(world, townHall, sender, style);
                IColonyManager.getInstance().getIColonyByOwner(world, sender).getBuildingManager().addNewBuilding((TileEntityColonyBuilding) tileEntity, world);
                LanguageHandler.sendPlayerMessage((Player) sender, "com.minecolonies.coremod.progress.colony_founded");
                return;
            }

        LanguageHandler.sendPlayerMessage((Player) sender, "com.minecolonies.coremod.gui.colony.create.failed");
    }
}
