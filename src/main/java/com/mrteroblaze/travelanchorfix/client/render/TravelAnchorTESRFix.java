package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.TileTravelAnchor;
import crazypants.enderio.teleport.TravelController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    private static final ResourceLocation BLOCK_TEXTURE = new ResourceLocation("enderio", "textures/blocks/travelAnchor.png");

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof TileTravelAnchor)) return;

        TileTravelAnchor anchor = (TileTravelAnchor) tile;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        if (!TravelController.instance.isAnchorVisibleToPlayer(anchor, player)) return;

        String name = anchor.getName();
        if (name == null || name.trim().isEmpty()) return;

        double distSq = player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5);
        if (distSq > TravelController.instance.getMaxDistanceSq(player)) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.renderViewEntity.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.renderViewEntity.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.016F, -0.016F, 0.016F);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        FontRenderer fontrenderer = this.getFontRenderer();
        int strWidth = fontrenderer.getStringWidth(name) / 2;

        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tessellator.addVertex(-strWidth - 1, -1, 0.0D);
        tessellator.addVertex(-strWidth - 1, 8, 0.0D);
        tessellator.addVertex(strWidth + 1, 8, 0.0D);
        tessellator.addVertex(strWidth + 1, -1, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        fontrenderer.drawString(name, -strWidth, 0, 0x20FFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        fontrenderer.drawString(name, -strWidth, 0, 0xFFFFFFFF);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }
}
