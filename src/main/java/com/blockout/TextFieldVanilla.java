package com.blockout;

import net.minecraft.util.ChatAllowedCharacters;

public class TextFieldVanilla extends TextField
{
    protected int colorOuter = 0xFFA0A09F, //-6250336
                  colorInner = 0xFEFFFFFF; //-16777216

    public TextFieldVanilla()
    {
        setFilter(new FilterVanilla());
    }

    public TextFieldVanilla(TextFieldVanilla other)
    {
        super(other);
        colorOuter = other.colorOuter;
        colorInner = other.colorInner;
        setFilter(new FilterVanilla());
    }

    public TextFieldVanilla(XMLNode xml)
    {
        super(xml);
        colorOuter = xml.getColorAttribute("outer", colorOuter);
        colorInner = xml.getColorAttribute("inner", colorInner);
        setFilter(new FilterVanilla());
    }

    public int getColorOuter() { return colorOuter; }
    public void setColorOuter(int c) { colorOuter = c; }

    public int getColorInner() { return colorInner; }
    public void setColorInner(int c) { colorInner = c; }

    public static class FilterNumeric implements Filter {
        public String filter(String s) {
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray())
                if (isAllowedCharacter(c))
                    sb.append(c);
            return sb.toString();
        }

        public boolean isAllowedCharacter(char c) { return Character.isDigit(c); }
    }

    public static class FilterVanilla implements Filter {
        public String filter(String s) {
            return ChatAllowedCharacters.filerAllowedCharacters(s);
        }

        public boolean isAllowedCharacter(char c) {
            return ChatAllowedCharacters.isAllowedCharacter(c);
        }
    }
}
