package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.controls.Text;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.event.ColonyStoryListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.Biome;

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
    private final int closestDistance;
    private final String closestName;

    public WindowTownHallColonyReactivate(final BlockPos pos, final String closestName, final int closestDistance)
    {
        super(MOD_ID + TOWNHALL_COLONY_REACTIVATE_GUI);
        this.pos = pos;
        this.closestName = closestName;
        this.closestDistance = closestDistance;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));

        registerButton(BUTTON_CANCEL, this::close);
        registerButton(BUTTON_CREATE, this::onCreate);

        final Random random = new Random(pos.asLong());
        final Holder<Biome> biome = mc.level.getBiome(pos);
        this.preName = ColonyStoryListener.pickRandom(ColonyStoryListener.abandonedColonyNames, biome, random);
        final String story = ColonyStoryListener.pickRandom(ColonyStoryListener.abandonedColonyStories, biome, random);

        this.findPaneOfTypeByID("title", Text.class).setText(Component.translatable("com.minecolonies.core.gui.colony.reactivate.title", this.preName));
        this.findPaneOfTypeByID("text1", Text.class).setText(Component.translatable(story, this.preName, Component.translatable(biome.unwrapKey().get().location().toLanguageKey("biome"))));
        this.findPaneOfTypeByID("text2", Text.class).setText(Component.translatable("com.minecolonies.core.gui.colony.reactivate.question", this.preName));
    }

    /**
     * On create button
     */
    public void onCreate()
    {
        new WindowTownHallColonyManage(pos, closestName, closestDistance, preName, true).open();
    }
}
