package com.minecolonies.api.entity.citizen;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

/**
 * Enum for citizen status icons, resource location and translation
 */
public enum VisibleCitizenStatus
{
    EAT(new ResourceLocation(Constants.MOD_ID, "textures/icons/hungry_2.png"), "com.minecolonies.gui.visiblestatus.eat"),
    HOUSE(new ResourceLocation(Constants.MOD_ID, "textures/icons/house_big.png"), "com.minecolonies.gui.visiblestatus.idle"),
    RAIDED(new ResourceLocation(Constants.MOD_ID, "textures/icons/raid_icon.png"), "com.minecolonies.gui.visiblestatus.raid"),
    MOURNING(new ResourceLocation(Constants.MOD_ID, "textures/icons/mourning.png"), "com.minecolonies.gui.visiblestatus.mourn"),
    BAD_WEATHER(new ResourceLocation(Constants.MOD_ID, "textures/icons/bad_weather.png"), "com.minecolonies.gui.visiblestatus.rain"),
    SLEEP(new ResourceLocation(Constants.MOD_ID, "textures/icons/bed_icon.png"), "com.minecolonies.gui.visiblestatus.sleep"),
    SICK(new ResourceLocation(Constants.MOD_ID, "textures/icons/sick_icon.png"), "com.minecolonies.gui.visiblestatus.sick");

    private ResourceLocation icon;
    private String           translationKey;

    VisibleCitizenStatus(final ResourceLocation icon, final String translationKey)
    {
        this.icon = icon;
        this.translationKey = translationKey;
    }

    /**
     * Returns the Icon to use
     *
     * @return icon texture location
     */
    public ResourceLocation getIcon()
    {
        return icon;
    }

    /**
     * Gets the translated display text
     *
     * @return String to display
     */
    public String getTranslatedText()
    {
        return LanguageHandler.translateKey(translationKey);
    }
}
