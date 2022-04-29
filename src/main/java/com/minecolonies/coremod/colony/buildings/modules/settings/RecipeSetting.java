package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockout.Loader;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.View;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.*;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores a string-list setting (Like enum, but easily serializable).
 */
public class CraftingSetting implements ICraftingSetting
{
    /**
     * Current index of the setting.
     */
    protected IToken<?> currentIndex;

    /**
     * The specific crafting module.
     */
    private final String craftingModuleId;

    /**
     * Create a new crafting setting.
     * @param craftingModuleId the crafting module id.
     */
    public CraftingSetting(final String craftingModuleId)
    {
        this.craftingModuleId = craftingModuleId;
    }

    /**
     * Create a new string list setting.
     *
     * @param currentIndex the current selected index.
     * @param craftingModuleId the crafting module id.
     */
    public CraftingSetting(final IToken<?> currentIndex, final String craftingModuleId)
    {
        this.currentIndex = currentIndex;
        this.craftingModuleId = craftingModuleId;
    }

    @Override
    public IToken<?> getValue(final IBuilding building)
    {
        final ICraftingBuildingModule craftingModule = building.getModuleMatching(ICraftingBuildingModule.class, m -> m.getId().equals(craftingModuleId));
        for (final IToken<?> token : craftingModule.getRecipes())
        {
            if (token.equals(currentIndex))
            {
                return currentIndex;
            }
        }

        currentIndex = building.getFirstModuleOccurance(ICraftingBuildingModule.class).getRecipes().get(0);
        return currentIndex;
    }

    @Override
    public ItemStack getValue(final IBuildingView building)
    {
        final CraftingModuleView craftingModule = building.getModuleViewMatching(CraftingModuleView.class, m -> m.getId().equals(craftingModuleId));

        for (final IRecipeStorage recipe : craftingModule.getRecipes())
        {
            if (recipe.getToken().equals(currentIndex))
            {
                return recipe.getPrimaryOutput();
            }
        }

        currentIndex = craftingModule.getRecipes().get(0).getToken();
        return craftingModule.getRecipes().get(0).getPrimaryOutput();
    }

    @Override
    public List<ItemStack> getSettings(final IBuilding building)
    {
        final List<ItemStack> settings = new ArrayList<>();
        for (final IToken<?> token : building.getFirstModuleOccurance(ICraftingBuildingModule.class).getRecipes())
        {
            settings.add(IColonyManager.getInstance().getRecipeManager().getRecipe(token).getPrimaryOutput());
        }
        return new ArrayList<>(settings);
    }

    @Override
    public List<ItemStack> getSettings(final IBuildingView building)
    {
        final List<ItemStack> settings = new ArrayList<>();
        for (final IRecipeStorage recipe : building.getModuleView(CraftingModuleView.class).getRecipes())
        {
            settings.add(recipe.getPrimaryOutput());
        }
        return new ArrayList<>(settings);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setupHandler(
      final ISettingKey<?> key,
      final Pane pane,
      final ISettingsModuleView settingsModuleView,
      final IBuildingView building, final Window window)
    {
        Loader.createFromXMLFile("minecolonies:gui/layouthuts/layoutcraftingsetting.xml", (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(key.getUniqueId().toString());
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(input -> {

            final List<IRecipeStorage> list = building.getModuleView(CraftingModuleView.class).getRecipes();
            int currentIntIndex = 0;

            int index = 0;
            for (final IRecipeStorage recipe : list)
            {
                index++;
                if (recipe.getToken().equals(currentIntIndex))
                {
                    currentIntIndex = index;
                    break;
                }
            }
            int newIndex = currentIntIndex + 1;
            if (newIndex >= list.size())
            {
                newIndex = 0;
            }

            currentIndex = list.get(newIndex).getToken();
            settingsModuleView.trigger(key);
        });
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final Window window)
    {
        final ItemStack stack = getValue(building);
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(new TranslationTextComponent(stack.getDescriptionId()));
        pane.findPaneOfTypeByID("icon", ItemIcon.class).setItem(stack);
    }

    @Override
    public void trigger()
    {

    }

    @Override
    public void set(final IToken<?> value)
    {
        currentIndex = value;
    }

    @Override
    public boolean isActive(final ISettingsModule module)
    {
        final ICraftingBuildingModule craftingModule = module.getBuilding().getModuleMatching(ICraftingBuildingModule.class, m -> m.getId().equals(craftingModuleId));
        return craftingModule != null && !craftingModule.getRecipes().isEmpty() ;
    }

    @Override
    public boolean isActive(final ISettingsModuleView module)
    {
        final CraftingModuleView craftingModule = module.getBuildingView().getModuleViewMatching(CraftingModuleView.class, m -> m.getId().equals(craftingModuleId));
        return craftingModule != null && !craftingModule.getRecipes().isEmpty() ;
    }
}
