import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class JmsBackEnd implements Backend, AutoCloseable {

    public Session session;
    private boolean closed;
    private final ReentrantReadWriteLock closeLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = closeLock.writeLock();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Queue queue;
    private final MessageProducer producer;
    private final MessageProducer dlqProducer;
    private final List<MessageConsumer> consumers = new ArrayList<>();

    public JmsBackEnd(Session session, String queueName, String dlqQueueName) throws JMSException {
        this.session = session;
        this.closed = false;

        this.queue = session.createQueue(queueName);
        this.producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);

        Queue dlqQueue = session.createQueue(dlqQueueName);
        this.dlqProducer = session.createProducer(dlqQueue);
        dlqProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
    }


    @Override
    public void publish(Executor executor) throws JMSException, JsonProcessingException {
        System.out.println(executor.getType());
        switch (executor.getType()) {
            case "Schedule", "Retry":
                producer.send(encode(executor.getTransaction()));
                break;
            case "Abort":
                dlqProducer.send(encode(executor.getTransaction()));
                break;
            default:
                break;
        }
    }

    @Override
    public void subscribe(Function<Transaction, Executor> deliver) {
        MessageListener listener = message -> {
            if (!(message instanceof BytesMessage bytesMessage)) {
                return;
            }

            Transaction transaction;
            try {
                transaction = decode(bytesMessage).copy(bytesMessage.getIntProperty("JMSXDeliveryCount"));
            } catch (Exception e) {
                return;
            }

            Executor executor = deliver.apply(transaction);
            if (!(executor instanceof Executor.Retry)) {
                try {
                    message.acknowledge();
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        for (int i = 0; i <= 1; i++) {
            try {
                MessageConsumer consumer = session.createConsumer(queue);
                consumer.setMessageListener(listener);
                consumers.add(consumer);
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void unsubscribe() throws JMSException {
        for (MessageConsumer consumer : consumers) {
            consumer.close();
        }
    }

    @Override
    public void close() throws Exception {
        writeLock.lock();
        try {
            if (!closed) {
                unsubscribe();
                closed = true;
            }
        } finally {
            writeLock.unlock();
        }
    }

    private BytesMessage encode(Transaction transaction) throws JsonProcessingException, JMSException {
        return toByteMessage(mapper.writeValueAsBytes(transaction));
    }

    public Transaction decode(BytesMessage message) throws IOException, JMSException {
        byte[] bytes = toByteArray(message);
        return mapper.readValue(bytes, Transaction.class);
    }

    private static byte[] toByteArray(BytesMessage message) throws JMSException {
        byte[] bytes = new byte[(int) message.getBodyLength()];
        message.readBytes(bytes);
        return bytes;
    }

    private BytesMessage toByteMessage(byte[] byteArray) throws JMSException {
        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(byteArray);
        return bytesMessage;
    }

}
