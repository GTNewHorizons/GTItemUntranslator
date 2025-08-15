package com.mrteroblaze.travelanchorfix.client.render;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.teleport.TravelController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class TravelAnchorFixRenderer extends TileEntitySpecialRenderer {

    // Настройки вида
    private static final float LABEL_Y_OFFSET = 1.2f;
    private static final float SCALE = 0.025f; // базовый множитель масштаба

    // Цвета (RGBA 0..1)
    private static final float BG_R = 0f, BG_G = 0f, BG_B = 0f, BG_A = 0.4f;
    private static final int TEXT_COLOR = 0xFFFFFFFF;  // белый
    private static final int SHADOW_COLOR = 0x80000000; // полупрозрачная чёрная тень

    private BatchingFontRenderer batched; // лениво инициализируемый рендерер из Angelica

    private BatchingFontRenderer ensureBatched() {
    if (batched != null) return batched;
    if (!Loader.isModLoaded("angelica")) return null;
    try {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;

        // glyphWidth
        Field fGlyphWidth = FontRenderer.class.getDeclaredField("glyphWidth");
        fGlyphWidth.setAccessible(true);
        byte[] glyphWidth = (byte[]) fGlyphWidth.get(fr);

        // charWidth
        Field fCharWidth = FontRenderer.class.getDeclaredField("charWidth");
        fCharWidth.setAccessible(true);
        int[] charWidth = (int[]) fCharWidth.get(fr);

        // colorCode
        Field fColorCode = FontRenderer.class.getDeclaredField("colorCode");
        fColorCode.setAccessible(true);
        int[] colorCode = (int[]) fColorCode.get(fr);

        // unicodePageLocations
        Field fUniPages = FontRenderer.class.getDeclaredField("unicodePageLocations");
        fUniPages.setAccessible(true);
        ResourceLocation[] unicodePages = (ResourceLocation[]) fUniPages.get(fr);

        // locationFontTexture (asciiTex)
        Field fLoc = FontRenderer.class.getDeclaredField("locationFontTexture");
        fLoc.setAccessible(true);
        ResourceLocation fontTex = (ResourceLocation) fLoc.get(fr);

        batched = new BatchingFontRenderer(fr, unicodePages, charWidth, glyphWidth, colorCode, fontTex);
        return batched;
    } catch (Throwable t) {
        return null;
    }
}

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) return;
        TileTravelAnchor anchor = (TileTravelAnchor) te;

        if (!anchor.isVisible()) return;

        final String label = anchor.getLabel();
        if (label == null || label.trim().isEmpty()) return;

        // billboard-поворот к камере
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

            RenderManager rm = RenderManager.instance;
            GL11.glRotatef(-rm.playerViewY, 0F, 1F, 0F);
            GL11.glRotatef(rm.playerViewX, 1F, 0F, 0F);

            // смещение над блоком
            GL11.glTranslatef(0f, LABEL_Y_OFFSET, 0f);

            // масштабируем относительно высоты шрифта
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            float scale = SCALE * fr.FONT_HEIGHT;
            GL11.glScalef(scale, scale, scale);

            drawLabel(fr, label, anchor);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private void drawLabel(FontRenderer fr, String text, TileTravelAnchor anchor) {
        final Minecraft mc = Minecraft.getMinecraft();

        // Ширина текста (проверяем batched сначала)
        BatchingFontRenderer b = ensureBatched();
        final int textW = (b != null ? b.getStringWidth(text) : fr.getStringWidth(text));
        final int textH = fr.FONT_HEIGHT;

        // Центрируем по X
        GL11.glTranslatef(-textW / 2.0f, 0f, 0f);

        // Фон (прямоугольник с альфой)
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        // если выбранный якорь — усилим альфу фона
        float alpha = BG_A;
        try {
            if (TravelController.instance.isBlockSelected(new crazypants.enderio.teleport.anchor.BlockCoord(anchor))) {
                alpha = Math.min(0.8f, BG_A + 0.3f);
            }
        } catch (Throwable ignore) {}
        GL11.glColor4f(BG_R, BG_G, BG_B, alpha);

        final float padX = 2f, padY = 1f;
        float x0 = -padX, y0 = -padY, x1 = textW + padX, y1 = textH + padY;
        Tessellator tes = Tessellator.instance;
        tes.startDrawingQuads();
        tes.addVertex(x0, y0, 0);
        tes.addVertex(x0, y1, 0);
        tes.addVertex(x1, y1, 0);
        tes.addVertex(x1, y0, 0);
        tes.draw();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Принудительно биндим текстуру шрифта (антиквадраты)
        try {
            Field fLoc = FontRenderer.class.getDeclaredField("locationFontTexture");
            fLoc.setAccessible(true);
            ResourceLocation fontTex = (ResourceLocation) fLoc.get(fr);
            if (fontTex != null) {
                mc.getTextureManager().bindTexture(fontTex);
            }
        } catch (Throwable ignore) {
            // если вдруг не удалось — ваниль сама обычно биндим при drawString, но биндинг тут желателен
        }

        // Рисуем текст (batched → ваниль)
        if (b != null) {
            // тень
            b.drawString(text, 0.5f, 0.5f, SHADOW_COLOR, false);
            // основной
            b.drawString(text, 0f, 0f, TEXT_COLOR, false);
        } else {
            // тень
            fr.drawString(text, 1, 1, SHADOW_COLOR, false);
            // основной
            fr.drawString(text, 0, 0, TEXT_COLOR, false);
        }
    }
}
