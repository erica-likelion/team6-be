package likelion.sajaboys.soboonsoboon.dto;

import java.util.List;

public class ReceiptDtos {
    public record Item(String name, Integer quantity, Double amount) {
    }

    public record ReceiptParsedResponse(List<Item> items, Double total) {

    }

    public record ReceiptParseRequest(String image) {
    }
}
