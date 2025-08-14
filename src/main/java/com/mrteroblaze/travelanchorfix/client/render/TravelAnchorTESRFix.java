package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;

import org.lwjgl.opengl.GL11;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.config.Config;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) return;

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String name = anchor.getLabel();
        if (name == null || name.trim().isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        double distSq = mc.thePlayer.getDistanceSq(te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5);
        if (distSq > Config.travelAnchorMaxDistanceSq) return;

        // Проверяем, что наведено именно на этот якорь
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.blockX != te.xCoord || mop.blockY != te.yCoord || mop.blockZ != te.zCoord) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.3, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;
        int width = fontrenderer.getStringWidth(name) / 2;
        fontrenderer.drawString(EnumChatFormatting.AQUA + name, -width, 0, 0xFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
