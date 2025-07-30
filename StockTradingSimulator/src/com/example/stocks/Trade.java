package com.example.stocks;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
public class Trade {

    private int id;
    private int buyerUserId;
    private int sellerUserId;
    private int stockId;
    private BigDecimal price;
    private int quantity;
    private LocalDateTime timestamp;

    public Trade(int buyerUserId, int sellerUserId, int stockId, BigDecimal price, int quantity) {
        this.buyerUserId = buyerUserId;
        this.sellerUserId = sellerUserId;
        this.stockId = stockId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = null; // Will be set by the database (CURRENT_TIMESTAMP)
    }

    public Trade(int id, int buyerUserId, int sellerUserId, int stockId, BigDecimal price, int quantity, LocalDateTime timestamp) {
        this.id = id;
        this.buyerUserId = buyerUserId;
        this.sellerUserId = sellerUserId;
        this.stockId = stockId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }
    public int getId() {
        return id;
    }

    public int getBuyerUserId() {
        return buyerUserId;
    }

    public int getSellerUserId() {
        return sellerUserId;
    }

    public int getStockId() {
        return stockId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", buyerUserId=" + buyerUserId +
                ", sellerUserId=" + sellerUserId +
                ", stockId=" + stockId +
                ", price=" + price +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        // For equality, we typically rely on the unique database ID.
        return id == trade.id; // Once saved, ID is the unique identifier.
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Hash code based on the unique ID.
    }


}
