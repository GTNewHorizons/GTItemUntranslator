package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.teleport.anchor.BlockTravelAnchor;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import com.mrteroblaze.travelanchorfix.TravelAnchorFix;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer vanillaFr;
    private final BatchingFontRenderer batchingFr;

    public TravelEntitySpecialRenderer() {
        this.vanillaFr = Minecraft.getMinecraft().fontRenderer;
        this.batchingFr = tryCreateBatchingFontRenderer();
    }

    private BatchingFontRenderer tryCreateBatchingFontRenderer() {
        try {
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            if (fr instanceof com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor) {
                com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor acc =
                        (com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor) fr;
                BatchingFontRenderer bfr = acc.angelica$getBatcher();
                if (bfr != null) {
                    LOG.info("[TravelAnchorFix] Успешно получили BatchingFontRenderer из Angelica");
                    return bfr;
                }
            }
            LOG.warn("[TravelAnchorFix] BatchingFontRenderer недоступен, fallback на FontRenderer");
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при создании BatchingFontRenderer, используем fallback", t);
        }
        return null;
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

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.016F, -0.016F, 0.016F);

        int width = vanillaFr.getStringWidth(text);
        int half = width / 2;

        // Фон под текст
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.5F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-half - 2, -2, 0.0F);
        GL11.glVertex3f(-half - 2, 9, 0.0F);
        GL11.glVertex3f(half + 2, 9, 0.0F);
        GL11.glVertex3f(half + 2, -2, 0.0F);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Сам текст
        if (batchingFr != null) {
            batchingFr.drawString(text, -half, 0, 0xFFFFFFFF, false);
        } else {
            vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
        }

        GL11.glPopMatrix();
    }

    private ResourceLocation getSelectedOverlayIcon() {
        ResourceLocation icon = BlockTravelAnchor.selectedOverlayIcon;
        LOG.debug("[TravelAnchorFix] getSelectedOverlayIcon -> {}", (icon != null ? icon.toString() : "null"));
        return icon;
    }

    private ResourceLocation getHighlightOverlayIcon() {
        ResourceLocation icon = BlockTravelAnchor.highlightOverlayIcon;
        LOG.debug("[TravelAnchorFix] getHighlightOverlayIcon -> {}", (icon != null ? icon.toString() : "null"));
        return icon;
    }
}
