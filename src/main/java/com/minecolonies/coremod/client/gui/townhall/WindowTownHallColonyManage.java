package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.structurize.client.gui.WindowSwitchPack;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractWindowSkeleton;
import com.minecolonies.coremod.network.messages.client.CreateColonyMessage;
import com.minecolonies.coremod.network.messages.client.VanillaParticleMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;
import static com.minecolonies.api.util.constant.BuildingConstants.DEACTIVATED;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * TownhallGUI for managing colony creation/deletion
 */
public class WindowTownHallColonyManage extends AbstractWindowSkeleton
{
    private static final String BUTTON_CLOSE  = "cancel";
    private static final String BUTTON_DELETE = "delete";
    private static final String BUTTON_CREATE = "create";
    private static final String TEXT_NEARBY   = "nearbycolony";
    private static final String TEXT_OWN      = "owncolony";
    private static final String TEXT_FEEDBACK = "creationpossible";
    private static final String TEXT_PACK     = "pack";

    /**
     * Townhall position
     */
    private BlockPos pos;

    public WindowTownHallColonyManage(final Player player, final BlockPos pos, final Level world)
    {
        super(MOD_ID + TOWNHALL_COLONY_MANAGEMENT_GUI);

        this.pos = pos;

        final IColony existingColony = IColonyManager.getInstance().getIColony(world, pos);

        if (existingColony != null)
        {
            // Colony here
            findPaneOfTypeByID(TEXT_NEARBY, Text.class).setText(Component.translatable(MESSAGE_COLONY_INSIDE,
              existingColony.getName(),
              existingColony.getPermissions().getOwnerName()));
        }
        else
        {
            // Close colony
            int closeColonyID = findNextNearbyColony(world, pos, MineColonies.getConfig().getServer().minColonyDistance.get());

            if (closeColonyID != 0)
            {
                findPaneOfTypeByID(TEXT_NEARBY, Text.class).setText(Component.translatable(MESSAGE_COLONY_NEARBY, closeColonyID));
            }
            else
            {
                // No close colony
                findPaneOfTypeByID(TEXT_NEARBY, Text.class).setText(Component.translatable(MESSAGE_COLONY_NO_NEARBY));
            }
        }

        final Text pack = findPaneOfTypeByID(TEXT_PACK, Text.class);
        final IColony ownerColony = IColonyManager.getInstance().getIColonyByOwner(world, player);
        if (ownerColony != null)
        {
            findPaneOfTypeByID(BUTTON_SWITCH, ButtonImage.class).hide();
            findPaneOfTypeByID(BUTTON_SWITCH, ButtonImage.class).disable();

            findPaneOfTypeByID(BUTTON_DELETE, ButtonImage.class).show();
            findPaneOfTypeByID(BUTTON_DELETE, ButtonImage.class).enable();
            pack.hide();
            findPaneOfTypeByID(TEXT_OWN, Text.class).setText(Component.translatable(MESSAGE_COLONY_OWN, ownerColony.getCenter()));

            if (MineColonies.getConfig().getServer().allowInfiniteColonies.get())
            {
                findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(Component.translatable(MESSAGE_COLONY_CREATE_DENIED_EXISTING_ABANDON));
            }
            else
            {
                findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(Component.translatable(MESSAGE_COLONY_CREATE_DENIED_EXISTING));
            }
        }
        else
        {
            findPaneOfTypeByID(TEXT_OWN, Text.class).setText(Component.translatable(MESSAGE_COLONY_NONE));
            pack.setText(Component.translatable(PACK_DESC, StructurePacks.selectedPack.getName()));

            if (existingColony != null || !IColonyManager.getInstance().isFarEnoughFromColonies(world, pos))
            {
                final IColony colony;
                if (existingColony != null)
                {
                    colony = existingColony;
                }
                else
                {
                    colony = IColonyManager.getInstance().getClosestColony(world, pos);
                }

                if (colony != null)
                {
                    findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(Component.translatable(MESSAGE_COLONY_CREATE_DENIED_TOO_CLOSE, colony.getName()));
                }
            }
        }

        if (MineColonies.getConfig().getServer().restrictColonyPlacement.get())
        {
            final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(pos, new BlockPos(world.getLevelData().getXSpawn(), world.getLevelData().getYSpawn(), world.getLevelData().getZSpawn())));
            if (spawnDistance < MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get())
            {
                findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(Component.translatable(CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN,
                  MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get()));
            }
            else if (spawnDistance > MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get())
            {
                findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(Component.translatable(CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN,
                  MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get()));
            }
        }

        if (findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).isTextEmpty())
        {
            findPaneOfTypeByID(BUTTON_CREATE, ButtonImage.class).enable();
            findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(Component.translatable(MESSAGE_COLONY_CREATE_ALLOWED));
        }

        registerButton(BUTTON_SWITCH, () -> new WindowSwitchPack(() -> new WindowTownHallColonyManage(player, pos, world)).open());
        registerButton(BUTTON_CLOSE, this::close);
        registerButton(BUTTON_CREATE, this::onCreate);
        registerButton(BUTTON_DELETE, () -> new WindowTownHallColonyDelete().open());
    }

    /**
     * On create button
     */
    public void onCreate()
    {
        final Player player = Minecraft.getInstance().player;
        final Component colonyName = Component.translatable(DEFAULT_COLONY_NAME, player.getName());

        new VanillaParticleMessage(pos.getX(), pos.getY(), pos.getZ(), ParticleTypes.DRAGON_BREATH).onExecute(null, false);
        Minecraft.getInstance().level.playSound(Minecraft.getInstance().player, new BlockPos(Minecraft.getInstance().player.position()),
          SoundEvents.CAMPFIRE_CRACKLE, SoundSource.AMBIENT, 2.5f, 0.8f);
        final boolean reactivate;
        final BlockEntity entity = Minecraft.getInstance().level.getBlockEntity(pos);
        if (entity instanceof final TileEntityColonyBuilding hut)
        {
            reactivate = hut.getPositionedTags().containsKey(BlockPos.ZERO) && hut.getPositionedTags().get(BlockPos.ZERO).contains(DEACTIVATED);
        }
        else
        {
            reactivate = false;
        }

        if (reactivate)
        {
            Network.getNetwork().sendToServer(new CreateColonyMessage(pos, true, colonyName.getString(),"", ((TileEntityColonyBuilding) entity).getBlueprintPath()));
            close();
        }
        else if (entity instanceof TileEntityColonyBuilding && !((TileEntityColonyBuilding) entity).getPackName().isEmpty())
        {
            Network.getNetwork().sendToServer(new CreateColonyMessage(pos, false, colonyName.getString(), StructurePacks.selectedPack.getName(), ((TileEntityColonyBuilding) entity).getBlueprintPath()));
            close();
        }
    }

    /**
     * Finds the first nearby colony claim in the range
     *
     * @param world world to use
     * @param start start position
     * @param range search range
     * @return the id of the found colony
     */
    private static int findNextNearbyColony(final Level world, final BlockPos start, final int range)
    {
        int startX = start.getX() >> 4;
        int startZ = start.getZ() >> 4;

        for (int x = -range; x <= range; x++)
        {
            for (int z = -range; z <= range; z++)
            {
                final int chunkX = startX + x;
                final int chunkZ = startZ + z;
                final LevelChunk chunk = world.getChunk(chunkX, chunkZ);
                final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
                if (cap != null)
                {
                    if (cap.getOwningColony() != 0)
                    {
                        return cap.getOwningColony();
                    }

                    if (!cap.getStaticClaimColonies().isEmpty())
                    {
                        return cap.getStaticClaimColonies().get(0);
                    }
                }
            }
        }

        return 0;
    }
}
