package me.isaiah.multiworld.forge.perm;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class Perm {

    public static Perm INSTANCE;
    public static void setPerm(Perm p) {INSTANCE = p;}
    
    public boolean has_impl(ServerPlayerEntity plr, String perm) {
        System.out.println("Platform Permission Handler not found!");
        return false;
    }

    public static boolean has(ServerPlayerEntity plr, String perm) {
        if (null == INSTANCE) {
            System.out.println("Platform Permission Handler not found!");
            return plr.hasPermissionLevel(2);
        }
        return INSTANCE.has_impl(plr, perm);
    }

    public static boolean has(ServerCommandSource s, String perm) {
        try {
            return has(s.getPlayer(), perm);
        } catch (CommandSyntaxException e) {
            return s.hasPermissionLevel(2);
        }
    }

}