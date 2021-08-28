package com.minecolonies.coremod.util.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

public class NonSiblingFormattingTextComponent extends TextComponent
{
    public NonSiblingFormattingTextComponent()
    {
        super("");
    }

    @NotNull
    @Override
    public String getString()
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (Component itextcomponent : this.siblings)
        {
            String s = itextcomponent.getString();

            if (!s.isEmpty())
            {
                stringbuilder.append(s);
            }
        }

        stringbuilder.append(ChatFormatting.RESET);

        return stringbuilder.toString();
    }
}
