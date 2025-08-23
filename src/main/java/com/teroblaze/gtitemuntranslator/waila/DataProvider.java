package com.teroblaze.gtitemuntranslator.waila;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

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
        try {
            ItemStack stack = itemStack;
            if (stack == null) {
                // если в инвентаре нет ItemStack, пробуем создать его из блока в мире
                Block block = accessor.getBlock();
                int meta = accessor.getMetadata();
                if (block != null) {
                    stack = new ItemStack(block, 1, meta);
                }
            }

            if (stack != null) {
                String unloc = stack.getUnlocalizedName();
                String englishName = TooltipEventHandler.getOriginalEnglishNameStatic(stack, unloc);

                if (englishName != null && !englishName.equals(unloc)
                    && !currenttip.get(0)
                        .contains(englishName)) {
                    currenttip.add(1, EnumChatFormatting.GRAY + "[EN] " + englishName);
                }
            }
        } catch (Throwable t) {
            System.err.println("[GT Item Untranslator] Waila head error: " + t.getMessage());
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
    public NBTTagCompound getNBTData(EntityPlayerMP player, net.minecraft.tileentity.TileEntity te, NBTTagCompound tag,
        World world, int x, int y, int z) {
        return tag;
    }
}
