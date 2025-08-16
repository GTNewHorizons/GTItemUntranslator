package com.mrteroblaze.travelanchorfix.client.render;

import com.mrteroblaze.travelanchorfix.TravelAnchorFix;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor;

import crazypants.enderio.EnderIO;
import crazypants.enderio.machine.travel.TileTravelAnchor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final BatchingFontRenderer batchingFr;
    private final FontRenderer vanillaFr;

    private IIcon selectedOverlayIcon;
    private IIcon highlightOverlayIcon;

    public TravelEntitySpecialRenderer() {
        this.vanillaFr = Minecraft.getMinecraft().fontRenderer;
        this.batchingFr = tryCreateBatchingFontRenderer();

        this.selectedOverlayIcon = getSelectedOverlayIcon();
        this.highlightOverlayIcon = getHighlightOverlayIcon();
    }

    private BatchingFontRenderer tryCreateBatchingFontRenderer() {
        try {
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            if (fr instanceof FontRendererAccessor acc) {
                BatchingFontRenderer bfr = acc.angelica$getBatcher();
                LOG.info("[TravelAnchorFix] Успешно получили BatchingFontRenderer из FontRendererAccessor");
                return bfr;
            }
            LOG.warn("[TravelAnchorFix] FontRenderer не является FontRendererAccessor, используем fallback");
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при инициализации BatchingFontRenderer", t);
        }
        return null;
    }

    private IIcon getSelectedOverlayIcon() {
        try {
            Field f = EnderIO.blockTravelPlatform.getClass().getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            IIcon ico = (IIcon) f.get(EnderIO.blockTravelPlatform);
            LOG.info("[TravelAnchorFix] selectedOverlayIcon = {}", ico);
            return ico;
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при получении selectedOverlayIcon", t);
            return null;
        }
    }

    private IIcon getHighlightOverlayIcon() {
        try {
            Field f = EnderIO.blockTravelPlatform.getClass().getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            IIcon ico = (IIcon) f.get(EnderIO.blockTravelPlatform);
            LOG.info("[TravelAnchorFix] highlightOverlayIcon = {}", ico);
            return ico;
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при получении highlightOverlayIcon", t);
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor anchor)) {
            return;
        }

        String text = anchor.getLabel();
        if (text == null || text.isEmpty()) {
            return;
        }

        LOG.debug("[TravelAnchorFix] Render anchor '{}' at {}, {}, {}", text, x, y, z);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
        float scale = 0.016666668F * 1.6F;
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        int width;
        if (batchingFr != null) {
            width = batchingFr.getStringWidth(text);
        } else {
            width = vanillaFr.getStringWidth(text);
        }
        int half = width / 2;

        // фон
        RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

        // текст
        if (batchingFr != null) {
            batchingFr.drawString(text, -half, 0, 0xFFFFFFFF, false);
        } else {
            vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
