/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2015, Heiko Brumme
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package astechzgo.luminescent.text;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.rendering.RectangularObjectRenderer;
import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.RenderingUtils;
import astechzgo.luminescent.utils.SystemUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * This class contains a font texture for drawing text.
 *
 * @author Heiko Brumme
 */
public class Font {
	
	public static final String TEXTURE_NAME = "Glyph-Atlas";

    public static final String MONO_FONT_PATH = "ubuntu-mono/UbuntuMono-R";

    /**
     * Contains the font texture.
     */
    private final CharTexture texture;

    public Font(String fontName, int size) {
        try {
            texture = createFontTexture(fontName, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a font texture from specified AWT font.
     *
     * @return Font texture
     */
    private static CharTexture createFontTexture(String font, int size) throws IOException {
        Map<Character, Glyph> glyphs = new HashMap<>();
        try (MemoryStack stack = MemoryStack.stackPush();  InputStream in = SystemUtils.getResourceAsURL("fonts/" + font + ".ttf").openStream()) {
            STBTTFontinfo fontInfo = STBTTFontinfo.malloc(stack);
            byte[] data = in.readAllBytes();
            ByteBuffer fontBuffer = MemoryUtil.memAlloc(data.length).put(data).flip();
            STBTruetype.stbtt_InitFont(fontInfo, fontBuffer);

            float scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, size * 4 / 3.0f);
            int[] ascent = new int[1];
            int[] descent = new int[1];
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, descent, null);

            int imageWidth = 0;
            int imageHeight = (int)(size * 4 / 3.0f);
            int baseline = (int)(ascent[0] * scale);

            for (int i = 32; i < 256; i++) {
                if (i == 127) continue;

                int[] advance = new int[1];
                int[] lsb = new int[1];
                if (STBTruetype.stbtt_FindGlyphIndex(fontInfo, i) == 0) {
                    continue;
                }
                STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, i, advance, lsb);
                imageWidth += (int)Math.ceil(advance[0] * scale);
            }

            int xpos = 0;
            // extra row to avoid writing out of bounds
            ByteBuffer textureData = MemoryUtil.memCalloc(imageWidth * (imageHeight + 1));
            for (int i = 32; i < 256; i++) {
                if (i == 127) continue;

                if (STBTruetype.stbtt_FindGlyphIndex(fontInfo, i) == 0) {
                    continue;
                }

                int[] advance = new int[1];
                int[] lsb = new int[1];
                STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, i, advance, lsb);

                int[] ix0 = new int[1], iy0 = new int[1], ix1  = new int[1], iy1 = new int[1];
                STBTruetype.stbtt_GetCodepointBitmapBox(fontInfo, i, scale, scale, ix0, iy0,  ix1, iy1);

                int x = Math.round(lsb[0] * scale);
                int y = Math.max(0, baseline + iy0[0]);
                int glyphWidth = ix1[0] - ix0[0];
                int glyphHeight = Math.min(imageHeight - y, iy1[0] - iy0[0]);

                int boxWidth = (int)Math.ceil(advance[0] * scale);
                glyphs.put((char)i, new Glyph(boxWidth, imageHeight, xpos, 0));

                // each row will be written to textureData[xpos + row * stride, xpos + row * stride + width]
                // therefore, set stride to be the width of the entire image so that each row will be the start of
                // the texture
                STBTruetype.stbtt_MakeCodepointBitmap(fontInfo, textureData.position(imageWidth * y + xpos + x), glyphWidth, glyphHeight, imageWidth, scale, scale, i);
                xpos += boxWidth;
            }
            textureData.rewind();

            MemoryUtil.memFree(fontBuffer);

            Color textColour = Color.WHITE;
            ByteBuffer outputData = MemoryUtil.memAlloc(4 * imageWidth * imageHeight);
            for (int i = 0; i < imageHeight; i++) {
                for (int j = 0; j < imageWidth; j++) {
                    byte alpha = textureData.get();
                    byte red = (byte)textColour.getRed();
                    byte green = (byte)textColour.getGreen();
                    byte blue = (byte)textColour.getBlue();
                    outputData.put(new byte[] {red, green, blue, alpha});
                }
            }
            outputData.flip();

            MemoryUtil.memFree(textureData);

            return new CharTexture(TEXTURE_NAME, outputData, imageWidth, imageHeight, glyphs);
        }
    }

    /**
     * Gets the width of the specified text.
     *
     * @param text The text
     *
     * @return Width of text
     */
    public int getWidth(CharSequence text) {
        int width = 0;
        int lineWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                /* Line end, set width to maximum from line width and stored
                 * width */
                width = Math.max(width, lineWidth);
                lineWidth = 0;
                continue;
            }
            if (c == '\r') {
                /* Carriage return, just skip it */
                continue;
            }
            Glyph g = texture.glyphs.get(c);
            lineWidth += g.width();
        }
        width = Math.max(width, lineWidth);
        return width;
    }

    /**
     * Gets the height of the specified text.
     *
     * @param text The text
     *
     * @return Height of text
     */
    public int getHeight(CharSequence text) {
        int height = 0;
        int lineHeight = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                /* Line end, add line height to stored height */
                height += lineHeight;
                lineHeight = 0;
                continue;
            }
            if (c == '\r') {
                /* Carriage return, just skip it */
                continue;
            }
            Glyph g = texture.glyphs.get(c);
            lineHeight = Math.max(lineHeight, g.height());
        }
        height += lineHeight;
        return height;
    }

    /**
     * Draw text at the specified position and color.
     *
     * @param text     Text to draw
     * @param coordinates        Coordinates of the text position
     * @param colour        Color to use
     */
    public CharRenderer[] drawText(CharSequence text, WindowCoordinates coordinates, Color colour) {
        CharRenderer[] characters = new CharRenderer[text.length()];
        
        int textHeight = getHeight(text);
        int fontHeight = texture.getHeight();

        int drawX = (int) coordinates.getWindowCoordinatesX();
        int drawY = (int) coordinates.getWindowCoordinatesY();
        if (textHeight > fontHeight) {
            drawY += textHeight - fontHeight;
        }

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                /* Line feed, set x and y to draw at the next line */
                drawY += fontHeight;
                drawX = (int) coordinates.getWindowCoordinatesX();
                continue;
            }
            if (ch == '\r') {
                /* Carriage return, just skip it */
                continue;
            }
            Glyph g = texture.glyphs.get(ch);
            characters[i] = new CharRenderer(new WindowCoordinates(drawX, drawY), g.width(), g.height(), texture, ch);
            characters[i].setColour(colour);
            
            WindowCoordinates a = new WindowCoordinates(drawX, drawY);
            WindowCoordinates b = new WindowCoordinates(drawX + g.width(), drawY);
            WindowCoordinates c = new WindowCoordinates(drawX + g.width(), drawY + g.height());
            WindowCoordinates d = new WindowCoordinates(drawX, drawY + g.height());
            
            final Supplier<Character> character = characters[i]::getCharacter;
            RenderingUtils.createQuad(a, b, c, d, colour, texture, Boolean.valueOf(false)::booleanValue, Optional.of(() -> texture.getCurrentFrame(character)), List.of(characters[i]::getModelMatrix));
            
            drawX += g.width();
        }
        
        return characters;
    }

    private record Glyph(int width, int height, int x, int y) {}

    private static class CharTexture extends Texture {
        private final Map<Character, Glyph> glyphs;

        // Caller should allocate imageData with MemoryUtil and never free it
        public CharTexture(String textureName, ByteBuffer imageData, int width, int height, Map<Character, Glyph> glyphs) {
            super(textureName, imageData, width, height);
            this.glyphs = glyphs;
        }
        
        public int getCurrentFrame(Supplier<Character> character) {
            return glyphs.get(character.get()).x() * glyphs.size() / this.getWidth();
        }
        
        @Override
        public int count() {
            return glyphs.size();
        }
        
    }
    
    public static class CharRenderer extends RectangularObjectRenderer {
        private char character;

        public CharRenderer(WindowCoordinates coordinates, double width, double height, Texture texture, char character) {
            super(coordinates, width, height, texture);
            
            this.character = character;
        }
        
        public char getCharacter() {
            return character;
        }
        
        public void setCharacter(char character) {
            this.character = character;
        }
    }
}