package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer vanillaFr;
    private final BatchingFontRenderer batchingFr; // может быть null, если не удалось создать

    private IIcon selectedOverlayIcon = null;
    private IIcon highlightOverlayIcon = null;

    public TravelEntitySpecialRenderer() {
        this.vanillaFr = Minecraft.getMinecraft().fontRenderer;
        this.batchingFr = tryCreateBatchingFontRenderer();

        try {
            java.lang.reflect.Field f1 = EnderIO.blockTravelPlatform.getClass().getDeclaredField("selectedOverlayIcon");
            f1.setAccessible(true);
            selectedOverlayIcon = (IIcon) f1.get(EnderIO.blockTravelPlatform);

            java.lang.reflect.Field f2 = EnderIO.blockTravelPlatform.getClass().getDeclaredField("highlightOverlayIcon");
            f2.setAccessible(true);
            highlightOverlayIcon = (IIcon) f2.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при получении иконок через reflection", t);
        }
    }

    private BatchingFontRenderer tryCreateBatchingFontRenderer() {
        try {
            LOG.info("[TravelAnchorFix] Пытаемся создать BatchingFontRenderer...");
            BatchingFontRenderer fr = new BatchingFontRenderer(
                    Minecraft.getMinecraft().fontRenderer,
                    null, null, null, null, null
            );
            LOG.info("[TravelAnchorFix] Успешно создан BatchingFontRenderer");
            return fr;
        } catch (Throwable t) {
            LOG.warn("[TravelAnchorFix] Не удалось создать BatchingFontRenderer, fallback на Vanilla FontRenderer", t);
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) return;

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String text = anchor.getLabel();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        LOG.debug("[TravelAnchorFix] Рендер якоря '{}' в {},{},{}", text, x, y, z);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        int width;
        if (batchingFr != null) {
            width = batchingFr.getStringWidth(text);
        } else {
            width = vanillaFr.getStringWidth(text);
        }
        int half = width / 2;

        // фон под текст
        RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

        // сам текст
        if (batchingFr != null) {
            batchingFr.drawString(text, -half, 0, 0xFFFFFFFF, false, false, null, 0, 0);
            LOG.debug("[TravelAnchorFix] Текст '{}' отрендерен через BatchingFontRenderer", text);
        } else {
            vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
            LOG.debug("[TravelAnchorFix] Текст '{}' отрендерен через Vanilla FontRenderer", text);
        }

        GL11.glPopMatrix();
    }

    private IIcon getSelectedOverlayIcon() {
        LOG.debug("[TravelAnchorFix] getSelectedOverlayIcon() -> {}", selectedOverlayIcon);
        return selectedOverlayIcon;
    }

    private IIcon getHighlightOverlayIcon() {
        LOG.debug("[TravelAnchorFix] getHighlightOverlayIcon() -> {}", highlightOverlayIcon);
        return highlightOverlayIcon;
    }
}
