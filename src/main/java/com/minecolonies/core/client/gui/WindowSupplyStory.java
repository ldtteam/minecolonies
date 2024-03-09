package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.controls.Text;
import com.minecolonies.core.client.gui.townhall.WindowTownHallColonyManage;
import com.minecolonies.core.datalistener.ColonyStoryDataListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.WindowConstants.SUPPLIES_STORY_RESOURCE_SUFFIX;
import static com.minecolonies.api.util.constant.WindowConstants.TOWNHALL_COLONY_REACTIVATE_GUI;

/**
 * Supply Story Window
 */
public class WindowSupplyStory extends AbstractWindowSkeleton
{
    private static final String BUTTON_CLOSE  = "cancel";
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

    public WindowSupplyStory(final Player player, final BlockPos pos, final Level world, final String closestName, final int closestDistance)
    {
        // todo: We have three modes: a) Already seen this UI, b) Dungeon Supplies and c) Crafted supplies.
        //  Already seen redirects directly to normal UI, so we store this in item nbt (on button click we store it as an ack)
        //  Crafted supplies just shows the small intro text about the placement and the colony
        //  Dungeon supplies show story + text
        // todo big text field. Concat the two translated texts with some \n in the middle into the same field. Paragraph 1: Story, Paragraph 2: Expalanation.
        // todo button to go to the actual UI, todo button to select a style, todo, give it a pos to the UI (player pos + direction facing + sth)


        super(MOD_ID + SUPPLIES_STORY_RESOURCE_SUFFIX);
        this.player = player;
        this.pos = pos;
        this.closestName = closestName;
        this.closestDistance = closestDistance;
        this.world = world;

        registerButton(BUTTON_CLOSE, this::close);
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
