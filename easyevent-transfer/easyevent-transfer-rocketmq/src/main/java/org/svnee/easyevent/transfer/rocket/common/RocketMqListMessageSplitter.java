package org.svnee.easyevent.transfer.rocket.common;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.rocketmq.common.message.Message;

/**
 * RocketMQ List Message Splitter
 *
 * @author svnee
 */
public class RocketMqListMessageSplitter implements Iterator<List<Message>> {

    private final int sizeLimit;
    private final List<Message> messages;
    private int currIndex;

    public RocketMqListMessageSplitter(List<Message> messages, int sizeLimit) {
        this.messages = messages;
        this.sizeLimit = sizeLimit;
    }

    @Override
    public boolean hasNext() {
        return currIndex < messages.size();
    }

    @Override
    public List<Message> next() {
        int nextIndex = currIndex;
        int totalSize = 0;
        for (; nextIndex < messages.size(); nextIndex++) {
            Message message = messages.get(nextIndex);
            int tmpSize = message.getTopic().length() + message.getBody().length;
            Map<String, String> properties = message.getProperties();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                tmpSize += entry.getKey().length() + entry.getValue().length();
            }
            //for log overhead
            tmpSize = tmpSize + 20;
            if (tmpSize > sizeLimit) {
                //it is unexpected that single message exceeds the sizeLimit
                //here just let it go, otherwise it will block the splitting process
                if (nextIndex - currIndex == 0) {
                    //if the next sublist has no element, add this one and then break, otherwise just break
                    nextIndex++;
                }
                break;
            }
            if (tmpSize + totalSize > sizeLimit) {
                break;
            } else {
                totalSize += tmpSize;
            }

        }
        List<Message> subList = messages.subList(currIndex, nextIndex);
        currIndex = nextIndex;
        return subList;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not allowed to remove");
    }
}