package com.livehouse.common.constants;

public class RabbitMqConstants {

    private RabbitMqConstants() {
    }

    public static final String LIVEHOUSE_EXCHANGE = "livehouse.topic";

    public static final String DEAD_LETTER_EXCHANGE = "livehouse.dlx";

    public static final String MESSAGE_NOTICE_QUEUE = "livehouse.message.notice";

    public static final String LIVE_EVENT_QUEUE = "livehouse.live.event";

    public static final String DEAD_LETTER_QUEUE = "livehouse.dead.letter";

    public static final String MESSAGE_NOTICE_ROUTING_KEY = "message.notice";

    public static final String LIVE_EVENT_ROUTING_KEY = "live.event";

    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter";
}
