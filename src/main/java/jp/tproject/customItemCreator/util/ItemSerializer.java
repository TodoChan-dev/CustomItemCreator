package jp.tproject.customItemCreator.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ItemStackをシリアライズ/デシリアライズするユーティリティクラス
 */
public class ItemSerializer {

    private static final Logger LOGGER = Logger.getLogger("ItemSerializer");

    /**
     * ItemStackをBase64文字列に変換
     * @param item 変換するアイテム
     * @return Base64にエンコードされた文字列
     */
    public static String toBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(item);
            dataOutput.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "アイテムをシリアライズできませんでした", e);
            return null;
        }
    }

    /**
     * Base64文字列からItemStackに変換
     * @param data Base64エンコードされた文字列
     * @return 復元されたItemStack
     */
    public static ItemStack fromBase64(String data) {
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();

            return item;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "アイテムをデシリアライズできませんでした", e);
            return null;
        }
    }
}