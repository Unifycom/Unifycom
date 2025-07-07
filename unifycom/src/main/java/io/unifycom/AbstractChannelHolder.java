package io.unifycom;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractChannelHolder {

    protected Map<String, Channel> channels = new ConcurrentHashMap<>();

    public void put(Channel channel) {

        String key = getKey(channel);
        channels.put(key, channel);
    }

    public Channel remove(Channel channel) {

        String key = getKey(channel);
        return channels.remove(key);
    }

    public Channel getByName(String channelName) {

        return channels.values().stream().filter(ch -> channelName.equals(ch.getName())).findFirst().orElse(null);
    }

    public Collection<Channel> all() {

        return channels.values();
    }

    public abstract String getKey(Channel channel);
}
