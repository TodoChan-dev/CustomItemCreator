package jp.tproject.customItemCreator.model;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * カスタムレシピを表すクラス
 */
public class CustomRecipe {

    private final String recipeId;
    private final String resultItemId;
    private final ItemStack[] ingredients;

    /**
     * 新しいカスタムレシピを作成
     * @param recipeId レシピID
     * @param resultItemId 結果アイテムのID
     * @param ingredients 材料アイテム（9マス分の配列）
     */
    public CustomRecipe(String recipeId, String resultItemId, ItemStack[] ingredients) {
        this.recipeId = recipeId;
        this.resultItemId = resultItemId;

        // 9マスになるよう調整
        this.ingredients = new ItemStack[9];
        for (int i = 0; i < Math.min(ingredients.length, 9); i++) {
            this.ingredients[i] = ingredients[i] != null ? ingredients[i].clone() : null;
        }
    }

    /**
     * レシピIDを取得
     * @return レシピID
     */
    public String getRecipeId() {
        return recipeId;
    }

    /**
     * 結果アイテムのIDを取得
     * @return 結果アイテムのID
     */
    public String getResultItemId() {
        return resultItemId;
    }

    /**
     * 材料アイテムの配列を取得
     * @return 材料アイテム（9マス分の配列）
     */
    public ItemStack[] getIngredients() {
        // 防御的コピーを返す
        ItemStack[] copy = new ItemStack[ingredients.length];
        for (int i = 0; i < ingredients.length; i++) {
            copy[i] = ingredients[i] != null ? ingredients[i].clone() : null;
        }
        return copy;
    }

    /**
     * レシピが空かどうか
     * @return 空であればtrue
     */
    public boolean isEmpty() {
        for (ItemStack item : ingredients) {
            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                return false;
            }
        }
        return true;
    }

    /**
     * このレシピが指定されたアイテム配列とマッチするか確認
     * @param testItems テストするアイテム配列
     * @return マッチすればtrue
     */
    public boolean matches(ItemStack[] testItems) {
        if (testItems.length != ingredients.length) {
            return false;
        }

        for (int i = 0; i < ingredients.length; i++) {
            ItemStack recipe = ingredients[i];
            ItemStack test = testItems[i];

            // 両方nullまたは空気の場合はマッチ
            if ((recipe == null || recipe.getType() == org.bukkit.Material.AIR) &&
                    (test == null || test.getType() == org.bukkit.Material.AIR)) {
                continue;
            }

            // 片方だけnullまたは空気の場合はマッチしない
            if ((recipe == null || recipe.getType() == org.bukkit.Material.AIR) ||
                    (test == null || test.getType() == org.bukkit.Material.AIR)) {
                return false;
            }

            // 素材タイプが一致するか確認
            if (recipe.getType() != test.getType()) {
                return false;
            }

            // 素材のData値が一致するか確認（必要に応じて）
            // これは古いバージョンのMinecraftで使用されていたため、必要に応じて実装
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        CustomRecipe other = (CustomRecipe) obj;
        return recipeId.equals(other.recipeId);
    }

    @Override
    public int hashCode() {
        return recipeId.hashCode();
    }
}