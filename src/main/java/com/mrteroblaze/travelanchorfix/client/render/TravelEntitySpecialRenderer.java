package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

import java.lang.reflect.Field;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private BatchingFontRenderer bfr = null;

    private static Object selectedOverlayIcon;
    private static Object highlightOverlayIcon;

    static {
        try {
            Class<?> clazz = EnderIO.blockTravelPlatform.getClass();
            Field f1 = clazz.getDeclaredField("selectedOverlayIcon");
            f1.setAccessible(true);
            selectedOverlayIcon = f1.get(EnderIO.blockTravelPlatform);

            Field f2 = clazz.getDeclaredField("highlightOverlayIcon");
            f2.setAccessible(true);
            highlightOverlayIcon = f2.get(EnderIO.blockTravelPlatform);

            LOG.info("[TravelAnchorFix] Successfully hooked overlay icons via reflection.");
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Failed to hook overlay icons", e);
        }
    }

    public TravelEntitySpecialRenderer() {
        super();
        try {
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            // Упрощённый конструктор – твоя версия Angelica может требовать больше аргументов
            // Если так – вернись к тому месту, где мы создавали bfr через все массивы.
            bfr = new BatchingFontRenderer(fr, null, null, null, null, (ResourceLocation) null);
            LOG.info("[TravelAnchorFix] BatchingFontRenderer successfully created!");
        } catch (Throwable t) {
            LOG.warn("[TravelAnchorFix] Failed to init BatchingFontRenderer, will fallback to default FontRenderer", t);
            bfr = null;
        }
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
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        int width = fr.getStringWidth(text);
        int half = width / 2;

        GL11.glTranslatef(-half, 0, 0);

        if (bfr != null) {
            LOG.info("[TravelAnchorFix] Drawing with BatchingFontRenderer: '{}'", text);
            bfr.drawString(text, 0, 0, 0xFFFFFFFF, false, false, null, 0, 0);
        } else {
            LOG.info("[TravelAnchorFix] Drawing with default FontRenderer: '{}'", text);
            fr.drawString(text, 0, 0, 0xFFFFFFFF);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();
    }
}
