package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.RenderUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

@SideOnly(Side.CLIENT)
public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static IIcon getSelectedOverlayIcon() {
        try {
            java.lang.reflect.Field f = crazypants.enderio.teleport.anchor.BlockTravelAnchor.class
                .getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static IIcon getHighlightOverlayIcon() {
        try {
            java.lang.reflect.Field f = crazypants.enderio.teleport.anchor.BlockTravelAnchor.class
                .getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private final IIcon selectedOverlay = getSelectedOverlayIcon();
    private final IIcon highlightOverlay = getHighlightOverlayIcon();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        TravelController tc = TravelController.instance;

        if (player == null) {
            return;
        }

        // рендерим название якоря
        String text = anchor.getLabel();
        if (text != null && !text.isEmpty()) {
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-0.025F, -0.025F, 0.025F);

            FontRenderer fr = mc.fontRenderer;
            int width = fr.getStringWidth(text) / 2;

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            // фон
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            RenderUtil.renderQuad2D(-width - 2, -2, 0, fr.getStringWidth(text) + 4, 10, 0x80000000);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);

            // текст
            fr.drawString(text, -width, 0, 0xFFFFFF);

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }
}
