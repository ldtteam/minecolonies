package com.blockout;

import net.minecraft.util.ChatAllowedCharacters;

public class TextFieldVanilla extends TextField
{
    protected int colorOuter = -6250336,
                  colorInner = -16777216;

    public TextFieldVanilla() { setFilter(new FilterVanilla()); }
    public TextFieldVanilla(TextFieldVanilla other) { super(other); setFilter(new FilterVanilla()); }
    public TextFieldVanilla(Pane.PaneInfo info) { super(info); setFilter(new FilterVanilla()); }
    public TextFieldVanilla(Pane.PaneInfo info, View view) { super(info, view); setFilter(new FilterVanilla()); }

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
