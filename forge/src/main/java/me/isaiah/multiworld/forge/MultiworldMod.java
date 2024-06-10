/**
 * Multiworld Mod
 * Copyright (c) 2021-2022 by Isaiah.
 */
package me.isaiah.multiworld.forge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.isaiah.multiworld.forge.command.CreateCommand;
import me.isaiah.multiworld.forge.command.SetspawnCommand;
import me.isaiah.multiworld.forge.command.SpawnCommand;
import me.isaiah.multiworld.forge.command.TpCommand;
import me.isaiah.multiworld.forge.perm.Perm;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Multiworld version 1.3
 */
public class MultiworldMod {

    public static final String MOD_ID = "multiworld";
    public static MinecraftServer mc;
    public static String CMD = "mw";
    public static ICreator world_creator;
    
    public static void setICreator(ICreator ic) {
        world_creator = ic;
    }

    public static ServerWorld create_world(String id, RegistryKey<DimensionType> dim, ChunkGenerator gen, Difficulty dif) {
        return world_creator.create_world(id, dim,gen,dif);
    }

    // On mod init
    public static void init() {
        System.out.println("Multiworld init");
    }

    // On server start
    public static void on_server_started(MinecraftServer mc) {
        MultiworldMod.mc = mc;
    }
    
    // On command register
    public static void register_commands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(CMD)
                    .requires(source -> {
                        try {
                            return Perm.has(source.getPlayer(), "multiworld.cmd") ||
                                    Perm.has(source.getPlayer(), "multiworld.admin");
                        } catch (CommandSyntaxException e) {
                            return source.hasPermissionLevel(1);
                        }
                    }) 
                        .executes(ctx -> {
                            return broadcast(ctx.getSource(), Formatting.AQUA, null);
                        })
                        .then(argument("message", greedyString()).suggests(new InfoSuggest())
                                .executes(ctx -> {
                                    try {
                                        return broadcast(ctx.getSource(), Formatting.AQUA, getString(ctx, "message") );
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        return 1;
                                    }
                                 }))); 
    }
    
    public static int broadcast(ServerCommandSource source, Formatting formatting, String message) throws CommandSyntaxException {
        final ServerPlayerEntity plr = source.getPlayer();

        if (null == message) {
            plr.sendMessage(new LiteralText("Usage:").formatted(Formatting.AQUA), false);
            return 1;
        }

        boolean ALL = Perm.has(plr, "multiworld.admin");
        String[] args = message.split(" ");

        if (args[0].equalsIgnoreCase("setspawn") && (ALL || Perm.has(plr, "multiworld.setspawn") )) {
            return SetspawnCommand.run(mc, plr, args);
        } else if (args[0].equalsIgnoreCase("spawn") && (ALL || Perm.has(plr, "multiworld.spawn")) ) {
            return SpawnCommand.run(mc, plr, args);
        }else if (args[0].equalsIgnoreCase("tp") ) {
            if (!(ALL || Perm.has(plr, "multiworld.tp"))) {
                plr.sendMessage(Text.of("No permission! Missing permission: multiworld.tp"), false);
                return 1;
            }
            if (args.length == 1) {
                plr.sendMessage(new LiteralText("Usage: /" + CMD + " tp <world>"), false);
                return 0;
            }
            return TpCommand.run(mc, plr, args);
        } else if (args[0].equalsIgnoreCase("list") ) {
            if (!(ALL || Perm.has(plr, "multiworld.cmd"))) {
                plr.sendMessage(Text.of("No permission! Missing permission: multiworld.cmd"), false);
                return 1;
            }
            plr.sendMessage(new LiteralText("All Worlds:").formatted(Formatting.AQUA), false);
            mc.getWorlds().forEach(world -> {
                String name = world.getRegistryKey().getValue().toString();
                if (name.startsWith("multiworld:")) name = name.replace("multiworld:", "");

                plr.sendMessage(new LiteralText("- " + name), false);
            });
        } else if (args[0].equalsIgnoreCase("version") && (ALL || Perm.has(plr, "multiworld.cmd")) ) {
            plr.sendMessage(new LiteralText("Multiworld Mod (Forge) version 1.4"), false);
            return 1;
        } else if (args[0].equalsIgnoreCase("create") ) {
            if (!(ALL || Perm.has(plr, "multiworld.create"))) {
                plr.sendMessage(Text.of("No permission! Missing permission: multiworld.create"), false);
                return 1;
            }
            return CreateCommand.run(mc, plr, args);
        } else if (args[0].equalsIgnoreCase("mobs")) {
            boolean mobs = Boolean.getBoolean(args[1]);
            plr.sendMessage(Text.of("Setting mob spawning to: " + mobs), false);
            plr.getWorld().setMobSpawnOptions(mobs, mobs);
        }

        return Command.SINGLE_SUCCESS; // Success
    }
    
    
}
