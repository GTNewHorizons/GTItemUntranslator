package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private static Object overlaySelected = null;
    private static Object overlayHighlight = null;

    private final FontRenderer fr;
    private final BatchingFontRenderer bfr;

    public TravelEntitySpecialRenderer() {
        fr = Minecraft.getMinecraft().fontRenderer;
        BatchingFontRenderer tmp = null;
        try {
            // Читаем приватные поля FontRenderer для конструктора BatchingFontRenderer
            Field unicodePagesF = FontRenderer.class.getDeclaredField("unicodePageLocations");
            unicodePagesF.setAccessible(true);
            ResourceLocation[] unicodePages = (ResourceLocation[]) unicodePagesF.get(fr);

            Field charWidthF = FontRenderer.class.getDeclaredField("charWidth");
            charWidthF.setAccessible(true);
            int[] charWidth = (int[]) charWidthF.get(fr);

            Field glyphWidthF = FontRenderer.class.getDeclaredField("glyphWidth");
            glyphWidthF.setAccessible(true);
            byte[] glyphWidth = (byte[]) glyphWidthF.get(fr);

            Field colorCodeF = FontRenderer.class.getDeclaredField("colorCode");
            colorCodeF.setAccessible(true);
            int[] colorCode = (int[]) colorCodeF.get(fr);

            Field fontTexF = FontRenderer.class.getDeclaredField("locationFontTexture");
            fontTexF.setAccessible(true);
            ResourceLocation fontTex = (ResourceLocation) fontTexF.get(fr);

            tmp = new BatchingFontRenderer(fr, unicodePages, charWidth, glyphWidth, colorCode, fontTex);
            LOG.info("[TravelAnchorFix] BatchingFontRenderer создан успешно");
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при создании BatchingFontRenderer, используем fallback", t);
        }
        bfr = tmp;

        try {
            Field f1 = EnderIO.blockTravelPlatform.getClass().getDeclaredField("selectedOverlayIcon");
            f1.setAccessible(true);
            overlaySelected = f1.get(EnderIO.blockTravelPlatform);

            Field f2 = EnderIO.blockTravelPlatform.getClass().getDeclaredField("highlightOverlayIcon");
            f2.setAccessible(true);
            overlayHighlight = f2.get(EnderIO.blockTravelPlatform);

            LOG.info("[TravelAnchorFix] Overlay иконки получены успешно через reflection");
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при доступе к overlay иконкам", t);
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }
        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String text = anchor.getLabel();

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        LOG.info("[TravelAnchorFix] Рендер якоря ({}, {}, {}), имя='{}'", te.xCoord, te.yCoord, te.zCoord, text);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        // 1.7.10 API — используем RenderManager.instance
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);

        float scale = 0.016F * 1.6F;
        GL11.glScalef(-scale, -scale, scale);

        int width = (bfr != null ? bfr.getStringWidth(text) : fr.getStringWidth(text));
        LOG.info("[TravelAnchorFix] Ширина строки '{}' = {}", text, width);

        int baseX = -width / 2;

        // Сначала фон (чёрный полупрозрачный)
        if (bfr != null) {
            bfr.drawString(text, baseX + 1, 1, 0x80000000, false, false, text, 0, text.length());
            bfr.drawString(text, baseX, 0, 0xFFFFFFFF, false, false, text, 0, text.length());
            LOG.info("[TravelAnchorFix] Нарисовано через BatchedFontRenderer");
        } else {
            fr.drawString(text, baseX + 1, 1, 0x80000000);
            fr.drawString(text, baseX, 0, 0xFFFFFFFF);
            LOG.info("[TravelAnchorFix] Нарисовано через обычный FontRenderer");
        }

        // DEBUG: красный квадрат чтобы проверить матрицу
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(1.0F, 0.0F, 0.0F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-5, -5, 0);
        GL11.glVertex3f(5, -5, 0);
        GL11.glVertex3f(5, 5, 0);
        GL11.glVertex3f(-5, 5, 0);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();
    }
}
