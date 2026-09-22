package com.bx.patcher;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public final class Main {
    private static final String TARGET = "com/bx/magicSmp/menus/SellMenu.class";
    private static final String OWNER = "com/bx/magicSmp/menus/SellMenu";
    private static final String BRIDGE = "com/bx/magicSmp/menus/SellMenuClickBridge";
    private static final String DESC = "(ILorg/bukkit/entity/Player;Lorg/bukkit/event/inventory/ClickType;)V";

    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Usage: <input.jar> <helper.jar> <output.jar>");
        Path input = Path.of(args[0]), helper = Path.of(args[1]), output = Path.of(args[2]);
        Files.createDirectories(output.toAbsolutePath().getParent());

        byte[] helperClass = readEntry(helper, "com/bx/magicSmp/menus/SellMenuClickBridge.class");
        if (helperClass == null) throw new IllegalStateException("SellMenuClickBridge.class missing from helper jar");

        boolean patched = false;
        try (JarFile jar = new JarFile(input.toFile()); JarOutputStream out = new JarOutputStream(Files.newOutputStream(output))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                if (e.getName().equals("com/bx/magicSmp/menus/SellMenuClickBridge.class")) continue;
                byte[] data;
                try (InputStream is = jar.getInputStream(e)) { data = is.readAllBytes(); }
                if (TARGET.equals(e.getName())) { data = patchSellMenu(data); patched = true; }
                JarEntry ne = new JarEntry(e.getName()); if (e.getTime() >= 0) ne.setTime(e.getTime());
                out.putNextEntry(ne); out.write(data); out.closeEntry();
            }
            JarEntry bridgeEntry = new JarEntry("com/bx/magicSmp/menus/SellMenuClickBridge.class");
            out.putNextEntry(bridgeEntry); out.write(helperClass); out.closeEntry();
        }
        if (!patched) throw new IllegalStateException("SellMenu.class not found");
        System.out.println("Patched SellMenu: protected multiplier clicks are deferred safely; progression and Back remain enabled.");
    }

    private static byte[] patchSellMenu(byte[] bytes) {
        ClassNode cn = new ClassNode(); new ClassReader(bytes).accept(cn, 0);
        int replacements = 0;
        for (MethodNode mn : cn.methods) {
            if (!mn.name.equals("handleInventoryClick") || !mn.desc.equals("(Lorg/bukkit/event/inventory/InventoryClickEvent;)V")) continue;
            for (AbstractInsnNode n = mn.instructions.getFirst(); n != null; n = n.getNext()) {
                if (n instanceof MethodInsnNode m && m.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                    m.owner.equals(OWNER) && m.name.equals("handleClick") && m.desc.equals(DESC)) {
                    m.setOpcode(Opcodes.INVOKESTATIC);
                    m.owner = BRIDGE;
                    m.name = "schedule";
                    m.desc = "(Lcom/bx/magicSmp/menus/SellMenu;ILorg/bukkit/entity/Player;Lorg/bukkit/event/inventory/ClickType;)V";
                    m.itf = false;
                    replacements++;
                }
            }
        }
        if (replacements != 1) throw new IllegalStateException("Expected exactly 1 protected SellMenu handleClick call, found " + replacements);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS); cn.accept(cw); return cw.toByteArray();
    }

    private static byte[] readEntry(Path jarPath, String name) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            JarEntry e = jar.getJarEntry(name); if (e == null) return null;
            try (InputStream is = jar.getInputStream(e)) { return is.readAllBytes(); }
        }
    }
}
