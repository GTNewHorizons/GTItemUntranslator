package com.mrteroblaze.travelanchorfix.client.render;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer fr;
    private final BatchingFontRenderer bfr; // может быть null, если не удалось создать

    public TravelEntitySpecialRenderer() {
        this.fr = Minecraft.getMinecraft().fontRenderer;
        this.bfr = tryCreateBatchingFontRenderer(this.fr);
    }

    private static BatchingFontRenderer tryCreateBatchingFontRenderer(FontRenderer fr) {
        try {
            // Поля ванильного FontRenderer (MCP 1.7.10)
            Field fUnicode = FontRenderer.class.getDeclaredField("unicodePageLocations");
            fUnicode.setAccessible(true);
            ResourceLocation[] unicodePages = (ResourceLocation[]) fUnicode.get(fr);

            Field fCharWidth = FontRenderer.class.getDeclaredField("charWidth");
            fCharWidth.setAccessible(true);
            int[] charWidth = (int[]) fCharWidth.get(fr);

            Field fGlyphWidth = FontRenderer.class.getDeclaredField("glyphWidth");
            fGlyphWidth.setAccessible(true);
            byte[] glyphWidth = (byte[]) fGlyphWidth.get(fr);

            Field fColorCode = FontRenderer.class.getDeclaredField("colorCode");
            fColorCode.setAccessible(true);
            int[] colorCode = (int[]) fColorCode.get(fr);

            Field fFontTex = FontRenderer.class.getDeclaredField("locationFontTexture");
            fFontTex.setAccessible(true);
            ResourceLocation fontTex = (ResourceLocation) fFontTex.get(fr);

            BatchingFontRenderer res = new BatchingFontRenderer(
                fr,
                unicodePages,
                charWidth,
                glyphWidth,
                colorCode,
                fontTex);
            LOG.info("[TravelAnchorFix] BatchingFontRenderer создан успешно");
            return res;
        } catch (Throwable t) {
            LOG.warn("[TravelAnchorFix] Не удалось создать BatchingFontRenderer, используем ванильный FontRenderer", t);
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) return;

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String label = anchor.getLabel();
        if (label == null || label.isEmpty()) return;

        // ширину всегда считаем через ванильный FontRenderer
        final int textW = fr.getStringWidth(label);
        final int half = textW / 2;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);

        // Повернуть к камере (MC 1.7.10 API)
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);

        // Масштаб как у табличек-неймплейтов
        float scale = 0.016666668F * 1.6F; // 1/60 * 1.6
        GL11.glScalef(-scale, -scale, scale);

        // Сдвинем так, чтобы текст был по центру
        GL11.glTranslatef(-half, 0, 0);

        // Настройки смешивания/альфы
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        // --- Проход 1: без depth test, чтобы было видно сквозь стены (фон+тень)
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        drawLabel(label, true);

        // --- Проход 2: с depth test, чтобы близко к камере текст был корректно "в мире"
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        drawLabel(label, false);

        // Восстановление
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    /**
     * Рисуем фон и сам текст.
     * 
     * @param behindWallsPass true = рисуем «сквозной» полупрозрачный черный фон + белый текст без depth
     *                        false = рисуем обычный белый текст с depth
     */
    private void drawLabel(String text, boolean behindWallsPass) {
        // фон (чёрный полупрозрачный прямоугольник)
        if (behindWallsPass) {
            // Прямоугольник на 1px шире и на 1px ниже
            int w = fr.getStringWidth(text);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(0f, 0f, 0f, 0.25f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(-2, -2, 0.0f);
            GL11.glVertex3f(-2, 9, 0.0f);
            GL11.glVertex3f(w + 2, 9, 0.0f);
            GL11.glVertex3f(w + 2, -2, 0.0f);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        // Текст: если есть batched — используем его, иначе ваниллу
        if (bfr != null) {
            // Сдвиг для тени (как в ваниле) только в первом проходе
            if (behindWallsPass) {
                // тень/подложка
                bfr.drawString(1f, 1f, 0xFF000000, false, false, text, 0, text.length());
            }
            // основной текст
            bfr.drawString(0f, 0f, 0xFFFFFFFF, false, false, text, 0, text.length());
        } else {
            if (behindWallsPass) {
                fr.drawString(text, 1, 1, 0xFF000000);
            }
            fr.drawString(text, 0, 0, 0xFFFFFFFF);
        }
    }
}
