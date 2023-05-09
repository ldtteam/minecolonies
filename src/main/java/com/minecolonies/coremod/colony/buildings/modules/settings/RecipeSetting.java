package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.*;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores a recipe based setting.
 */
public class RecipeSetting implements ICraftingSetting
{
    /**
     * Current index of the setting.
     */
    protected IToken<?> currentIndex;

    /**
     * The specific crafting module.
     */
    protected final String craftingModuleId;

    /**
     * Create a new crafting setting.
     * @param craftingModuleId the crafting module id.
     */
    public RecipeSetting(final String craftingModuleId)
    {
        this.craftingModuleId = craftingModuleId;
    }

    /**
     * Create a new string list setting.
     *
     * @param currentIndex the current selected index.
     * @param craftingModuleId the crafting module id.
     */
    public RecipeSetting(final IToken<?> currentIndex, final String craftingModuleId)
    {
        this.currentIndex = currentIndex;
        this.craftingModuleId = craftingModuleId;
    }

    @Override
    public IRecipeStorage getValue(final IBuilding building)
    {
        final ICraftingBuildingModule craftingModule = building.getModuleMatching(ICraftingBuildingModule.class, m -> m.getId().equals(craftingModuleId));
        for (final IToken<?> token : craftingModule.getRecipes())
        {
            if (token.equals(currentIndex))
            {
                return IColonyManager.getInstance().getRecipeManager().getRecipe(currentIndex);
            }
        }

        currentIndex = building.getFirstModuleOccurance(ICraftingBuildingModule.class).getRecipes().get(0);
        return IColonyManager.getInstance().getRecipeManager().getRecipe(currentIndex);
    }

    @Override
    public IRecipeStorage getValue(final IBuildingView building)
    {
        final CraftingModuleView craftingModule = building.getModuleViewMatching(CraftingModuleView.class, m -> m.getId().equals(craftingModuleId));

        for (final IRecipeStorage recipe : craftingModule.getRecipes())
        {
            if (recipe.getToken().equals(currentIndex))
            {
                return recipe;
            }
        }

        currentIndex = craftingModule.getRecipes().get(0).getToken();
        return craftingModule.getRecipes().get(0);
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
      final IBuildingView building, final BOWindow window)
    {
        Loader.createFromXMLFile(new ResourceLocation("minecolonies:gui/layouthuts/layoutcraftingsetting.xml"), (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(Component.literal(key.getUniqueId().toString()));
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(input -> {

            final List<IRecipeStorage> list = building.getModuleView(CraftingModuleView.class).getRecipes();
            int currentIntIndex = 0;

            int index = 0;
            for (final IRecipeStorage recipe : list)
            {
                if (recipe.getToken().equals(currentIndex))
                {
                    currentIntIndex = index;
                    break;
                }
                index++;
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
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final BOWindow window)
    {
        final IRecipeStorage stack = getValue(building);
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(Component.translatable(stack.getPrimaryOutput().getDescriptionId()));
        pane.findPaneOfTypeByID("iconto", ItemIcon.class).setItem(stack.getPrimaryOutput());
        pane.findPaneOfTypeByID("iconfrom", ItemIcon.class).setItem(stack.getCleanedInput().get(0).getItemStack());
    }

    @Override
    public void trigger()
    {

    }

    @Override
    public void set(final IRecipeStorage value)
    {
        currentIndex = value.getToken();
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

    @Override
    public void copyValue(final ISetting iSetting)
    {
        if (iSetting instanceof final RecipeSetting other)
        {
            currentIndex = other.currentIndex;
        }
    }
}
