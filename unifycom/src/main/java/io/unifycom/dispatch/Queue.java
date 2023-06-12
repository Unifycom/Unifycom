package io.unifycom.dispatch;

import io.unifycom.Channel;

public interface Queue {

    String RING = "RING";
    String BLOCKING = "BLOCKING";

    void put(Channel channel, Object object) throws Exception;

    void close();

    class Element {

        private Object object;
        private Channel channel;

        public Element() {

        }

        public Element(Channel channel, Object object) {

            this.object = object;
            this.channel = channel;
        }

        public Object getObject() {

            return object;
        }

        public void setObject(Object object) {

            this.object = object;
        }

        public Channel getChannel() {

            return channel;
        }

        public void setChannel(Channel channel) {

            this.channel = channel;
        }

        @Override
        public String toString() {

            return String.format("%s in %s", object, channel == null ? "null" : channel.getName());
        }
    }
}
