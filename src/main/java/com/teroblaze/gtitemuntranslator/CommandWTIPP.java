package com.teroblaze.gtitemuntranslator;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandWTIPP extends CommandBase {

    @Override
    public String getCommandName() {
        return "wtipp";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/wtipp <on|off>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }
        if ("on".equalsIgnoreCase(args[0])) {
            GTItemUntranslator.wailaEnabled = true;
            GTItemUntranslator.config.get("general", "wailaEnabled", true)
                .set(GTItemUntranslator.wailaEnabled);
            GTItemUntranslator.config.save();
            sender.addChatMessage(new ChatComponentText("GT Waila original names: ON"));
        } else if ("off".equalsIgnoreCase(args[0])) {
            GTItemUntranslator.wailaEnabled = false;
            GTItemUntranslator.config.get("general", "wailaEnabled", true)
                .set(GTItemUntranslator.wailaEnabled);
            GTItemUntranslator.config.save();
            sender.addChatMessage(new ChatComponentText("GT Waila original names: OFF"));
        } else {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
