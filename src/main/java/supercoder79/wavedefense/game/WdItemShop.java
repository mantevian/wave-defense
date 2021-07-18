package supercoder79.wavedefense.game;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class WdItemShop {
    public static NamedScreenHandlerFactory create(ServerPlayerEntity player, WdActive game) {
        return (NamedScreenHandlerFactory) ShopUi.create(player, new LiteralText("Item Shop"), shop -> {
            WdPlayer wdPlayer = game.players.get(PlayerRef.of(player));
            List<WdConfig.ShopEntry> entries = game.config.shop.get(wdPlayer.openedShopPage);

            for (WdConfig.ShopEntry entry : entries) {
                ItemStack displayItem = new ItemStack(Registry.ITEM.get(id(entry.display)));
                String displayName = entry.name;
                String displayDescription = entry.description;

                if (entry.display.equals("sword") || entry.display.equals("helmet") || entry.display.equals("chestplate") || entry.display.equals("leggings") || entry.display.equals("boots")) {
                    displayItem = getFirstInInventory(player, i -> i.getItem().toString().contains(entry.display));
                }

                ItemStack itemToGive = new ItemStack(Items.AIR);
                ItemStack itemToEnchant = new ItemStack(Items.AIR);
                ItemStack itemToUpgrade = new ItemStack(Items.AIR);
                ItemStack itemToReplaceWith = new ItemStack(Items.AIR);

                int currentLevel = 0;
                boolean canBuy = true;

                if (entry.item != null) {
                    itemToGive = new ItemStack(Registry.ITEM.get(id(entry.item.item)), entry.item.count);

                    if (!entry.potion.equals("")) {
                        NbtCompound potionTag = new NbtCompound();
                        potionTag.put("Potion", NbtString.of(entry.potion));
                        itemToGive.setNbt(potionTag);
                    }

                    if (!entry.item.item.equals("air")) {
                        displayItem = itemToGive;
                    }
                }

                if (entry.itemToUpgrade != null) {
                    if (entry.itemToUpgrade.equals("sword") || entry.itemToUpgrade.equals("helmet") || entry.itemToUpgrade.equals("chestplate") || entry.itemToUpgrade.equals("leggings") || entry.itemToUpgrade.equals("boots")) {
                        itemToUpgrade = getFirstInInventory(player, i -> i.getItem().toString().contains(entry.itemToUpgrade));
                        String name = itemToUpgrade.getItem().toString().replace("minecraft:", "");

                        switch (name) {
                            case "iron_sword" -> {
                                currentLevel = 0;
                                itemToReplaceWith = ItemStackBuilder.of(Items.DIAMOND_SWORD).setUnbreakable().build();
                            }
                            case "diamond_sword" -> {
                                currentLevel = 1;
                                itemToReplaceWith = ItemStackBuilder.of(Items.NETHERITE_SWORD).setUnbreakable().build();
                            }
                            case "netherite_sword" -> {
                                currentLevel = 2;
                                itemToReplaceWith = ItemStackBuilder.of(Items.NETHERITE_SWORD).setUnbreakable().build();
                            }
                            case "chainmail_helmet" -> {
                                currentLevel = 0;
                                itemToReplaceWith = ItemStackBuilder.of(Items.IRON_HELMET).setUnbreakable().build();
                            }
                            case "iron_helmet" -> {
                                currentLevel = 1;
                                itemToReplaceWith = ItemStackBuilder.of(Items.DIAMOND_HELMET).setUnbreakable().build();
                            }
                            case "diamond_helmet" -> {
                                currentLevel = 2;
                                itemToReplaceWith = ItemStackBuilder.of(Items.NETHERITE_HELMET).setUnbreakable().build();
                            }
                            case "netherite_helmet" -> {
                                currentLevel = 3;
                                itemToReplaceWith = ItemStackBuilder.of(Items.NETHERITE_HELMET).setUnbreakable().build();
                            }
                            case "chainmail_chestplate" -> {
                                currentLevel = 0;
                                itemToReplaceWith = ItemStackBuilder.of(Items.IRON_CHESTPLATE).setUnbreakable().build();
                            }
                            case "iron_chestplate" -> {
                                currentLevel = 1;
                                itemToReplaceWith = ItemStackBuilder.of(Items.DIAMOND_CHESTPLATE).setUnbreakable().build();
                            }
                            case "diamond_chestplate" -> {
                                currentLevel = 2;
                                itemToReplaceWith = ItemStackBuilder.of(Items.NETHERITE_CHESTPLATE).setUnbreakable().build();
                            }
                            case "netherite_chestplate" -> {
                                currentLevel = 3;
                                itemToReplaceWith = ItemStackBuilder.of(Items.NETHERITE_CHESTPLATE).setUnbreakable().build();
                            }
                            case "chainmail_leggings" -> {
                                currentLevel = 0;
                                itemToReplaceWith = ItemStackBuilder.of(Items.IRON_LEGGINGS).setUnbreakable().build();
                            }
                            case "iron_leggings" -> {
                                currentLevel = 1;
                                itemToReplaceWith = ItemStackBuilder.of(Items.DIAMOND_LEGGINGS).setUnbreakable().build();
                            }
                            case "diamond_leggings" -> {
                                currentLevel = 2;
                                itemToReplaceWith = ItemStackBuilder.of(Items.NETHERITE_LEGGINGS).setUnbreakable().build();
                            }
                            case "netherite_leggings" -> {
                                currentLevel = 3;
                                itemToReplaceWith = ItemStackBuilder.of(Items.NETHERITE_LEGGINGS).setUnbreakable().build();
                            }
                            case "chainmail_boots" -> {
                                currentLevel = 0;
                                itemToReplaceWith = ItemStackBuilder.of(Items.IRON_BOOTS).setUnbreakable().build();
                            }
                            case "iron_boots" -> {
                                currentLevel = 1;
                                itemToReplaceWith = ItemStackBuilder.of(Items.DIAMOND_BOOTS).setUnbreakable().build();
                            }
                            case "diamond_boots" -> {
                                currentLevel = 2;
                                itemToReplaceWith = ItemStackBuilder.of(Items.NETHERITE_BOOTS).setUnbreakable().build();
                            }
                            case "netherite_boots" -> {
                                currentLevel = 3;
                                itemToReplaceWith = ItemStackBuilder.of(Items.NETHERITE_BOOTS).setUnbreakable().build();
                            }
                        }
                    }
                }

                if (entry.enchantment != null) {
                    if (entry.enchantment.target.equals("sword") || entry.enchantment.target.equals("helmet") || entry.enchantment.target.equals("chestplate") || entry.enchantment.target.equals("leggings") || entry.enchantment.target.equals("boots") || entry.enchantment.target.equals("bow") || entry.enchantment.target.equals("crossbow")) {
                        itemToEnchant = getFirstInInventory(player, i -> i.getItem().toString().contains(entry.enchantment.target));
                        currentLevel = EnchantmentHelper.getLevel(Registry.ENCHANTMENT.get(id(entry.enchantment.enchantment)), itemToEnchant);

                        if (entry.enchantment.limit > 0 && currentLevel >= entry.enchantment.limit) {
                            canBuy = false;
                        }

                        if (!displayItem.getItem().equals(Items.ENCHANTED_BOOK)) {
                            displayItem = itemToEnchant;
                        }
                    }
                }

                Cost cost = Cost.no();
                int c = 0;
                String currency = "iron";

                switch (entry.cost.type) {
                    case "fixed":
                        c = entry.cost.cost;
                        currency = entry.cost.currency;
                        break;

                    case "scaled":
                        c = (int) (Math.pow(entry.cost.scale, currentLevel) * entry.cost.base);
                        currency = entry.cost.currency;
                        break;

                    case "scaled_alt":
                        c = (currentLevel + 1) * entry.cost.base + 4 * (int) Math.max(0, Math.pow(currentLevel - 2, entry.cost.scale));
                        currency = entry.cost.currency;
                        break;

                    case "custom":
                        if (currentLevel >= entry.cost.levels.size()) {
                            canBuy = false;
                        } else {
                            c = entry.cost.levels.get(currentLevel).cost;
                            currency = entry.cost.levels.get(currentLevel).currency;
                        }
                        break;
                }

                switch (currency) {
                    case "iron":
                        cost = Cost.ofIron(c);
                        break;

                    case "gold":
                        cost = Cost.ofGold(c);
                        break;

                    case "emerald":
                        cost = Cost.ofEmeralds(c);
                        break;
                }

                if (!canBuy || displayItem.getItem().equals(Items.LIGHT_GRAY_STAINED_GLASS_PANE)) {
                    cost = Cost.no();
                }

                int changePage = 0;
                int upgradePointsCost = 0;

                switch (entry.name) {
                    case "next_page":
                        changePage = 1;
                        displayName = "Next Page";
                        break;
                    case "previous_page":
                        changePage = -1;
                        displayName = "Previous Page";
                        break;
                    case "display_stats":
                        displayItem = new ItemStack(Items.PLAYER_HEAD);
                        NbtCompound skullTag = new NbtCompound();
                        skullTag.put("SkullOwner", NbtString.of(player.getName().getString()));
                        displayItem.setNbt(skullTag);
                        displayName = "Your Stats";
                        displayDescription = "Max Health: "
                                + player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH)
                                + "\nMovement Speed: "
                                + player.getAttributeBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                                + "\nAttack Speed: "
                                + player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED)
                                + "\n\n"
                                + wdPlayer.mobKillsToString()
                                + "\n\n"
                                + wdPlayer.mobAssistsToString();
                        break;
                    case "display_xp":
                        displayItem = new ItemStack(Items.EXPERIENCE_BOTTLE);
                        displayName = "Your Stats";
                        displayDescription = "XP: " + player.totalExperience + "\nUpgrade Points: " + wdPlayer.upgradePoints + " / " + player.experienceLevel;
                        break;
                    case "upgrade_health":
                        cost = Cost.free();
                        displayItem = new ItemStack(Items.GOLDEN_CHESTPLATE);
                        displayName = "Upgrade Shared Health";
                        displayDescription = "+1 HP to every player\nCost: 5 Upgrade Points";
                        upgradePointsCost = 5;
                        break;
                    case "upgrade_movement_speed":
                        cost = Cost.free();
                        displayItem = new ItemStack(Items.GOLDEN_BOOTS);
                        displayName = "Upgrade Shared Movement Speed";
                        displayDescription = "+0.004 Movement Speed to every player\nCost: 2 Upgrade Points";
                        upgradePointsCost = 2;
                        break;
                    case "upgrade_attack_speed":
                        cost = Cost.free();
                        displayItem = new ItemStack(Items.GOLDEN_PICKAXE);
                        displayName = "Upgrade Shared Attack Speed";
                        displayDescription = "+0.02 Attack Speed to every player\nCost: 2 Upgrade Points";
                        upgradePointsCost = 2;
                        break;
                }

                if (changePage != 0) {
                    cost = Cost.free();
                    displayItem = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
                }

                if (wdPlayer.upgradePoints - upgradePointsCost < 0) {
                    cost = Cost.no();
                }

                if (!itemToGive.getItem().equals(Items.AIR)) {
                    shop.addItem(itemToGive, cost);
                } else {
                    boolean shouldEnchantItem = !itemToEnchant.getItem().equals(Items.AIR);
                    boolean shouldUpgradeItem = !itemToReplaceWith.getItem().equals(Items.AIR);
                    ItemStack finalItemToUpgrade = itemToUpgrade;
                    ItemStack finalItemToReplaceWith = itemToReplaceWith;
                    ItemStack finalItemToEnchant = itemToEnchant;
                    int finalCurrentLevel = currentLevel;
                    int finalChangePage = changePage;
                    int finalUpgradePointsCost = upgradePointsCost;

                    ShopEntry shopEntry = ShopEntry.ofIcon(displayItem)
                            .withName(new LiteralText(displayName))
                            .withCost(cost)
                            .onBuy(p -> {
                                if (shouldUpgradeItem) {
                                    replaceItem(player, stack -> stack.getItem().equals(finalItemToUpgrade.getItem()), finalItemToReplaceWith);
                                } else if (shouldEnchantItem) {
                                    applyEnchantments(player, stack -> stack.getItem().equals(finalItemToEnchant.getItem()), Registry.ENCHANTMENT.get(id(entry.enchantment.enchantment)), finalCurrentLevel + 1);
                                }

                                wdPlayer.openedShopPage += finalChangePage;
                                wdPlayer.upgradePoints -= finalUpgradePointsCost;

                                switch (entry.name) {
                                    case "upgrade_health":
                                        game.sharedHealth += 1;
                                        break;
                                    case "upgrade_movement_speed":
                                        game.sharedSpeed += 0.004;
                                        break;
                                    case "upgrade_attack_speed":
                                        game.sharedAttackSpeed += 0.02;
                                        break;
                                }
                            });

                    for (String s : displayDescription.split("\n")) {
                        shopEntry.addLore(new LiteralText(s).styled(style -> style.withColor(TextColor.parse("gray")).withItalic(false)));
                    }

                    shop.add(shopEntry);
                }
            }
        });
    }

    private static Identifier id(String name) {
        return new Identifier("minecraft", name.replace("minecraft:", ""));
    }

    private static void applyEnchantments(ServerPlayerEntity player, Predicate<ItemStack> predicate, Enchantment enchantment, int level) {
        if (level <= 0) return;

        PlayerInventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty() && predicate.test(stack)) {
                int existingLevel = EnchantmentHelper.getLevel(enchantment, stack);
                if (existingLevel != level) {
                    Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
                    enchantments.remove(enchantment);
                    EnchantmentHelper.set(EnchantmentHelper.get(stack), stack);
                    stack.addEnchantment(enchantment, level);
                }
            }
        }
    }

    private static void replaceItem(ServerPlayerEntity player, Predicate<ItemStack> predicate, ItemStack newItem) {
        PlayerInventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty() && predicate.test(stack)) {
                for (Map.Entry<Enchantment, Integer> enchantments : EnchantmentHelper.get(stack).entrySet()) {
                    newItem.addEnchantment(enchantments.getKey(), enchantments.getValue());
                }
                inventory.setStack(slot, newItem);
            }
        }
    }

    private static ItemStack getFirstInInventory(ServerPlayerEntity player, Predicate<ItemStack> predicate) {
        PlayerInventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return stack;
            }
        }
        return null;
    }
}
