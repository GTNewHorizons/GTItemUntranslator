package com.mrteroblaze.travelanchorfix.client.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.machine.travel.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("enderio", "textures/blocks/blockTravelAnchor.png");

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        if (player == null) return;

        // Условие показа — либо держит Staff, либо стоит на якоре
        boolean holdingStaff = mc.thePlayer.getHeldItem() != null &&
                mc.thePlayer.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("travelstaff");
        boolean standingOnAnchor = Math.floor(player.posX) == te.xCoord &&
                                   Math.floor(player.posY - 1) == te.yCoord &&
                                   Math.floor(player.posZ) == te.zCoord;

        if (!holdingStaff && !standingOnAnchor) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        String name = anchor.getLabel();
        if (name != null && !name.isEmpty()) {
            FontRenderer fr = mc.fontRenderer;
            int width = fr.getStringWidth(name) / 2;
            fr.drawString(name, -width, 0, 0xFFFFFF);
        }

        GL11.glPopMatrix();
    }
}
