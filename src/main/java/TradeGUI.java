import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import Types.LogType;
import Types.Currency;
import javafx.application.Platform;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TradeGUI extends Application {

    private TradeHandler tradeHandler = null;

    private final long START_TIME = System.currentTimeMillis();

    private final Image APPLICATION_ICON = new Image("Logos/currency_exchange.png");

    private final InlineCssTextArea CONSOLE_LOG = getSizedTextArea();

    private final String CONSOLE_LOG_TEXT_COLOR_DEFAULT = "BLACK";
    private final String CONSOLE_LOG_TEXT_COLOR_INFORMATION = "BLUE";
    private final String CONSOLE_LOG_TEXT_COLOR_ERROR = "RED";
    private final String CONSOLE_LOG_TEXT_COLOR_BUY = "GREEN";
    private final String CONSOLE_LOG_TEXT_COLOR_SELL = "RED";
    private final String CONSOLE_LOG_TEXT_COLOR_TIME = "GREY";

    private Label BALANCE_LABEL = null;
    private Label PROFIT_LABEL = null;

    @Override
    public void start(Stage primaryStage) {
        tradeHandler = new TradeHandler(this);
        tradeHandler.start();

        while (!tradeHandler.isInitialized())
            Util.sleep(10);

        BALANCE_LABEL = new Label(tradeHandler.getBalance() + " " + tradeHandler.getCurrency());
        PROFIT_LABEL = new Label(tradeHandler.getTotalProfit() + " " + tradeHandler.getCurrency());

        primaryStage.setTitle("TradeGUI");
        primaryStage.getIcons().add(APPLICATION_ICON);

        StackPane root = new StackPane();
        Scene scene = new Scene(root, 400, 337);

        scene.getStylesheets().add("trade-gui.css");

        TabPane tabPane = getTabPane();
        root.getChildren().add(tabPane);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TabPane getTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        Tab mainTab = new Tab("Main"), assetsTab = new Tab("Assets")/*, historyTab = new Tab("History")*/;

        mainTab.setContent(getMainContent());

        ObservableList<Asset> observableAssetsList = tradeHandler.getAssets();
        ListView<Asset> listView = new ListView<>(observableAssetsList);
        assetsTab.setContent(listView);

        tabPane.getTabs().addAll(mainTab, assetsTab/*, historyTab*/);
        return tabPane;
    }

    private Node getMainContent() {
        BorderPane borderPane = new BorderPane();
        GridPane grid = new GridPane();
        borderPane.setPadding(new Insets(5, 5, 5, 5));
        grid.setHgap(4);

        String[] labelText = new String[] {"Runtime: ", "Balance: ", "Profit: "};
        Font boldFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, Font.getDefault().getSize() * 1.1);
        for (int i = 0; labelText.length > i; i++) {
            Label label = new Label(labelText[i]);
            label.setFont(boldFont);
            grid.add(label, 0, i);
        }
        grid.add(getRuntimeLabel(), 1, 0);
        grid.add(BALANCE_LABEL, 1, 1);
        grid.add(PROFIT_LABEL, 1, 2);
        borderPane.setTop(grid);

        CONSOLE_LOG.setEditable(false);
        CONSOLE_LOG.setStyle("-fx-font-family: consolas, candera; -fx-font-size: 9pt;");
        VirtualizedScrollPane<InlineCssTextArea> vsPane = new VirtualizedScrollPane<>(CONSOLE_LOG);
        VBox vbox = new VBox();
        VBox.setVgrow(vsPane, Priority.ALWAYS);
        vbox.getChildren().addAll(vsPane);
        borderPane.setCenter(vbox);

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5, 0, 0, 0));
        hbox.setSpacing(5);
        hbox.getChildren().addAll(getCurrencyBox(), getDumpAssetsButton());
        borderPane.setBottom(hbox);

        return borderPane;
    }

    void consoleLog(LogType logType, String message) {
        if (message == null || message.length() == 0) {
            return;
        }

        Platform.runLater(() -> {
            if (CONSOLE_LOG.getText().length() > 0) {
                CONSOLE_LOG.appendText("\n"); // new line
            }

            int entryStartIndex = CONSOLE_LOG.getLength();

            CONSOLE_LOG.appendText(Util.getLocalTime() + " " +
                    (logType == LogType.BUY ? "[B] " : "") +
                    (logType == LogType.SELL ? "[S] " : "") +
                    message);

            int messageStartIndex = CONSOLE_LOG.getText().lastIndexOf(message);

            CONSOLE_LOG.setStyle(entryStartIndex, messageStartIndex+message.length(), "-fx-fill: " + CONSOLE_LOG_TEXT_COLOR_DEFAULT);
            CONSOLE_LOG.setStyle(entryStartIndex, messageStartIndex, "-fx-fill: " + CONSOLE_LOG_TEXT_COLOR_TIME);

            switch (logType){
                case BUY:
                    CONSOLE_LOG.setStyle(messageStartIndex-4, messageStartIndex, "-fx-fill: " + CONSOLE_LOG_TEXT_COLOR_BUY);
                    break;

                case SELL:
                    CONSOLE_LOG.setStyle(messageStartIndex-4, messageStartIndex, "-fx-fill: " + CONSOLE_LOG_TEXT_COLOR_SELL);
                    break;

                case ERROR:
                    CONSOLE_LOG.setStyle(messageStartIndex, messageStartIndex+message.length(), "-fx-fill: " + CONSOLE_LOG_TEXT_COLOR_ERROR);
                    break;

                case INFORMATION:
                default:
                    CONSOLE_LOG.setStyle(messageStartIndex, messageStartIndex+message.length(), "-fx-fill: " + CONSOLE_LOG_TEXT_COLOR_INFORMATION);
                    break;
            }

            CONSOLE_LOG.requestFollowCaret();
        });
    }

    private InlineCssTextArea getSizedTextArea() {
        int rows = 10000;
        return new InlineCssTextArea() {
            @Override
            public void replaceText(int start, int end, String text) {
                super.replaceText(start, end, text);
                while (getText().split("\n", -1).length > rows) {
                    int fle = getText().indexOf("\n");
                    super.replaceText(0, fle + 1, "");
                }
                //positionCaret(getText().length());
            }
        };
    }

    private Label getRuntimeLabel() {
        Label runtime = new Label("00:00");

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(25), event -> {
            runtime.setText(Util.getFormattedRunTime(System.currentTimeMillis() - START_TIME));
            updateStats();
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        return runtime;
    }

    private void updateStats() {
        DecimalFormat df = tradeHandler.getCurrency().getDecimalFormat();

        BALANCE_LABEL.setText(df.format(tradeHandler.getBalance()) + " " + tradeHandler.getCurrency());

        PROFIT_LABEL.setText(df.format(tradeHandler.getTotalProfit()) + " " + tradeHandler.getCurrency() +
                " (" + df.format(Util.getPerHour(tradeHandler.getTotalProfit(), START_TIME)) + " " + tradeHandler.getCurrency() + " p/h)");
    }

    private ComboBox<String> getCurrencyBox() {
        ObservableList<String> options = FXCollections.observableArrayList(Arrays.stream(Currency.values()).map(Enum::name).toArray(String[]::new));
        ComboBox<String> comboBox = new ComboBox<>(options);

        comboBox.getSelectionModel().select(tradeHandler.getCurrency().toString());

        comboBox.valueProperty().addListener((arg0, arg1, text) -> tradeHandler.setCurrency(Currency.valueOf(text)));

        return comboBox;
    }

    private Button getDumpAssetsButton() {
        Button btn = new Button("Sell assets");
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: #dc3545;"/* -fx-background-radius: 4; fx-border-radius:4; fx-border-insets:4; -fx-border-color:black;"*/);
        //btn.setDisable(true);

        btn.setOnAction(event -> {
            List<Asset> assets = tradeHandler.getAssets();
            if (assets == null || assets.size() == 0) {
                Alert alert = new Alert(AlertType.INFORMATION, "You have no assets to sell");
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(APPLICATION_ICON);
                alert.showAndWait();
            }
            else {
                StringBuilder alertText = new StringBuilder("Are you sure you want to sell ALL assets?");

                BigDecimal totalAssetsCost = new BigDecimal(0);
                for (Asset asset : assets) {
                    BigDecimal buyCost = asset.getTotalBuyPriceCost();
                    DecimalFormat df = asset.getCurrencyMarket().getDecimalFormat();

                    alertText.append("\n" + df.format(asset.getAmount()) + " " + asset.getSymbol() +
                            " (" +  df.format(buyCost) + " " + asset.getCurrencyMarket() + ")");

                    if (!tradeHandler.getCurrency().equals(asset.getCurrencyMarket())) {
                        BigDecimal currencyMultiplier = Util.getCurrencyMultiplier(asset.getCurrencyMarket(), tradeHandler.getCurrency());
                        buyCost = buyCost.multiply(currencyMultiplier); // convert to show total assets wealth in selected currency
                    }

                    totalAssetsCost = totalAssetsCost.add(buyCost);
                }

                DecimalFormat df = tradeHandler.getCurrency().getDecimalFormat();
                alertText.append("\n\n" + "Total value: " + df.format(totalAssetsCost) + " " + tradeHandler.getCurrency());

                // Alert alert = new Alert(AlertType.WARNING, alertText, ButtonType.YES, ButtonType.NO);
                // Optional<ButtonType> result = alert.showAndWait();
                // if (result.get() == ButtonType.YES) {}

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Confirmation");
                dialog.setHeaderText(alertText.toString());
                dialog.setContentText("Write \"yes\" to confirm");

                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                stage.getIcons().add(APPLICATION_ICON);

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent() && result.get().equalsIgnoreCase("yes")) {
                    tradeHandler.dumpAssets();
                }
            }
        });

        return btn;
    }

}