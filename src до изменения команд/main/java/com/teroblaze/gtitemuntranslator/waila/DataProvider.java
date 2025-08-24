package com.teroblaze.gtitemuntranslator.waila;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.teroblaze.gtitemuntranslator.GTItemUntranslator;
import com.teroblaze.gtitemuntranslator.TooltipEventHandler;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

public class DataProvider implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        if (!GTItemUntranslator.wailaEnabled) return currenttip;

        try {
            TileEntity te = accessor.getTileEntity();
            if (te != null) {
                Block block = accessor.getBlock();
                int x = accessor.getPosition().blockX;
                int y = accessor.getPosition().blockY;
                int z = accessor.getPosition().blockZ;

                // Достаём ItemStack блока так, как будто middle-click (pick block)
                ItemStack pick = block.getPickBlock(
                    new MovingObjectPosition(x, y, z, 1, accessor.getRenderingPosition(), false),
                    accessor.getWorld(),
                    x,
                    y,
                    z);

                if (pick != null) {
                    String unloc = pick.getUnlocalizedName();
                    String englishName = TooltipEventHandler.getOriginalEnglishNameStatic(pick, unloc + ".name");

                    if (englishName != null && !englishName.equals(unloc)) {
                        currenttip.add("[EN] " + englishName);
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("[GT Item Untranslator][Waila] Exception while resolving name:");
            t.printStackTrace();
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
        int y, int z) {
        return tag;
    }
}
