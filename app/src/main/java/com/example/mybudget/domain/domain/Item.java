package com.example.mybudget.domain.domain;

/**
 * Item class - Holds all the data of added expense
 */
public class Item {

    private String category;
    private double amount;
    private String date;
    private String payDate;
    private String currency;
    private String description = "";

    public static Item copyItemWithAmount(Item item, double amount) {
        return new Builder(item.category, item.date, item.payDate)
                .withCurrency(item.currency)
                .withDescription(item.description)
                .withAmount(amount)
                .build();
    }

    public static Item copyItemWithDescription(Item item, String description) {
        return new Builder(item.category, item.date, item.payDate)
                .withCurrency(item.currency)
                .withDescription(description)
                .withAmount(item.amount)
                .build();
    }

    public static class Builder{
        private final String category;
        private final String date;
        private final String payDate;
        private double amount;
        private String currency;
        private String description = "";

        public Builder(String category, String date, String payDate){
            this.category = category;
            this.date = date;
            this.payDate = payDate;
        }

        public Builder withDescription(String description){
            this.description = description;
            return this;
        }

        public Builder withAmount(double amount){
            this.amount = amount;
            return this;
        }

        public Builder withCurrency(String currency){
            this.currency = currency;
            return this;
        }

        public Item build(){
            Item item = new Item();  //Since the builder is in the BankAccount class, we can invoke its private constructor.
            item.date = this.date;
            item.amount = this.amount;
            item.category = this.category;
            item.payDate = this.payDate;
            item.currency = this.currency;
            item.description = this.description;
            return item;
        }
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getPayDate() {
        return payDate;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }
}
