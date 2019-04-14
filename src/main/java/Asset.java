import Types.Currency;

import java.math.BigDecimal;
import java.math.MathContext;

class Asset {
    private String symbol;
    private Currency currencyMarket;
    private BigDecimal buyPrice;
    private BigDecimal amount;

    Asset(String symbol, Currency currencyMarket, BigDecimal buyPrice, BigDecimal amount) {
        this.symbol = symbol;
        this.currencyMarket = currencyMarket;
        this.buyPrice = buyPrice;
        this.amount = amount;
    }

    String getSymbol() {
        return symbol;
    }

    Currency getCurrencyMarket() {
        return currencyMarket;
    }

    BigDecimal getBuyPrice() {
        return buyPrice;
    }

    void setAmount(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal(0)) < 0)
            throw new UnsupportedOperationException("Cannot set amount to below 0");

        this.amount = amount;
    }

    BigDecimal getAmount() {
        return amount;
    }

    void combineOrder(Asset asset) {
        if (!symbol.equals(asset.getSymbol()))
            throw new UnsupportedOperationException("Cannot combine orders with different symbols");

        if (!currencyMarket.equals(asset.getCurrencyMarket()))
            throw new UnsupportedOperationException("Cannot combine orders with different currency markets");

        BigDecimal preValue = buyPrice.multiply(amount);

        BigDecimal addedValue = asset.buyPrice.multiply(asset.amount);

        BigDecimal newValue = preValue.add(addedValue);

        amount = amount.add(asset.amount);

        buyPrice = newValue.divide(amount, MathContext.DECIMAL128);
    }

    BigDecimal getTotalBuyPriceCost() {
        return amount.multiply(buyPrice);
    }

    @Override
    public String toString() {
        return amount + " " + symbol;
    }
}
