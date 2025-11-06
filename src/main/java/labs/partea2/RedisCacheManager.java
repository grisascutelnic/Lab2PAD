package labs.partea2;

import redis.clients.jedis.Jedis;

public class RedisCacheManager {
    private final Jedis jedis;

    public RedisCacheManager() {
        this.jedis = new Jedis("localhost", 6379); // asigură-te că Redis e pornit
    }

    public String get(String key) {
        return jedis.get(key);
    }

    public void set(String key, String value, int ttlSeconds) {
        jedis.setex(key, ttlSeconds, value);
    }
}
