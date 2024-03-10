package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.controls.Text;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.datalistener.ColonyStoryDataListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_CANCEL;
import static com.minecolonies.api.util.constant.WindowConstants.TOWNHALL_COLONY_REACTIVATE_GUI;

/**
 * UI to reactivate a colony.
 */
public class WindowTownHallColonyReactivate extends AbstractWindowSkeleton
{
    private static final String BUTTON_CREATE = "create";

    /**
     * Townhall position
     */
    private final BlockPos pos;
    private final String preName;
    private final Level world;
    private final int closestDistance;
    private final String closestName;
    private final Player player;

    public WindowTownHallColonyReactivate(final Player player, final BlockPos pos, final Level world, final String closestName, final int closestDistance)
    {
        super(MOD_ID + TOWNHALL_COLONY_REACTIVATE_GUI);
        this.player = player;
        this.pos = pos;
        this.closestName = closestName;
        this.closestDistance = closestDistance;
        this.world = world;

        registerButton(BUTTON_CANCEL, this::close);
        registerButton(BUTTON_CREATE, this::onCreate);

        final Random random = new Random(pos.asLong());
        this.preName = ColonyStoryDataListener.abandonedColonyNames.get(random.nextInt(ColonyStoryDataListener.abandonedColonyNames.size()));
        final String story = ColonyStoryDataListener.abandonedColonyStories.get(random.nextInt(ColonyStoryDataListener.abandonedColonyStories.size()));

        this.findPaneOfTypeByID("title", Text.class).setText(Component.translatable("com.minecolonies.core.gui.colony.reactivate.title", this.preName));
        this.findPaneOfTypeByID("text1", Text.class).setText(Component.translatable(story, this.preName, Component.translatable(world.getBiome(pos).unwrapKey().get().location().toLanguageKey("biome"))));
        this.findPaneOfTypeByID("text2", Text.class).setText(Component.translatable("com.minecolonies.core.gui.colony.reactivate.question", this.preName));
    }

    /**
     * On create button
     */
    public void onCreate()
    {
        new WindowTownHallColonyManage(player, pos, world, closestName, closestDistance, preName, true).open();
    }
}
