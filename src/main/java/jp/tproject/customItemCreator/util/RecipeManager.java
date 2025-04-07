package jp.tproject.customItemCreator.util;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.model.CustomRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * カスタムレシピをBukkitレシピシステムに登録するクラス
 */
public class RecipeManager {

    private final CustomItemCreator plugin;
    private final Map<String, NamespacedKey> registeredRecipes = new HashMap<>();

    /**
     * RecipeManagerを初期化
     * @param plugin プラグインインスタンス
     */
    public RecipeManager(CustomItemCreator plugin) {
        this.plugin = plugin;
    }

    /**
     * 保存されているすべてのレシピをサーバーに登録
     */
    public void registerAllRecipes() {
        // 既存のレシピを全て削除
        clearAllRecipes();

        // 全てのレシピを取得
        List<CustomRecipe> recipes = plugin.getConfigManager().getAllRecipes();

        for (CustomRecipe recipe : recipes) {
            registerRecipe(recipe);
        }

        plugin.getLogger().info("合計 " + registeredRecipes.size() + " 個のカスタムレシピを登録しました。");
    }

    /**
     * カスタムレシピをサーバーに登録
     * @param recipe 登録するレシピ
     * @return 登録に成功したらtrue
     */
    public boolean registerRecipe(CustomRecipe recipe) {
        // レシピが空なら登録しない
        if (recipe.isEmpty()) {
            return false;
        }

        // 結果アイテムを取得
        String resultItemId = recipe.getResultItemId();
        ItemStack resultItem = plugin.getConfigManager().getItem(resultItemId);

        if (resultItem == null) {
            plugin.getLogger().warning("レシピの結果アイテムが見つかりません: " + resultItemId);
            return false;
        }

        try {
            // レシピキーを作成
            NamespacedKey key = new NamespacedKey(plugin, "recipe_" + recipe.getRecipeId());

            // 既に登録されているレシピを削除
            if (registeredRecipes.containsKey(recipe.getRecipeId())) {
                Bukkit.removeRecipe(registeredRecipes.get(recipe.getRecipeId()));
                registeredRecipes.remove(recipe.getRecipeId());
            }

            // ShapedRecipeを作成
            ShapedRecipe shapedRecipe = new ShapedRecipe(key, resultItem);

            // 材料の配置から形状を決定
            String[] shape = determineShape(recipe.getIngredients());
            shapedRecipe.shape(shape);

            // 材料を設定
            setIngredients(shapedRecipe, recipe.getIngredients(), shape);

            // サーバーにレシピを登録
            Bukkit.addRecipe(shapedRecipe);

            // 登録済みレシピに追加
            registeredRecipes.put(recipe.getRecipeId(), key);

            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "レシピの登録に失敗しました: " + recipe.getRecipeId(), e);
            return false;
        }
    }

    /**
     * レシピをサーバーから削除
     * @param recipeId 削除するレシピID
     */
    public void unregisterRecipe(String recipeId) {
        if (registeredRecipes.containsKey(recipeId)) {
            Bukkit.removeRecipe(registeredRecipes.get(recipeId));
            registeredRecipes.remove(recipeId);
        }
    }

    /**
     * 登録したすべてのレシピを削除
     */
    public void clearAllRecipes() {
        for (NamespacedKey key : registeredRecipes.values()) {
            Bukkit.removeRecipe(key);
        }
        registeredRecipes.clear();
    }

    /**
     * 材料配置からレシピの形状を決定
     * @param ingredients 材料アイテム配列
     * @return レシピの形状（3行の文字列配列）
     */
    private String[] determineShape(ItemStack[] ingredients) {
        // レシピの各行を表す文字列を作成
        String[] shape = new String[3];
        boolean[] rowHasItem = new boolean[3];
        boolean[] colHasItem = new boolean[3];

        // 行と列に材料があるかチェック
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                if (index < ingredients.length && ingredients[index] != null && ingredients[index].getType() != Material.AIR) {
                    rowHasItem[row] = true;
                    colHasItem[col] = true;
                }
            }
        }

        // 有効な行と列の数をカウント
        int validRows = 0;
        int validCols = 0;
        int firstRow = -1;
        int firstCol = -1;

        for (int i = 0; i < 3; i++) {
            if (rowHasItem[i]) {
                validRows++;
                if (firstRow == -1) firstRow = i;
            }

            if (colHasItem[i]) {
                validCols++;
                if (firstCol == -1) firstCol = i;
            }
        }

        // 有効な材料がない場合
        if (validRows == 0 || validCols == 0) {
            return new String[]{"A"};
        }

        // 形状文字列を作成
        int shapeIndex = 0;
        for (int row = 0; row < 3; row++) {
            if (rowHasItem[row]) {
                StringBuilder sb = new StringBuilder();
                for (int col = 0; col < 3; col++) {
                    if (colHasItem[col]) {
                        int index = row * 3 + col;
                        if (index < ingredients.length && ingredients[index] != null && ingredients[index].getType() != Material.AIR) {
                            // アイテムがある場所には対応する文字を配置
                            sb.append((char)('A' + (row - firstRow) * 3 + (col - firstCol)));
                        } else {
                            // 空のスロットには空白を配置
                            sb.append(' ');
                        }
                    }
                }
                shape[shapeIndex++] = sb.toString();
            }
        }

        // 未使用の行をnullに設定
        for (int i = shapeIndex; i < 3; i++) {
            shape[i] = null;
        }

        // nullでない行だけを含む配列を作成
        String[] finalShape = new String[shapeIndex];
        for (int i = 0; i < shapeIndex; i++) {
            finalShape[i] = shape[i];
        }

        return finalShape;
    }

    /**
     * ShapedRecipeに材料を設定
     * @param recipe ShapedRecipeオブジェクト
     * @param ingredients 材料アイテム配列
     * @param shape レシピの形状
     */
    private void setIngredients(ShapedRecipe recipe, ItemStack[] ingredients, String[] shape) {
        Map<Character, RecipeChoice> ingredientMap = new HashMap<>();

        // 形状内の各文字に対応する材料を設定
        for (int row = 0; row < shape.length; row++) {
            if (shape[row] == null) continue;

            for (int col = 0; col < shape[row].length(); col++) {
                char c = shape[row].charAt(col);

                if (c == ' ') continue;

                // 文字から元の配列インデックスを計算
                int index = row * 3 + col;

                if (index < ingredients.length && ingredients[index] != null && ingredients[index].getType() != Material.AIR) {
                    // RecipeChoice.ExactChoiceを使用して正確な素材を指定
                    ingredientMap.put(c, new RecipeChoice.ExactChoice(ingredients[index]));
                }
            }
        }

        // レシピに材料を設定
        for (Map.Entry<Character, RecipeChoice> entry : ingredientMap.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }
    }
}