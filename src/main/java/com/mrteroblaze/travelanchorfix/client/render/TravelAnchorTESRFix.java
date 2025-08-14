package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.TravelSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    private static final int MAX_DISTANCE = 512; // из настроек мода

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String name = anchor.getLabel();
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        double distSq = player.getDistanceSq(te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5);
        if (distSq > MAX_DISTANCE * MAX_DISTANCE) {
            return;
        }

        // Условия показа имени:
        boolean showLabel = false;

        // 1. Стоим на якоре
        if (Math.floor(player.posX) == te.xCoord &&
            Math.floor(player.posY - 1) == te.yCoord &&
            Math.floor(player.posZ) == te.zCoord) {
            showLabel = true;
        }

        // 2. В руке Staff of Traveling
        ItemStack held = player.getCurrentEquippedItem();
        if (held != null && held.getUnlocalizedName().toLowerCase().contains("travelstaff")) {
            showLabel = true;
        }

        // 3. Если наводимся мышкой — телепортация
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop != null && mop.blockX == te.xCoord && mop.blockY == te.yCoord && mop.blockZ == te.zCoord) {
            showLabel = true;
        }

        if (!showLabel) {
            return;
        }

        // Если игрок нажал Shift и целится на якорь — телепортируем
        if (player.isSneaking() && mop != null &&
            mop.blockX == te.xCoord && mop.blockY == te.yCoord && mop.blockZ == te.zCoord) {
            TravelController.instance.travelToLocation(player, anchor, TravelSource.BLOCK);
        }

        // Рендер имени
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        FontRenderer fontrenderer = this.func_147498_b();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        fontrenderer.drawString(EnumChatFormatting.AQUA + name, -fontrenderer.getStringWidth(name) / 2, 0, 0xFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
