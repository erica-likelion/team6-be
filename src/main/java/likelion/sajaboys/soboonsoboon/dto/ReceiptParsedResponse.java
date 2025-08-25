package likelion.sajaboys.soboonsoboon.dto;

import java.util.List;

public class ReceiptParsedResponse {

    public static class Item {
        public String name;
        public Integer quantity;
        public Double amount;

        public Item() {
        }

        public Item(String name, Integer quantity, Double amount) {
            this.name = name;
            this.quantity = quantity;
            this.amount = amount;
        }
    }

    public List<Item> items;
    public Double total;

    public ReceiptParsedResponse() {
    }

    public ReceiptParsedResponse(List<Item> items, Double total) {
        this.items = items;
        this.total = total;
    }
}
