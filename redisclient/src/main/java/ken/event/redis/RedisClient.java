package ken.event.redis;

import java.util.Set;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author KennyZJ
 * 
 */
public class RedisClient {

	public static Logger LOG = Logger.getLogger(RedisClient.class);

	private final static String REDISHOST = "localhost";
	
	private final static JedisPool pool = new JedisPool(new JedisPoolConfig(),
			REDISHOST);

	private Jedis jedis;

	private RedisClient(Jedis jedis) {
		this.jedis = jedis;
	}

	public static RedisClient getClient() {
		return new RedisClient(pool.getResource());
	}

	public Set<String> getMembers(String setname) {
		if (setname == null || "".equals(setname)) {
			throw new NullPointerException("Not specify set_name!");
		}
		if (jedis == null || !jedis.isConnected()) {
			throw new NullPointerException("Not connect to Redis!");
		}
		return jedis.smembers(setname);
	}

	public void returnClient() {
		pool.returnResource(jedis);
	}

}
