package ken.event.management;

import java.util.Set;

import ken.event.redis.RedisClient;

/**
 * @author KennyZJ
 * 
 */
public class RedisLocator extends ELocator {

	private RedisClient redis;

	public RedisLocator() {
		redis = RedisClient.getClient();
	}

	@Override
	public Set<String> lookup(String eventType) {
		return redis.getMembers(eventType + RedisConsts.E_FOLLOWER_SURFIX); 
	}

	@Override
	public void release() {
		redis.returnClient();
	}
	
	

}
