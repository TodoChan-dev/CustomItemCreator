package jp.tproject.customItemCreator.api;

import com.sun.net.httpserver.HttpServer;
import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.api.handlers.ItemDetailHandler;
import jp.tproject.customItemCreator.api.handlers.ItemsHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CustomItemCreatorのWeb APIサーバーを提供するクラス
 * ポート2002で動作し、アイテムデータをJSON形式で提供
 */
public class ApiServer {
    private static final Logger LOGGER = Logger.getLogger(ApiServer.class.getName());
    private final CustomItemCreator plugin;
    private HttpServer server;
    private final int port;

    /**
     * APIサーバーを初期化
     * @param plugin プラグインインスタンス
     * @param port ポート番号
     */
    public ApiServer(CustomItemCreator plugin, int port) {
        this.plugin = plugin;
        this.port = port;
    }

    /**
     * サーバーを起動
     */
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // コンテキスト（エンドポイント）の設定
            server.createContext("/api/items", new ItemsHandler(plugin));
            server.createContext("/api/items/", new ItemDetailHandler(plugin));

            // 別スレッドでサーバーを実行
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            LOGGER.info("API Server started on port " + port);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start API server", e);
        }
    }

    /**
     * サーバーを停止
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            LOGGER.info("API Server stopped");
        }
    }
}