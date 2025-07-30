package com.example.stocks;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrderMatchingEngine {
    private final OrderService orderService;
    private final TradeService tradeService;
    private final StockService stockService;

    private ScheduledExecutorService scheduler;

    private static final long MATCHING_INTERVAL_SECONDS=3;

    public OrderMatchingEngine(OrderService orderServide,TradeService tradeService,StockService stockService){
        this.orderService=orderServide;
        this.tradeService=tradeService;
        this.stockService=stockService;
        this.scheduler=Executors.newSingleThreadScheduledExecutor();
    }
    public void startEngine(){
        scheduler.scheduleAtFixedRate(()->{
            try{
                attemptMatchOrders();
            }catch(SQLException e){
                System.err.println("Database error during order matching: "+e.getMessage());
            }catch (Exception e){
                System.err.println("Unexpected error during order matching: "+e.getMessage());

            }
        },0,MATCHING_INTERVAL_SECONDS,TimeUnit.SECONDS);
        System.out.println("Order Matching Engine started. Matching orders every "+MATCHING_INTERVAL_SECONDS+" seconds.");
    }
    public void stopEngine(){
        scheduler.shutdown();
        try{
            if(!scheduler.awaitTermination(5,TimeUnit.SECONDS)){
                scheduler.shutdown();
                System.out.println("Order Matching Engine forcefully shut down.");
            }
        }catch (InterruptedException e){
            scheduler.shutdown();
            System.err.println("Order Matching Engine shutdown interrupted and forcefully shut down");
        }
        System.out.println("Order Matching Engine stopped.");
    }

    private void attemptMatchOrders() throws SQLException{
        List<Stock> allStocks=stockService.getAllStocks();

        for(Stock stock: allStocks){
            List<Order> buyOrders=orderService.getPendingBuyOrdersByStockId(stock.getId());
            List<Order> sellOrder=orderService.getPendingSellOrdersByStockId(stock.getId());
            int buyIndex=0;
            int sellIndex=0;
            while(buyIndex<buyOrders.size() && sellIndex<sellOrder.size()){
                Order buyerOrder=buyOrders.get(buyIndex);
                Order sellerOrder=sellOrder.get(sellIndex);

                if(buyerOrder.getPrice().compareTo(sellerOrder.getPrice())>=0){
                    BigDecimal executedPrice;
                    executedPrice=sellerOrder.getPrice();
                    int executedQuantity=Math.min(buyerOrder.getQuantity(), sellerOrder.getQuantity());

                    try{
                        tradeService.executetrade(buyerOrder,sellerOrder,executedPrice,executedQuantity);

                        if(buyerOrder.getQuantity()==executedQuantity){
                            buyIndex++;
                        }else{
                            buyerOrder.setQuantity(buyerOrder.getQuantity()-executedQuantity);
                        }

                        if(sellerOrder.getQuantity()==executedQuantity){
                            sellIndex++;
                        }else{
                            sellerOrder.setQuantity(sellerOrder.getQuantity()-executedQuantity);
                        }
                        System.out.println("Matched "+executedQuantity+" shares of "+stock.getSymbol()+" at "+executedPrice+ "(Buyer: "+buyerOrder.getUserId()+", Seller: "+sellerOrder.getUserId()+")");
                    }catch (SQLException e){
                        System.err.println("Error executing trade for stock "+stock.getSymbol()+": "+e.getMessage());
                    }catch (IllegalArgumentException e){
                        System.err.println("Trade Validation error for stock "+stock.getSymbol()+": "+e.getMessage());
                    }

                }else{
                    break;
                }



            }
        }
    }

}
