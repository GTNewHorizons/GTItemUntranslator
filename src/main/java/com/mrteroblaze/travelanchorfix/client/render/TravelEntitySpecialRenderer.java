package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.common.vecmath.Vector3f;
import com.enderio.core.common.vecmath.Vector4f;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

import java.lang.reflect.Field;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer fr;
    private final BatchingFontRenderer batchingFont;

    private static Field selectedOverlayIconField;
    private static Field highlightOverlayIconField;

    static {
        try {
            Class<?> clazz = Class.forName("crazypants.enderio.teleport.anchor.BlockTravelAnchor");
            selectedOverlayIconField = clazz.getDeclaredField("selectedOverlayIcon");
            selectedOverlayIconField.setAccessible(true);
            highlightOverlayIconField = clazz.getDeclaredField("highlightOverlayIcon");
            highlightOverlayIconField.setAccessible(true);
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Не удалось получить ссылки на иконки через рефлексию", e);
        }
    }

    public TravelEntitySpecialRenderer() {
        this.fr = Minecraft.getMinecraft().fontRenderer;

        BatchingFontRenderer tmp = null;
        try {
            tmp = BatchingFontRenderer.INSTANCE;
            LOG.info("[TravelAnchorFix] Успешно получили BatchingFontRenderer");
        } catch (Throwable t) {
            LOG.warn("[TravelAnchorFix] Не удалось создать BatchingFontRenderer, используем ванильный FontRenderer", t);
        }
        this.batchingFont = tmp;
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        LOG.info("[TravelAnchorFix] renderTileEntityAt anchor={} at {}, {}, {}", anchor.getLabel(), x, y, z);

        renderBlockOverlay(anchor, x, y, z);
        renderLabelAlways(anchor, x, y, z);
    }

    private void renderBlockOverlay(TileTravelAnchor anchor, double x, double y, double z) {
        IIcon icon = getSelectedOverlayIcon();
        if (icon == null) {
            LOG.debug("[TravelAnchorFix] Нет иконки для overlay");
            return;
        }

        BoundingBox bb = new BoundingBox(anchor.xCoord, anchor.yCoord, anchor.zCoord,
                anchor.xCoord + 1, anchor.yCoord + 1, anchor.zCoord + 1);

        RenderHelper.disableStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        RenderUtil.renderBoundingBox(bb, new Vector4f(0f, 1f, 0f, 0.4f));

        GL11.glPopMatrix();
        RenderHelper.enableStandardItemLighting();
    }

    private void renderLabelAlways(TileEntity te, double x, double y, double z) {
        if (!(te instanceof TileTravelAnchor)) return;
        String label = ((TileTravelAnchor) te).getLabel();
        if (label == null || label.isEmpty()) return;

        LOG.info("[TravelAnchorFix] Rendering label '{}' at {}, {}, {}", label, x, y, z);

        int textW = fr.getStringWidth(label);
        int half = textW / 2;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);

        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F);

        float scale = 0.016666668F * 1.6F;
        GL11.glScalef(-scale, -scale, scale);
        GL11.glTranslatef(-half, 0, 0);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        drawLabel(label, true);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        drawLabel(label, false);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private void drawLabel(String text, boolean shadow) {
        if (batchingFont != null) {
            LOG.info("[TravelAnchorFix] Drawing with Angelica BatchingFontRenderer: '{}'", text);
            batchingFont.drawString(0, 0, 0xFFFFFFFF, shadow, false, text, 0, 0);
        } else {
            LOG.info("[TravelAnchorFix] Drawing with vanilla FontRenderer: '{}'", text);
            fr.drawString(text, 0, 0, 0xFFFFFFFF, shadow);
        }
    }

    private IIcon getSelectedOverlayIcon() {
        try {
            return (IIcon) selectedOverlayIconField.get(EnderIO.blockTravelPlatform);
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Не удалось получить selectedOverlayIcon", e);
            return null;
        }
    }

    private IIcon getHighlightOverlayIcon() {
        try {
            return (IIcon) highlightOverlayIconField.get(EnderIO.blockTravelPlatform);
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Не удалось получить highlightOverlayIcon", e);
            return null;
        }
    }
}
