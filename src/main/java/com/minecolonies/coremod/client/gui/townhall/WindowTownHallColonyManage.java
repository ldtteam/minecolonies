package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractWindowSkeleton;
import com.minecolonies.coremod.network.messages.client.CreateColonyMessage;
import com.minecolonies.coremod.network.messages.client.VanillaParticleMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN;
import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN;
import static com.minecolonies.api.util.constant.WindowConstants.TOWNHALL_COLONY_MANAGEMENT_GUI;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

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

    /**
     * Townhall position
     */
    private final BlockPos pos;

    public WindowTownHallColonyManage(final Player player, final BlockPos pos, final Level world)
    {
        super(MOD_ID + TOWNHALL_COLONY_MANAGEMENT_GUI);

        this.pos = pos;

        final IColony existingColony = IColonyManager.getInstance().getIColony(world, pos);

        if (existingColony != null)
        {
            // Colony here
            findPaneOfTypeByID(TEXT_NEARBY, Text.class).setText(new TranslatableComponent("com.minecolonies.coremod.gui.colony.here",
              existingColony.getName(),
              existingColony.getPermissions().getOwnerName()));
        }
        else
        {
            // Close colony
            int closeClonyID = findNextNearbyColony(world, pos, MineColonies.getConfig().getServer().minColonyDistance.get());

            if (closeClonyID != 0)
            {
                findPaneOfTypeByID(TEXT_NEARBY, Text.class).setText(new TranslatableComponent("com.minecolonies.coremod.gui.colony.near", closeClonyID));
            }
            else
            {
                // No close colony
                findPaneOfTypeByID(TEXT_NEARBY, Text.class).setText(new TranslatableComponent("com.minecolonies.coremod.gui.colony.nonenearby"));
            }
        }

        final IColony ownerColony = IColonyManager.getInstance().getIColonyByOwner(world, player);
        if (ownerColony != null)
        {
            findPaneOfTypeByID(BUTTON_DELETE, ButtonImage.class).enable();
            findPaneOfTypeByID(TEXT_OWN, Text.class).setText(new TranslatableComponent("com.minecolonies.coremod.gui.colony.own", ownerColony.getCenter()));

            if (MineColonies.getConfig().getServer().allowInfiniteColonies.get())
            {
                findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(new TranslatableComponent("com.minecolonies.coremod.gui.colony.denied.existingandabandon"));
            }
            else
            {
                findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(new TranslatableComponent("com.minecolonies.coremod.gui.colony.denied.existing"));
            }
        }
        else
        {
            findPaneOfTypeByID(TEXT_OWN, Text.class).setText(new TranslatableComponent("com.minecolonies.coremod.gui.colony.none"));

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
                    findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(LanguageHandler.buildChatComponent("com.minecolonies.coremod.gui.colony.denied.tooclose",
                      colony.getName()));
                }
            }
        }

        if (MineColonies.getConfig().getServer().restrictColonyPlacement.get())
        {
            final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(pos, new BlockPos(world.getLevelData().getXSpawn(), world.getLevelData().getYSpawn(), world.getLevelData().getZSpawn())));
            if (spawnDistance < MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get())
            {
                findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(new TranslatableComponent(CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN,
                  MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get()));
            }
            else if (spawnDistance > MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get())
            {
                findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(new TranslatableComponent(CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN,
                  MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get()));
            }
        }

        if (findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).isTextEmpty())
        {
            findPaneOfTypeByID(BUTTON_CREATE, ButtonImage.class).enable();
            findPaneOfTypeByID(TEXT_FEEDBACK, Text.class).setText(new TranslatableComponent("com.minecolonies.coremod.gui.colony.allowed.create"));
        }

        registerButton(BUTTON_CLOSE, () -> close());
        registerButton(BUTTON_CREATE, () -> onCreate());
        registerButton(BUTTON_DELETE, () -> new WindowTownHallColonyDelete().open());
    }

    /**
     * On create button
     */
    public void onCreate()
    {
        new VanillaParticleMessage(pos.getX(), pos.getY(), pos.getZ(), ParticleTypes.DRAGON_BREATH).onExecute(null, false);
        Minecraft.getInstance().level.playSound(Minecraft.getInstance().player, new BlockPos(Minecraft.getInstance().player.position()),
          SoundEvents.CAMPFIRE_CRACKLE, SoundSource.AMBIENT, 2.5f, 0.8f);
        Network.getNetwork().sendToServer(new CreateColonyMessage(pos));
        close();
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
