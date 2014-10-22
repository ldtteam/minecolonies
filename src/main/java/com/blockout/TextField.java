package com.blockout;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class TextField extends Pane
{
    public interface Filter
    {
        public String filter(String s);
        public boolean isAllowedCharacter(char c);
    }

    //  Attributes
    protected int     maxTextLength     = 32;
    protected int     textColor         = 0xE0E0E0; //14737632
    protected int     textColorDisabled = 0x707070; //7368816
    protected boolean shadow            = true;

    protected boolean backgroundEnabled    = true;
    protected int     backgroundOuterColor = 0xFFA0A09F; //-6250336
    protected int     backgroundInnerColor = 0xFEFFFFFF; //-16777216

    //  Runtime
    protected String text = "";
    protected Filter filter;
    protected int cursorPosition     = 0;
    protected int scrollOffset       = 0;
    protected int selectionEnd       = 0;
    protected int cursorBlinkCounter = 0;

    public TextField(){}

    public TextField(TextField other)
    {
        super(other);
        maxTextLength = other.maxTextLength;
        textColor = other.textColor;
        textColorDisabled = other.textColorDisabled;
        shadow = other.shadow;
        backgroundEnabled = other.backgroundEnabled;
        backgroundOuterColor = other.backgroundOuterColor;
        backgroundInnerColor = other.backgroundInnerColor;
    }

    public TextField(XMLNode xml)
    {
        super(xml);
        maxTextLength        = xml.getIntegerAttribute("maxlength", maxTextLength);
        textColor            = xml.getColorAttribute("color", textColor);
        textColorDisabled    = xml.getColorAttribute("colordisabled", textColorDisabled);
        shadow               = xml.getBooleanAttribute("shadow", shadow);
        backgroundEnabled    = xml.getBooleanAttribute("background", backgroundEnabled);
        backgroundOuterColor = xml.getColorAttribute("backgroundOuter", backgroundOuterColor);
        backgroundInnerColor = xml.getColorAttribute("backgroundInner", backgroundInnerColor);
    }

    public Filter getFilter() { return filter; }
    public void setFilter(Filter f) { filter = f; }

    public String getText() { return text; }
    public void setText(String s)
    {
        text = (s.length() <= maxTextLength ? s : s.substring(0, maxTextLength));
        setCursorPosition(text.length());
    }

    public int getMaxTextLength() { return maxTextLength; }
    public void setMaxTextLength(int m) { maxTextLength = m; }

    public int getTextColor() { return textColor; }
    public int getTextColorDisabled() { return textColorDisabled; }
    public void setTextColor(int c) { textColor = c; }
    public void setTextColorDisabled(int c) { textColorDisabled = c; }

    public boolean getBackgroundEnabled() { return backgroundEnabled; }
    public int getBackgroundOuterColor() { return backgroundOuterColor; }
    public int getBackgroundInnerColor() { return backgroundInnerColor; }
    public void setBackgroundEnabled(boolean e) { backgroundEnabled = e; }
    public void setBackgroundOuterColor(int c) { backgroundOuterColor = c; }
    public void setBackgroundInnerColor(int c) { backgroundInnerColor = c; }

    public int getCursorPosition() { return cursorPosition; }
    public void setCursorPosition(int pos)
    {
        cursorPosition = MathHelper.clamp_int(pos, 0, text.length());
        setSelectionEnd(cursorPosition);
    }
//    public void setCursorPositionZero() { setCursorPosition(0); }
//    public void setCursorPositionEnd() { setCursorPosition(text.length()); }
    public void moveCursorBy(int offset) { setCursorPosition(selectionEnd + offset); }

    public int getSelectionEnd() { return selectionEnd; }
    public void setSelectionEnd(int pos)
    {
        selectionEnd = MathHelper.clamp_int(pos, 0, text.length());

        int drawWidth = backgroundEnabled ? getWidth() - 8 : getWidth();
        if (drawWidth > 0)
        {
            if (scrollOffset > text.length())
            {
                scrollOffset = text.length();
            }

            String visibleString = mc.fontRenderer.trimStringToWidth(text.substring(scrollOffset), drawWidth);
            int rightmostVisibleChar = visibleString.length() + scrollOffset;

            if (selectionEnd == scrollOffset)
            {
                scrollOffset -= mc.fontRenderer.trimStringToWidth(text, drawWidth, true).length();
            }

            if (selectionEnd > rightmostVisibleChar)
            {
                scrollOffset += selectionEnd - rightmostVisibleChar;
            }
            else if (selectionEnd <= scrollOffset)
            {
                scrollOffset -= scrollOffset - selectionEnd;
            }

            scrollOffset = MathHelper.clamp_int(scrollOffset, 0, text.length());
        }
    }

    public String getSelectedText()
    {
        int start = Math.min(cursorPosition, selectionEnd);
        int end = Math.max(cursorPosition, selectionEnd);
        return text.substring(start, end);
    }

    @Override
    public void putInside(View view)
    {
        super.putInside(view);
        setSelectionEnd(selectionEnd);  //  Recompute scroll offset
    }

    @Override
    protected void drawSelf(int mx, int my)
    {
        int color = enabled ? textColor : textColorDisabled;
        int drawX = x;
        int drawY = y;
        int drawWidth = width;

        if (backgroundEnabled)
        {
            //  Draw box
            drawRect(x - 1, y - 1, x + width + 1, y + height + 1, backgroundOuterColor);
            drawRect(x, y, x + width, y + height, backgroundInnerColor);

            drawX += 4;
            drawY += (height - 8) / 2;
            drawWidth -= 8;
        }

        //  Determine the portion of the string that is visible on screen
        String visibleString = mc.fontRenderer.trimStringToWidth(text.substring(scrollOffset), drawWidth);

        int relativeCursorPosition = cursorPosition - scrollOffset;
        int relativeSelectionEnd = selectionEnd - scrollOffset;
        boolean cursorVisible = (relativeCursorPosition >= 0 && relativeCursorPosition <= visibleString.length());
        boolean cursorBeforeEnd = cursorPosition < text.length() || text.length() >= maxTextLength;

        //  Enforce selection to the length limit of the visible string
        if (relativeSelectionEnd > visibleString.length())
        {
            relativeSelectionEnd = visibleString.length();
        }

        //  Draw string up through cursor
        int textX = drawX;
        if (visibleString.length() > 0)
        {
            String s1 = cursorVisible ? visibleString.substring(0, relativeCursorPosition) : visibleString;
            textX = mc.fontRenderer.drawString(s1, textX, drawY, color, shadow);
        }

        int cursorX = textX;
        if (!cursorVisible)
        {
            cursorX = (relativeCursorPosition > 0 ? drawX + width : drawX);
        }
        else if (cursorBeforeEnd)
        {
            if (shadow) textX -= 1;
            cursorX = textX;
        }

        //  Draw string after cursor
        if (visibleString.length() > 0 && cursorVisible && relativeCursorPosition < visibleString.length())
        {
            mc.fontRenderer.drawString(visibleString.substring(relativeCursorPosition), textX, drawY, color, shadow);
        }

        //  Should we draw the cursor this frame?
        if (isFocus() && cursorVisible && (cursorBlinkCounter / 6 % 2 == 0))
        {
            if (cursorBeforeEnd)
            {
                drawRect(cursorX, drawY - 1, cursorX + 1, drawY + 1 + mc.fontRenderer.FONT_HEIGHT, -3092272);
            }
            else
            {
                mc.fontRenderer.drawString("_", cursorX, drawY, color, shadow);
            }
        }

        //  Draw selection
        if (relativeSelectionEnd != relativeCursorPosition)
        {
            int selectedDrawX = drawX + mc.fontRenderer.getStringWidth(visibleString.substring(0, relativeSelectionEnd));
            //this.drawCursorVertical(drawX3, drawY - 1, selectedDrawWidth - 1, drawY + 1 + mc.fontRenderer.FONT_HEIGHT);

            int selectionStartX = Math.min(cursorX, selectedDrawX - 1);
            int selectionEndX = Math.max(cursorX, selectedDrawX - 1);

            if (selectionStartX > (x + width))
            {
                selectionStartX = (x + width);
            }

            if (selectionEndX > (x + width))
            {
                selectionEndX = (x + width);
            }

            Tessellator tessellator = Tessellator.instance;
            GL11.glColor4f(0.0F, 0.0F, 255.0F, 255.0F);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
            GL11.glLogicOp(GL11.GL_OR_REVERSE);
            tessellator.startDrawingQuads();
            tessellator.addVertex((double)selectionStartX, (double)drawY + 1 + mc.fontRenderer.FONT_HEIGHT, 0.0D);
            tessellator.addVertex((double)selectionEndX, (double)drawY + 1 + mc.fontRenderer.FONT_HEIGHT, 0.0D);
            tessellator.addVertex((double)selectionEndX, (double)drawY - 1, 0.0D);
            tessellator.addVertex((double)selectionStartX, (double)drawY - 1, 0.0D);
            tessellator.draw();
            GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    @Override
    public void onMouseClicked(int mx, int my)
    {
        if (mx < 0) return;

        int clickX = mx;
        int drawWidth = width;

        if (backgroundEnabled)
        {
            clickX -= 4;
            drawWidth -= 8;
        }

        String visibleString = mc.fontRenderer.trimStringToWidth(text.substring(scrollOffset), drawWidth);
        String trimmedString = mc.fontRenderer.trimStringToWidth(visibleString, clickX);

        setCursorPosition(trimmedString.length() + scrollOffset);

        setFocus();
    }

    @Override
    public boolean onKeyTyped(char c, int key)
    {
        switch (c)
        {
            case 1:
                setCursorPosition(text.length());
                setSelectionEnd(0);
                return true;

            case 3:
                GuiScreen.setClipboardString(getSelectedText());
                return true;

            case 22:
                writeText(GuiScreen.getClipboardString());
                return true;

            case 24:
                GuiScreen.setClipboardString(getSelectedText());
                writeText("");
                return true;

            default:
                switch (key)
                {
                    case Keyboard.KEY_BACK:
                    case Keyboard.KEY_DELETE:
                    {
                        int direction = (key == Keyboard.KEY_BACK) ? -1 : 1;

                        if (GuiScreen.isCtrlKeyDown())
                        {
                            deleteWords(direction);
                        }
                        else
                        {
                            deleteFromCursor(direction);
                        }

                        return true;
                    }

                    case Keyboard.KEY_HOME:
                    case Keyboard.KEY_END:
                    {
                        int position = (key == Keyboard.KEY_HOME) ? 0 : text.length();

                        if (GuiScreen.isShiftKeyDown())
                        {
                            setSelectionEnd(position);
                        }
                        else
                        {
                            setCursorPosition(position);
                        }
                        return true;
                    }

                    case Keyboard.KEY_LEFT:
                    case Keyboard.KEY_RIGHT:
                    {
                        int direction = (key == Keyboard.KEY_LEFT) ? -1 : 1;

                        if (GuiScreen.isShiftKeyDown())
                        {
                            if (GuiScreen.isCtrlKeyDown())
                            {
                                setSelectionEnd(getNthWordFromPos(direction, getSelectionEnd()));
                            }
                            else
                            {
                                setSelectionEnd(getSelectionEnd() + direction);
                            }
                        }
                        else if (GuiScreen.isCtrlKeyDown())
                        {
                            setCursorPosition(getNthWordFromCursor(direction));
                        }
                        else
                        {
                            moveCursorBy(direction);
                        }
                        return true;
                    }

                    default:
                        if (filter.isAllowedCharacter(c))
                        {
                            writeText(Character.toString(c));
                            return true;
                        }
                        break;
                }
        }

        return false;
    }

    @Override
    public void onUpdate()
    {
        cursorBlinkCounter++;
    }

    @Override
    public void onFocus()
    {
        cursorBlinkCounter = 0;
    }

    public void writeText(String str)
    {
        str = filter.filter(str);

        int insertAt = Math.min(cursorPosition, selectionEnd);
        int insertEnd = Math.max(cursorPosition, selectionEnd);
        int availableChars = (maxTextLength - text.length()) + (insertEnd - insertAt);

        String result = "";
        if (text.length() > 0 && insertAt > 0)
        {
            result = text.substring(0, insertAt);
        }

        int insertedLength;
        if (availableChars < str.length())
        {
            result = result + str.substring(0, availableChars);
            insertedLength = availableChars;
        }
        else
        {
            result = result + str;
            insertedLength = str.length();
        }

        if (text.length() > 0 && insertEnd < text.length())
        {
            result = result + text.substring(insertEnd);
        }

        text = result;
        moveCursorBy((insertAt - selectionEnd) + insertedLength);
    }

    public void deleteWords(int count)
    {
        if (text.length() != 0)
        {
            if (selectionEnd != cursorPosition)
            {
                this.writeText("");
            }
            else
            {
                deleteFromCursor(this.getNthWordFromCursor(count) - this.cursorPosition);
            }
        }
    }

    public void deleteFromCursor(int count)
    {
        if (text.length() == 0) return;

        if (selectionEnd != cursorPosition)
        {
            this.writeText("");
        }
        else
        {
            boolean backwards = count < 0;
            int     start = backwards ? this.cursorPosition + count : this.cursorPosition;
            int     end   = backwards ? this.cursorPosition : this.cursorPosition + count;
            String  result = "";

            if (start > 0)
            {
                result = text.substring(0, start);
            }

            if (end < text.length())
            {
                result = result + text.substring(end);
            }

            text = result;

            if (backwards)
            {
                this.moveCursorBy(count);
            }
        }
    }

    public int getNthWordFromPos(int count, int pos)
    {
        boolean reverse = count < 0;
        count = Math.abs(count);

        for (int i1 = 0; i1 < count; ++i1)
        {
            if (reverse)
            {
                while (pos > 0 && text.charAt(pos - 1) == ' ') { --pos; }
                while (pos > 0 && text.charAt(pos - 1) != ' ') { --pos; }
            }
            else
            {
                pos = text.indexOf(' ', pos);

                if (pos == -1) { pos = text.length(); }
                else
                {
                    while (pos < text.length() && text.charAt(pos) == ' ') { ++pos; }
                }
            }
        }

        return pos;
    }

    public int getNthWordFromCursor(int count)
    {
        return getNthWordFromPos(count, cursorPosition);
    }
}
