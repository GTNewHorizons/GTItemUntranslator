package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final BatchingFontRenderer batchingFr;
    private final FontRenderer vanillaFr;

    public TravelEntitySpecialRenderer() {
        BatchingFontRenderer tmp = null;
        try {
            tmp = new BatchingFontRenderer(
                Minecraft.getMinecraft().fontRenderer,
                new net.minecraft.util.ResourceLocation[0],
                new int[0],
                new byte[0],
                new int[0],
                null
            );
            LOG.info("[TravelAnchorFix] ✅ Успешно создан BatchingFontRenderer");
        } catch (Throwable t) {
            LOG.warn("[TravelAnchorFix] ⚠ Не удалось создать BatchingFontRenderer, используем ванильный FontRenderer", t);
        }
        this.batchingFr = tmp;
        this.vanillaFr = Minecraft.getMinecraft().fontRenderer;
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String text = anchor.getLabel();
        if (text == null || text.isEmpty()) {
            return;
        }

        LOG.debug("[TravelAnchorFix] Рендер якоря '{}' на координатах ({}, {}, {})", text, x, y, z);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        int width;
        if (batchingFr != null) {
            width = batchingFr.getStringWidth((CharSequence) text, 0, text.length());
            LOG.debug("[TravelAnchorFix] Используется BatchingFontRenderer. Ширина текста '{}' = {}", text, width);
        } else {
            width = vanillaFr.getStringWidth(text);
            LOG.debug("[TravelAnchorFix] Используется ванильный FontRenderer. Ширина текста '{}' = {}", text, width);
        }

        int half = width / 2;

        // Фон под текст
        RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

        // Сам текст
        if (batchingFr != null) {
            batchingFr.drawString(text, -half, 0, 0xFFFFFFFF, false, false, 0, text.length());
        } else {
            vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
        }

        GL11.glPopMatrix();
    }

    // --- Отладка overlay иконок ---
    private Object getSelectedOverlayIcon() {
        try {
            java.lang.reflect.Field f = crazypants.enderio.teleport.anchor.BlockTravelAnchor.class.getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            Object icon = f.get(null);
            LOG.debug("[TravelAnchorFix] getSelectedOverlayIcon() = {}", icon);
            return icon;
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Ошибка при доступе к selectedOverlayIcon", e);
            return null;
        }
    }

    private Object getHighlightOverlayIcon() {
        try {
            java.lang.reflect.Field f = crazypants.enderio.teleport.anchor.BlockTravelAnchor.class.getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            Object icon = f.get(null);
            LOG.debug("[TravelAnchorFix] getHighlightOverlayIcon() = {}", icon);
            return icon;
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Ошибка при доступе к highlightOverlayIcon", e);
            return null;
        }
    }
}
