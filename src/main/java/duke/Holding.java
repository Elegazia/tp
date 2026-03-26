package duke;

public class Holding {
    private final AssetType assetType;
    private final String ticker;
    private double quantity;
    private Double lastPrice;
    private double averageBuyPrice;

    public Holding(AssetType assetType, String ticker, double quantity, double purchasePrice) {
        this.assetType = assetType;
        this.ticker = ticker.toUpperCase();
        this.quantity = quantity;
        this.lastPrice = purchasePrice;
        this.averageBuyPrice = purchasePrice;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public String getTicker() {
        return ticker;
    }

    public double getQuantity() {
        return quantity;
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void addQuantity(double quantityToAdd, double purchasePrice) {
        double totalCostBefore = averageBuyPrice * quantity;
        double addedCost = purchasePrice * quantityToAdd;
        quantity += quantityToAdd;
        averageBuyPrice = (totalCostBefore + addedCost) / quantity;
    }

    public double removeQuantity(double quantityToRemove, double sellPrice) {
        if (quantityToRemove <= 0 || quantityToRemove > quantity) {
            throw new IllegalArgumentException("Invalid quantity to remove");
        }

        double realizedPnl = (sellPrice - averageBuyPrice) * quantityToRemove;
        quantity -= quantityToRemove;

        if (quantity == 0) {
            averageBuyPrice = 0;
        }

        return realizedPnl;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public boolean hasPrice() {
        return lastPrice != null;
    }

    public double getUnrealizedPnl() {
        if (lastPrice == null) {
            return 0.0;
        }
        return (lastPrice - averageBuyPrice) * quantity;
    }

    public void restoreMarketData(Double restoredLastPrice, double restoredAverageBuyPrice) {
        this.lastPrice = restoredLastPrice;
        this.averageBuyPrice = restoredAverageBuyPrice;
    }

    public double getValue() {
        if (lastPrice == null) {
            return 0.0;
        }
        return quantity * lastPrice;
    }
}
