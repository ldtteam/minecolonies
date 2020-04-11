package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.CreateColonyMessage;
import com.minecolonies.coremod.network.messages.VanillaParticleMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN;
import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN;
import static com.minecolonies.api.util.constant.WindowConstants.TOWNHALL_COLONY_MANAGEMENT_GUI;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

public class WindowTownHallColonyManage extends AbstractWindowSkeleton
{

    private static final String BUTTON_CLOSE  = "cancel";
    private static final String BUTTON_DELETE = "delete";
    private static final String BUTTON_CREATE = "create";

    private final BlockPos pos;

    /**
     * Constructor for the skeleton class of the windows.
     */
    public WindowTownHallColonyManage(final PlayerEntity player, final BlockPos pos, final World world)
    {
        super(MOD_ID + TOWNHALL_COLONY_MANAGEMENT_GUI);

        this.pos = pos;

        final IColony existingColony = IColonyManager.getInstance().getIColony(world, pos);

        if (existingColony != null)
        {
            // Colony here
            findPaneOfTypeByID("nearbycolony", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.colony.here",
              existingColony.getName(),
              existingColony.getPermissions().getOwnerName()));
        }
        else
        {
            // Close colony
            int closeClonyID = findNextNearbyColony(world, pos, MineColonies.getConfig().getCommon().minTownHallPadding.get());

            if (closeClonyID != 0)
            {
                findPaneOfTypeByID("nearbycolony", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.colony.near", closeClonyID));
            }
            else
            {
                // No close colony
                findPaneOfTypeByID("nearbycolony", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.colony.nonenearby"));
            }
        }

        final IColony ownerColony = IColonyManager.getInstance().getIColonyByOwner(world, player);
        if (ownerColony != null)
        {
            findPaneOfTypeByID("delete", ButtonImage.class).enable();
            findPaneOfTypeByID("owncolony", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.colony.own", ownerColony.getCenter()));

            if (MineColonies.getConfig().getCommon().allowInfiniteColonies.get())
            {
                findPaneOfTypeByID("creationpossible", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.colony.denied.existingandabandon"));
            }
            else
            {
                findPaneOfTypeByID("creationpossible", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.colony.denied.existing"));
            }
        }
        else
        {
            findPaneOfTypeByID("owncolony", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.colony.none"));

            if (existingColony != null || IColonyManager.getInstance().isTooCloseToColony(world, pos))
            {
                findPaneOfTypeByID("creationpossible", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.colony.denied.tooclose"));
            }
        }

        if (MineColonies.getConfig().getCommon().restrictColonyPlacement.get())
        {
            final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(pos, world.getSpawnPoint()));
            if (spawnDistance < MineColonies.getConfig().getCommon().minDistanceFromWorldSpawn.get())
            {
                findPaneOfTypeByID("creationpossible", Text.class).setTextContent(LanguageHandler.format(CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN,
                  MineColonies.getConfig().getCommon().minDistanceFromWorldSpawn.get()));
            }
            else if (spawnDistance > MineColonies.getConfig().getCommon().maxDistanceFromWorldSpawn.get())
            {
                findPaneOfTypeByID("creationpossible", Text.class).setTextContent(LanguageHandler.format(CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN,
                  MineColonies.getConfig().getCommon().maxDistanceFromWorldSpawn.get()));
            }
        }

        if (findPaneOfTypeByID("creationpossible", Text.class).getTextContent().isEmpty())
        {
            findPaneOfTypeByID("create", ButtonImage.class).enable();
            findPaneOfTypeByID("creationpossible", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.colony.allowed.create"));
        }
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        switch (button.getID())
        {
            case BUTTON_CLOSE:
            {
                close();
                return;
            }
            case BUTTON_CREATE:
            {
                new VanillaParticleMessage(pos.getX(), pos.getY(), pos.getZ(), ParticleTypes.DRAGON_BREATH).onExecute(null, false);
                Minecraft.getInstance().world.playSound(Minecraft.getInstance().player, Minecraft.getInstance().player.getPosition(),
                  SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.AMBIENT, 2.5f, 0.8f);
                Network.getNetwork().sendToServer(new CreateColonyMessage(pos));
                close();

                return;
            }
            case BUTTON_DELETE:
            {
                new WindowTownHallColonyDelete().open();
            }
            default:
        }
    }

    /**
     * Finds the first nearby colony in the range
     *
     * @param world world to use
     * @param start start position
     * @param range search range
     * @return
     */
    private static int findNextNearbyColony(final World world, final BlockPos start, final int range)
    {
        int startX = start.getX() >> 4;
        int startZ = start.getZ() >> 4;

        for (int x = -range; x <= range; x++)
        {
            for (int z = -range; z <= range; z++)
            {
                final int chunkX = startX + x;
                final int chunkZ = startZ + z;
                final Chunk chunk = world.getChunk(chunkX, chunkZ);
                final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
                if (cap != null)
                {
                    if (cap.getOwningColony() != 0)
                    {
                        return cap.getOwningColony();
                    }

                    if (!cap.getAllCloseColonies().isEmpty())
                    {
                        return cap.getAllCloseColonies().get(0);
                    }
                }
            }
        }

        return 0;
    }
}
