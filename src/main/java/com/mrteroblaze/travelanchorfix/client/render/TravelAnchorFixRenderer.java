package com.mrteroblaze.travelanchorfix.client.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

@SideOnly(Side.CLIENT)
public class TravelAnchorFixRenderer extends TileEntitySpecialRenderer {

    // Параметры отображения
    private static final float LABEL_Y_OFFSET = 1.2f;
    private static final float SCALE = 0.025f; // базовый масштабирующий множитель
    private static final float BG_R = 0f, BG_G = 0f, BG_B = 0f, BG_A = 0.40f;
    private static final int TEXT_COLOR = 0xFFFFFFFF; // белый (ARGB)
    private static final int SHADOW_COLOR = 0x80000000; // чёрный с ~50% альфой

    private BatchingFontRenderer batched; // ленивый батчер из Angelica

    private BatchingFontRenderer ensureBatched() {
        if (batched != null) return batched;
        if (!Loader.isModLoaded("angelica")) return null;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            FontRenderer fr = mc.fontRenderer;

            // Достаём нужные поля FontRenderer (1.7.10) через рефлексию
            Field fGlyphWidth = FontRenderer.class.getDeclaredField("glyphWidth");
            fGlyphWidth.setAccessible(true);
            byte[] glyphWidth = (byte[]) fGlyphWidth.get(fr);

            Field fCharWidth = FontRenderer.class.getDeclaredField("charWidth");
            fCharWidth.setAccessible(true);
            int[] charWidth = (int[]) fCharWidth.get(fr);

            Field fColorCode = FontRenderer.class.getDeclaredField("colorCode");
            fColorCode.setAccessible(true);
            int[] colorCode = (int[]) fColorCode.get(fr);

            Field fUniPages = FontRenderer.class.getDeclaredField("unicodePageLocations");
            fUniPages.setAccessible(true);
            ResourceLocation[] unicodePages = (ResourceLocation[]) fUniPages.get(fr);

            Field fLoc = FontRenderer.class.getDeclaredField("locationFontTexture");
            fLoc.setAccessible(true);
            ResourceLocation asciiTex = (ResourceLocation) fLoc.get(fr);

            // Сигнатура из твоей ошибки: (FontRenderer, ResourceLocation[], int[], byte[], int[], ResourceLocation)
            batched = new BatchingFontRenderer(fr, unicodePages, charWidth, glyphWidth, colorCode, asciiTex);
            return batched;
        } catch (Throwable t) {
            // Если что-то не так — просто работаем без Angelica батчера
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) return;
        TileTravelAnchor anchor = (TileTravelAnchor) te;

        if (!anchor.isVisible()) return;

        // Пока оставляем заглушку, как ты просил
        String label = "Travel Anchor";
        if (label == null || label.trim()
            .isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;
        BatchingFontRenderer bfr = ensureBatched();

        GL11.glPushMatrix();
        try {
            // Центр блока
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

            // Биллбординг к камере
            RenderManager rm = RenderManager.instance;
            GL11.glRotatef(-rm.playerViewY, 0F, 1F, 0F);
            GL11.glRotatef(rm.playerViewX, 1F, 0F, 0F);

            // Смещение вверх над блоком
            GL11.glTranslatef(0f, LABEL_Y_OFFSET, 0f);

            // Масштабируем под пиксели шрифта
            float scale = SCALE * fr.FONT_HEIGHT;
            GL11.glScalef(scale, scale, scale);

            // Ширина текста (через batched если доступен — рефлексией)
            int textW = getStringWidthReflective(bfr, fr, label);
            int textH = fr.FONT_HEIGHT;

            // Центрируем по X
            GL11.glTranslatef(-textW / 2.0f, 0f, 0f);

            // Фон
            drawBackgroundQuad(anchor, textW, textH);

            // Обязательный бинд текстуры шрифта — это ключ к фиксу «квадратов»
            bindFontTextureSafe(fr, mc);

            // Тень и основной текст
            drawStringReflective(bfr, fr, label, SHADOW_COLOR, 1, 1);
            drawStringReflective(bfr, fr, label, TEXT_COLOR, 0, 0);

        } finally {
            GL11.glPopMatrix();
        }
    }

    // ---------- утилиты рисования ----------

    private void drawBackgroundQuad(TileTravelAnchor anchor, int textW, int textH) {
        float alpha = BG_A;
        try {
            if (TravelController.instance.isBlockSelected(new com.enderio.core.common.util.BlockCoord(anchor))) {
                alpha = Math.min(0.85f, BG_A + 0.30f);
            }
        } catch (Throwable ignored) {}

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(BG_R, BG_G, BG_B, alpha);

        final float padX = 2f, padY = 1f;
        float x0 = -padX, y0 = -padY, x1 = textW + padX, y1 = textH + padY;

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertex(x0, y0, 0);
        t.addVertex(x0, y1, 0);
        t.addVertex(x1, y1, 0);
        t.addVertex(x1, y0, 0);
        t.draw();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f); // reset
    }

    private void bindFontTextureSafe(FontRenderer fr, Minecraft mc) {
        try {
            Field fLoc = FontRenderer.class.getDeclaredField("locationFontTexture");
            fLoc.setAccessible(true);
            ResourceLocation loc = (ResourceLocation) fLoc.get(fr);
            if (loc != null) {
                mc.getTextureManager()
                    .bindTexture(loc);
                return;
            }
        } catch (Throwable ignored) {}
        // fallback — обычно drawString сам биндит, но лучше явно
    }

    private int getStringWidthReflective(BatchingFontRenderer bfr, FontRenderer fr, String s) {
        if (bfr != null) {
            try {
                Method m = bfr.getClass()
                    .getMethod("getStringWidth", String.class);
                Object res = m.invoke(bfr, s);
                if (res instanceof Integer) return (Integer) res;
            } catch (Throwable ignored) {}
        }
        return fr.getStringWidth(s);
    }

    private void drawStringReflective(BatchingFontRenderer bfr, FontRenderer fr, String s, int color, int offX,
        int offY) {
        // Пробуем batched: (String,float,float,int,boolean) или (String,int,int,int,boolean) или (String,int,int,int)
        if (bfr != null) {
            try {
                Method m = bfr.getClass()
                    .getMethod("drawString", String.class, float.class, float.class, int.class, boolean.class);
                m.invoke(bfr, s, (float) offX, (float) offY, color, false);
                return;
            } catch (Throwable ignored) {}
            try {
                Method m = bfr.getClass()
                    .getMethod("drawString", String.class, int.class, int.class, int.class, boolean.class);
                m.invoke(bfr, s, offX, offY, color, false);
                return;
            } catch (Throwable ignored) {}
            try {
                Method m = bfr.getClass()
                    .getMethod("drawString", String.class, int.class, int.class, int.class);
                m.invoke(bfr, s, offX, offY, color);
                return;
            } catch (Throwable ignored) {}
        }
        // Fallback — ваниль
        if (offX != 0 || offY != 0) {
            fr.drawString(s, offX, offY, color, false);
        } else {
            fr.drawString(s, 0, 0, color, false);
        }
    }
}
