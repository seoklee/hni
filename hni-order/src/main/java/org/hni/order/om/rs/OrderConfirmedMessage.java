package org.hni.order.om.rs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class OrderConfirmedMessage {

    private final String text = "A meal has been confirmed";

    private List<OrderConfirmedMessageAttachment> attachments = new ArrayList<>();

    @JsonProperty("unfurl_links")
    private Boolean unfurlLink = false;

    @JsonProperty("unfurl_media")
    private Boolean unfurMedia = false;

    public OrderConfirmedMessage() {
    }

    public OrderConfirmedMessage(OrderConfirmedMessageAttachment attachment) {
        attachments.add(attachment);
    }

    public String getText() {
        return text;
    }

    public List<OrderConfirmedMessageAttachment> getAttachments() {
        return attachments;
    }

    public Boolean getUnfurlLink() {
        return unfurlLink;
    }

    public Boolean getUnfurMedia() {
        return unfurMedia;
    }

    public static class OrderConfirmedMessageAttachment {

        private String fallback;

        private final String color = "#36a64f";

        private String title;

        @JsonProperty("title_link")
        private String titleLink;

        @JsonProperty("fields")
        private List<AttachementField> fields;

        private final String footer = "HNI Server";

        @JsonProperty("ts")
        private Long timestamp;

        public OrderConfirmedMessageAttachment() {
        }

        public OrderConfirmedMessageAttachment(String userName, String titleLink, String orderedItem, String orderedQuantity, Long timestamp) {
            title = "Please place the order for " + userName;
            this.titleLink = titleLink;
            fields = new ArrayList<>(3);
            fields.add(new AttachementField("Ordered item", orderedItem));
            fields.add(new AttachementField("Ordered quantity", orderedQuantity));
            this.timestamp = timestamp / 1000;
        }

        public String getFallback() {
            return fallback;
        }

        public String getColor() {
            return color;
        }

        public String getTitle() {
            return title;
        }

        public String getFooter() {
            return footer;
        }

        public String getTitleLink() {
            return titleLink;
        }

        public List<AttachementField> getFields() {
            return fields;
        }

        public Long getTimestamp() {
            return timestamp;
        }
    }

    private static class AttachementField {

        private String title;
        private String value;
        @JsonProperty("short")
        private final boolean shortDisplay = true;

        public AttachementField() {
        }

        public AttachementField(String title, String value) {
            this.title = title;
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }

        public boolean isShortDisplay() {
            return shortDisplay;
        }
    }
}
