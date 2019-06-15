package com.rayaan.dupcloudconsumer;
import BigTable.BTPublisher;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
//import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.util.Bytes;

public class PubSubSubscriber {
    // use the default project id
    //private static final String PROJECT_ID = ServiceOptions.getDefaultProjectId();
    private static final String PROJECT_ID = "ambient-polymer-228615";

    private static final BlockingQueue<PubsubMessage> messages = new LinkedBlockingDeque<>();

    private static final Logger LOGGER = Logger.getLogger(PubSubSubscriber.class.getName());

    static class MessageReceiverExample implements MessageReceiver {

        @Override
        public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
            System.out.println("received message");
            messages.offer(message);
            consumer.ack();
        }
    }

    /** Receive messages over a subscription. */
    public static void main(String... args) throws Exception {
        // set subscriber id, eg. my-sub
        String subscriptionId = "FileContentTopicSubscription";//args[0];
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
                PROJECT_ID, subscriptionId);
        //System.out.println("1");
        Subscriber subscriber = null;
        BTPublisher btPublisher = new BTPublisher();
        //System.out.println("2");
        try {
            // create a subscriber bound to the asynchronous message receiver
            subscriber = Subscriber.newBuilder(subscriptionName, new MessageReceiverExample()).build();
            subscriber.startAsync().awaitRunning();
            //System.out.println("3");
            // Continue to listen to messages
            while (true) {
                PubsubMessage message = messages.take();
                System.out.println("Message Id: " + message.getMessageId());
                String data = message.getData().toStringUtf8();
                System.out.println("Data: " + data);
                final byte[] Data = Bytes.toBytes(data);
                String result = btPublisher.PublishData(Data);
                System.out.println("Result : " + result);

            }
        }finally {
            if (subscriber != null) {
                System.out.println("finally");
                subscriber.stopAsync();
            }
        }
    }
}
