import Types.LogType;
import Types.Currency;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradeHandler extends Thread {

    private TradeGUI gui;

    private boolean initialized = false;

    private Currency currency = Currency.USD;

    private BigDecimal balance = new BigDecimal(0);
    private BigDecimal startBalance = new BigDecimal(0);

    private ObservableList<Asset> assets = FXCollections.observableArrayList();
    private List<Asset> startAssets = new ArrayList<>();

    TradeHandler(TradeGUI tradeGUI) {
        this.gui = tradeGUI;
        this.setDaemon(true);
    }

    public void run() {
        // Main trading logic here.
        // This method is run in a seperate thread than the GUI.

        // If running live be sure to save logs of executed trades, with parameters such as:
        // Date & time (unix-format), Bot-name/version, Setup/indicators, Broker-platform, Trade-reference-id?, Pair/market, Entries/stops/exits, Result, Close-time

        setDummyData();
        initialized = true;

        gui.consoleLog(LogType.INFORMATION, "Adding dummy trades in 5 seconds...");
        Util.sleep(5000);
        addDummyTrades();

        gui.consoleLog(LogType.INFORMATION,"Finished setting & adding dummy data");
    }

    private void setDummyData() {
        currency = Currency.USD;

        balance = new BigDecimal(100000);
        startBalance = balance;

        assets.add(new Asset("AMZN", Currency.USD, new BigDecimal(1850), new BigDecimal(3)));
        startAssets.add(new Asset("AMZN", Currency.USD, new BigDecimal(1850), new BigDecimal(3)));
    }

    private void addDummyTrades() {
        for (int i = 1; 100 >= i; i++) {
            String symbol;
            BigDecimal usdPrice;

            if (i < 25) {
                symbol = "AMZN";
                usdPrice = new BigDecimal(1850);
            }
            else if (i < 50) {
                symbol = "AAPL";
                usdPrice = new BigDecimal(200);
            }
            else if (i < 75) {
                symbol = "FB";
                usdPrice = new BigDecimal(180);
            }
            else {
                symbol = "MSFT";
                usdPrice = new BigDecimal(120);
            }

            buyAsset(symbol, currency, usdPrice.multiply(currency.getUsdValue()), new BigDecimal(i));
            Util.sleep(50);
            sellAsset(symbol, currency, usdPrice.multiply(currency.getUsdValue()).multiply(new BigDecimal(1.02)), new BigDecimal(i));
            Util.sleep(50);
        }
    }

    private boolean buyAsset(String symbol, Currency currencyMarket, BigDecimal buyPrice, BigDecimal amount) {
        DecimalFormat df = currencyMarket.getDecimalFormat();
        BigDecimal buyCost = buyPrice.multiply(amount);

        if (!currency.equals(currencyMarket)) {
            BigDecimal currencyMultiplier = Util.getCurrencyMultiplier(currencyMarket, currency);
            buyCost = buyCost.multiply(currencyMultiplier);
        }

        if (balance.compareTo(buyCost) < 0) {
            gui.consoleLog(LogType.ERROR, "Not enough money to buy " + df.format(amount) + " " + symbol +
                    " - lacking " + df.format(buyCost.subtract(balance)) + " " + currency);
            return false;
        }

        balance = balance.subtract(buyCost);
        Asset newAsset = new Asset(symbol, currencyMarket, buyPrice, amount);

        Asset existingAsset = assets.stream()
                .filter(x -> x.getSymbol().equals(symbol) &&
                        x.getCurrencyMarket().equals(currencyMarket))
                .findAny()
                .orElse(null);

        Platform.runLater(() -> {
            if (existingAsset == null)
                assets.add(newAsset);
            else
                existingAsset.combineOrder(newAsset);
        });

        gui.consoleLog(LogType.BUY, df.format(amount) + " " + symbol + " for " + df.format(buyPrice) + " " +
                currencyMarket + " each " + "(" + df.format(buyCost) + " " + currency + ")");
        return true;
    }

    private boolean sellAsset(String symbol, Currency currencyMarket, BigDecimal sellPrice, BigDecimal amount) {
        DecimalFormat df = currencyMarket.getDecimalFormat();
        Asset asset = assets.stream()
                .filter(x -> x.getSymbol().equals(symbol) &&
                        x.getCurrencyMarket().equals(currencyMarket))
                .findAny()
                .orElse(null);

        if (asset == null) {
            gui.consoleLog(LogType.ERROR, "Don't have any " + symbol + " assets to sell");
            return false;
        }

        if (asset.getAmount().compareTo(amount) < 0) {
            gui.consoleLog(LogType.ERROR, "Don't have enough " + symbol + " assets to sell " +
                    " - lacking " + df.format(amount.subtract(asset.getAmount())) + " " + symbol);
            return false;
        }

        Platform.runLater(() -> {
            if (asset.getAmount().compareTo(amount) == 0) // same amount
                assets.remove(asset);
            else
                asset.setAmount(asset.getAmount().subtract(amount));
        });

        BigDecimal balanceReceived = sellPrice.multiply(amount);

        if (!currency.equals(currencyMarket)) {
            BigDecimal currencyMultiplier = Util.getCurrencyMultiplier(currencyMarket, currency);
            balanceReceived = balanceReceived.multiply(currencyMultiplier);
        }

        balance = balance.add(balanceReceived);

        gui.consoleLog(LogType.SELL, df.format(amount) + " " + symbol + " for " + df.format(sellPrice) + " " +
                currencyMarket + " each " + "(" + df.format(balanceReceived) + " " + currency + ")");
        return true;
    }

    void dumpAssets() {
        gui.consoleLog(LogType.INFORMATION, "Selling all assets");

        Collections.reverse(assets);
        int size = assets.size();
        for (int i = size - 1; i >= 0; i--) { // reverse order to avoid ConcurrentModificationException when the elements get removed
            Asset asset = assets.get(i);
            sellAsset(asset.getSymbol(), asset.getCurrencyMarket(), asset.getBuyPrice(), asset.getAmount());
        }
    }

    BigDecimal getTotalProfit() {
        BigDecimal balanceProfit = balance.subtract(startBalance);

        BigDecimal assetsProfit = getListWealth(assets).subtract(getListWealth(startAssets));

        return balanceProfit.add(assetsProfit);
    }

    private BigDecimal getListWealth(List<Asset> list) {
        BigDecimal listWealth = new BigDecimal(0);

        for (Asset asset : list) {
            BigDecimal buyCost = asset.getTotalBuyPriceCost();

            if (!currency.equals(asset.getCurrencyMarket())) {
                BigDecimal currencyMultiplier = Util.getCurrencyMultiplier(asset.getCurrencyMarket(), currency);
                buyCost = buyCost.multiply(currencyMultiplier);
            }

            listWealth = listWealth.add(buyCost);
        }

        return listWealth;
    }

    boolean isInitialized() {
        return initialized;
    }

    BigDecimal getBalance() {
        return balance;
    }

    ObservableList<Asset> getAssets() {
        return assets;
    }

    void setCurrency(Currency newCurrency) {
        BigDecimal currencyMultiplier = Util.getCurrencyMultiplier(currency, newCurrency);
        currency = newCurrency;
        startBalance = startBalance.multiply(currencyMultiplier);
        balance = balance.multiply(currencyMultiplier);
    }

    Currency getCurrency() {
        return currency;
    }
}
