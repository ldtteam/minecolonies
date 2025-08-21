package com.minecolonies.core.datalistener;

import com.google.gson.JsonObject;
import com.minecolonies.core.datalistener.model.StudyItem;
import com.minecolonies.core.datalistener.util.MappingResult;
import com.minecolonies.core.util.GsonHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

/**
 * Loads and listens to study items for the {@link com.minecolonies.core.colony.buildings.workerbuildings.BuildingLibrary}.
 */
public class StudyItemListener extends BaseDataListener<StudyItem>
{
    /**
     * Singleton instance.
     */
    public static final StudyItemListener INSTANCE = new StudyItemListener();

    /**
     * Default constructor.
     *
     */
    private StudyItemListener()
    {
        super("study_items");
    }

    @Override
    @NotNull
    protected MappingResult<StudyItem> mapEntry(final ResourceLocation key, final JsonObject object)
    {
        final Item item = BuiltInRegistries.ITEM.get(GsonHelper.getAsResourceLocation(object, "item"));
        final int skillIncreaseChance = Mth.clamp(GsonHelper.getAsInt(object, "skill_increase_chance", 1), 1, 100);
        final int breakChance = Mth.clamp(GsonHelper.getAsInt(object, "break_chance", 1), 1, 100);
        return MappingResult.ok(new StudyItem(item, skillIncreaseChance, breakChance));
    }
}
