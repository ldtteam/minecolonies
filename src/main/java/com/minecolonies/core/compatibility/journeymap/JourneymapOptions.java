package com.minecolonies.core.compatibility.journeymap;

import journeymap.client.api.option.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_JOURNEY_MAP_INFO;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class JourneymapOptions
{
    private final Option<BorderStyle> borderFullscreenStyle;
    private final Option<BorderStyle> borderMinimapStyle;
    private final Option<Boolean> deathpoints;
    private final Option<Boolean> colonyname;
    private final Option<Boolean> colonistNameMinimap;
    private final Option<Boolean> colonistNameFullscreen;
    private final Option<Boolean> colonistTooltips;
    private final Option<Boolean> colonistTeam;
    private final Option<Boolean> guards;
    private final Option<Boolean> citizens;
    private final Option<Boolean> visitors;
    private final Option<RaiderColor> raiders;

    public JourneymapOptions()
    {
        final String prefix = PARTIAL_JOURNEY_MAP_INFO + "options.";
        final OptionCategory category = new OptionCategory(MOD_ID, prefix + "category");

        this.borderFullscreenStyle = new EnumOption<>(category, "borderFullscreenStyle", prefix + "borderfullscreenstyle", BorderStyle.FILLED).setSortOrder(100);
        this.borderMinimapStyle = new EnumOption<>(category, "borderMinimapStyle", prefix + "borderminimapstyle", BorderStyle.FRAMED).setSortOrder(101);
        this.deathpoints = new BooleanOption(category, "deathpoints", prefix + "deathpoints", true).setSortOrder(150);
        this.colonyname = new BooleanOption(category, "colonyname", prefix + "colonyname", true).setSortOrder(180);
        this.colonistNameMinimap = new BooleanOption(category, "colonistNameMinimap", prefix + "colonistnameminimap", true).setSortOrder(201);
        this.colonistNameFullscreen = new BooleanOption(category, "colonistNameFullscreen", prefix + "colonistnamefullscreen", true).setSortOrder(200);
        this.colonistTooltips = new BooleanOption(category, "colonistTooltips", prefix + "colonisttooltips", true).setSortOrder(202);
        this.colonistTeam = new BooleanOption(category, "colonistTeam", prefix + "colonistteam", true).setSortOrder(203);
        this.guards = new BooleanOption(category, "guards", prefix + "guards", true).setSortOrder(300);
        this.citizens = new BooleanOption(category, "citizens", prefix + "citizens", true).setSortOrder(301);
        this.visitors = new BooleanOption(category, "visitors", prefix + "visitors", true).setSortOrder(302);
        this.raiders = new EnumOption<>(category, "raiders", prefix + "raiders", RaiderColor.HOSTILE).setSortOrder(303);
    }

    public static BorderStyle getBorderFullscreenStyle(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.borderFullscreenStyle.get()).orElse(BorderStyle.FILLED);
    }

    public static BorderStyle getBorderMinimapStyle(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.borderMinimapStyle.get()).orElse(BorderStyle.FRAMED);
    }

    public static boolean getDeathpoints(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.deathpoints.get()).orElse(true);
    }

    public static boolean getShowColonyName(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.colonyname.get()).orElse(true);
    }

    public static boolean getShowColonistNameMinimap(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.colonistNameMinimap.get()).orElse(true);
    }

    public static boolean getShowColonistNameFullscreen(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.colonistNameFullscreen.get()).orElse(true);
    }

    public static boolean getShowColonistTooltip(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.colonistTooltips.get()).orElse(true);
    }

    public static boolean getShowColonistTeamColour(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.colonistTeam.get()).orElse(true);
    }

    public static boolean getShowGuards(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.guards.get()).orElse(true);
    }

    public static boolean getShowCitizens(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.citizens.get()).orElse(true);
    }

    public static boolean getShowVisitors(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.visitors.get()).orElse(true);
    }

    public static RaiderColor getRaiderColor(@NotNull final Optional<JourneymapOptions> options)
    {
        return options.map(o -> o.raiders.get()).orElse(RaiderColor.HOSTILE);
    }

    public enum BorderStyle implements KeyedEnum
    {
        HIDDEN(PARTIAL_JOURNEY_MAP_INFO + "borderstyle.hidden"),
        FRAMED(PARTIAL_JOURNEY_MAP_INFO + "borderstyle.framed"),
        FILLED(PARTIAL_JOURNEY_MAP_INFO + "borderstyle.filled");

        private final String key;

        BorderStyle(final String key)
        {
            this.key = key;
        }

        @Override
        public String getKey()
        {
            return this.key;
        }
    }

    public enum RaiderColor implements KeyedEnum
    {
        HOSTILE(PARTIAL_JOURNEY_MAP_INFO + "raidercolor.hostile", TextColor.fromRgb(0xFFFFFFFF)),
        NONE(PARTIAL_JOURNEY_MAP_INFO + "raidercolor.none", TextColor.fromRgb(0xFF000000)),
        YELLOW(PARTIAL_JOURNEY_MAP_INFO + "raidercolor.yellow", TextColor.fromLegacyFormat(ChatFormatting.YELLOW)),
        RED(PARTIAL_JOURNEY_MAP_INFO + "raidercolor.red", TextColor.fromLegacyFormat(ChatFormatting.RED)),
        PURPLE(PARTIAL_JOURNEY_MAP_INFO + "raidercolor.purple", TextColor.fromLegacyFormat(ChatFormatting.LIGHT_PURPLE)),
        ORANGE(PARTIAL_JOURNEY_MAP_INFO + "raidercolor.orange", TextColor.fromLegacyFormat(ChatFormatting.GOLD));

        private final String key;
        private final TextColor color;

        RaiderColor(final String key, final TextColor color)
        {
            this.key = key;
            this.color = color;
        }

        @Override
        public String getKey()
        {
            return this.key;
        }

        @NotNull
        public TextColor getColor()
        {
            return this.color;
        }
    }
}
