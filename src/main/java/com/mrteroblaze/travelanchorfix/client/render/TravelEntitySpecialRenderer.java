package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final ResourceLocation TEX = new ResourceLocation("enderio", "textures/blocks/TravelAnchor.png");

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }
        TileTravelAnchor anchor = (TileTravelAnchor) te;

        TravelController tc = TravelController.instance;

        String label = anchor.getLabel();
        if (label == null || label.isEmpty()) {
            return;
        }

        if (!tc.canSeeBlock(mc.thePlayer, anchor.xCoord, anchor.yCoord, anchor.zCoord)) {
            return;
        }

        double distSq = mc.thePlayer.getDistanceSq(anchor.xCoord + 0.5, anchor.yCoord + 0.5, anchor.zCoord + 0.5);
        if (distSq > TravelController.instance.getMaxDistanceSq()) {
            return;
        }

        renderLabel(anchor, x, y, z, label);
    }

    private void renderLabel(TileTravelAnchor anchor, double x, double y, double z, String text) {
        EntityPlayer player = mc.thePlayer;

        FontRenderer fr = mc.fontRenderer;
        float scale = 0.016666668F * 1.6F;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        Tessellator tess = Tessellator.instance;
        int textW = fr.getStringWidth(text);
        int halfW = textW / 2;

        // фон под текст
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tess.startDrawingQuads();
        tess.setColorRGBA_F(0, 0, 0, 0.25F);
        tess.addVertex(-halfW - 2, -2, 0.0D);
        tess.addVertex(-halfW - 2, 9, 0.0D);
        tess.addVertex(halfW + 2, 9, 0.0D);
        tess.addVertex(halfW + 2, -2, 0.0D);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // сам текст
        fr.drawString(text, -halfW, 0, 0xFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }

    private IIcon getOverlayIcon(boolean selected) {
        try {
            if (selected) {
                java.lang.reflect.Field f = EnderIO.blockTravelPlatform.getClass().getDeclaredField("selectedOverlayIcon");
                f.setAccessible(true);
                return (IIcon) f.get(EnderIO.blockTravelPlatform);
            } else {
                java.lang.reflect.Field f = EnderIO.blockTravelPlatform.getClass().getDeclaredField("highlightOverlayIcon");
                f.setAccessible(true);
                return (IIcon) f.get(EnderIO.blockTravelPlatform);
            }
        } catch (Exception e) {
            return null;
        }
    }
}
