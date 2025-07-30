package com.example.stocks;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class PortfolioService {
    private final PortfolioDAO portfolioDAO;
    private final UserDAO userDAO;
    private final StockDAO stockDAO;

    public  PortfolioService(PortfolioDAO portfolioDAO,UserDAO userDAO,StockDAO stockDAO){
        this.portfolioDAO=portfolioDAO;
        this.userDAO=userDAO;
        this.stockDAO=stockDAO;
    }

    public List<PortfolioDetail> getUserPortfolio(int userId) throws SQLException,IllegalArgumentException{
        Optional<User> userOptional=userDAO.findById(userId);
        if(!userOptional.isPresent()){
            throw new IllegalArgumentException("user with ID "+userId+" not found");
        }
        List<PortfolioItem> items=portfolioDAO.findByUserId(userId);
        List<PortfolioDetail> portfolioDetails=new ArrayList<>();
        for(PortfolioItem item:items){
            Optional<Stock> stockOptional=stockDAO.findById(item.getStockId());
            if(stockOptional.isPresent()){
                Stock stock=stockOptional.get();
                portfolioDetails.add(new PortfolioDetail(stock.getSymbol(),stock.getCompanyName(),item.getQuantity(),stock.getCurrentPrice()));
            }else{
                System.err.println("Warning: Stock with Id "+item.getStockId()+" not found for user "+ userId+" in portfolio");
            }
        }
        return portfolioDetails;
    }



    public static class PortfolioDetail{
        private final String stockSymbol;
        private final String companyName;
        private final int quantity;
        private final BigDecimal currentPrice;

        public PortfolioDetail(String stockSymbol,String companyName,int quantity,BigDecimal currentPrice){
            this.stockSymbol=stockSymbol;
            this.companyName=companyName;
            this.quantity=quantity;
            this.currentPrice=currentPrice;
        }

        public String getStockSymbol(){
            return stockSymbol;
        }
        public String getCompanyName(){
            return companyName;
        }
        public int getQuantity(){
            return quantity;
        }
        public BigDecimal getCurrentPrice(){
            return currentPrice;
        }
        public BigDecimal getTotalValue(){
            return currentPrice.multiply(new BigDecimal(quantity));
        }
        @Override
        public String toString(){
            return "PortfolioDetail{" +
                    "stockSymbol='" + stockSymbol + '\'' +
                    ", companyName='" + companyName + '\'' +
                    ", quantity=" + quantity +
                    ", currentPrice=" + currentPrice +
                    '}';
        }
    }

}
