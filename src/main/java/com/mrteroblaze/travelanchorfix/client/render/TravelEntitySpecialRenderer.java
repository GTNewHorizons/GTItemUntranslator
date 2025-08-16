package com.mrteroblaze.travelanchorfix.client.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.BlockTravelAnchor;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.teleport.TravelController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static IIcon getSelectedOverlayIcon() {
        try {
            java.lang.reflect.Field f = BlockTravelAnchor.class.getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static IIcon getHighlightOverlayIcon() {
        try {
            java.lang.reflect.Field f = BlockTravelAnchor.class.getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private final Minecraft mc = Minecraft.getMinecraft();
    private final RenderManager rm = RenderManager.instance;
    private final TravelController tc = TravelController.instance;

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        EntityPlayer player = mc.thePlayer;

        if (player == null || !tc.isTravelAnchor(anchor)) {
            return;
        }

        if (!tc.canBlockBeAccessed(mc.theWorld, anchor.xCoord, anchor.yCoord, anchor.zCoord, player)) {
            return;
        }

        String name = anchor.getLabel();
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        GL11.glRotatef(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);

        GL11.glScalef(-0.016F * 1.6F, -0.016F * 1.6F, 0.016F * 1.6F);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        FontRenderer fr = mc.fontRenderer;
        int textWidth = fr.getStringWidth(name) / 2;

        // фон
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.25F);
        GL11.glVertex3f(-textWidth - 2, -2, 0.0F);
        GL11.glVertex3f(-textWidth - 2, 9, 0.0F);
        GL11.glVertex3f(textWidth + 2, 9, 0.0F);
        GL11.glVertex3f(textWidth + 2, -2, 0.0F);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // текст
        fr.drawStringWithShadow(name, -textWidth, 0, 0xFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();
    }
}
