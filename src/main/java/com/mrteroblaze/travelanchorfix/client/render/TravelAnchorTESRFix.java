package com.mrteroblaze.travelanchorfix.client.render;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    private static final ResourceLocation TRAVEL_ANCHOR_TEXTURE = new ResourceLocation("enderio", "textures/blocks/blockTravelAnchor.png");

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorTESRFix());
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        if (player == null) {
            return;
        }

        boolean holdingStaff = isHoldingTravelStaff(player);
        boolean standingOnAnchor = Math.floor(player.posX) == te.xCoord && Math.floor(player.posY - 1) == te.yCoord && Math.floor(player.posZ) == te.zCoord;

        if (!holdingStaff && !standingOnAnchor) {
            return;
        }

        double distSq = player.getDistanceSq(te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5);
        if (distSq > 512 * 512) { // из конфига travelAnchorMaxDistance = 512
            return;
        }

        renderName(anchor, x, y, z, player, mc);
    }

    private void renderName(TileTravelAnchor anchor, double x, double y, double z, EntityPlayer player, Minecraft mc) {
        String label = anchor.getLabel();
        if (label == null || label.trim().isEmpty()) {
            return;
        }

        FontRenderer fontrenderer = this.func_147498_b();
        float scale = 0.016666668F * 1.6F;

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        int i = fontrenderer.getStringWidth(label) / 2;
        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tessellator.addVertex(-i - 1, -1, 0.0D);
        tessellator.addVertex(-i - 1, 8, 0.0D);
        tessellator.addVertex(i + 1, 8, 0.0D);
        tessellator.addVertex(i + 1, -1, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        fontrenderer.drawString(label, -fontrenderer.getStringWidth(label) / 2, 0, 553648127);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        fontrenderer.drawString(label, -fontrenderer.getStringWidth(label) / 2, 0, -1);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glPopMatrix();
    }

    private boolean isHoldingTravelStaff(EntityPlayer player) {
        ItemStack held = player.getCurrentEquippedItem();
        return held != null && held.getItem() != null && held.getItem().getUnlocalizedName().toLowerCase().contains("travelstaff");
    }
}
