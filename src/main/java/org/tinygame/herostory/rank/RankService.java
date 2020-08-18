package org.tinygame.herostory.rank;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.async.AsyncOperationProcessor;
import org.tinygame.herostory.async.IAsyncOperation;
import org.tinygame.herostory.util.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 排行榜服务
 */
public class RankService {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(RankService.class);

    /**
     * 单例对象
     */
    private static RankService _instance = new RankService();

    /**
     * 私有化类默认构造器
     */
    private RankService() {
    }

    /**
     * 获取单例对象
     *
     * @return
     */
    public static RankService getInstance() {
        return _instance;
    }

    /**
     * 获取排名列表
     *
     * 返回值为 void ，不直接返回查询出来的json，因为 redis 操作也是 I/O 操作，应该进入 I/O 线程执行
     * 回调方法中传入的 List<?>，List 中需要的参数 其实是 RankItem 类，但是不能直接用 GameMsgProtocol 的 RankItem，因为 RankService 是
     * 业务代码，业务代码不需要关心消息，所以需要建立一个 BO/VO 这种业务对象传接数据
     *
     * @param callback 回调函数
     */
    public void getRank(Function<List<RankItem>, Void> callback) {
        if (null == callback) {
            return;
        }

        AsyncOperationProcessor.getInstance().process(new AsyncGetRank() {
            @Override
            public void doFinish() {
                callback.apply(this.getRankItem());
            }
        });
    }

    /**
     * 异步方式获取排名列表
     */
    private class AsyncGetRank implements IAsyncOperation {

        /**
         * 排名列表
         */
        private List<RankItem> _rankItemList = null;

        /**
         * 获取排名列表
         *
         * @return 排名列表
         */
        public List<RankItem> getRankItem () {
            return _rankItemList;
        }

        @Override
        public void doAsync() {
            try (Jedis redis = RedisUtil.getRedis()) {
                //获取字符串值集合
                Set<Tuple> valSet = redis.zrangeWithScores("Rank",0,9);

                int rankId = 0;

                List<RankItem> rankItemList = new ArrayList<>();

                for (Tuple t : valSet) {
                    //获取用户 id
                    int userId = Integer.parseInt(t.getElement());

                    //获取用户基本信息
                    String jsonStr = redis.hget("User_" + userId,"BasicInfo");
                    if (null == jsonStr ||
                        jsonStr.isEmpty()) {
                        continue;
                    }

                    JSONObject jsonObject = JSONObject.parseObject(jsonStr);

                    RankItem newItem = new RankItem();

                    newItem.setRankId(rankId);
                    newItem.setUserId(userId);
                    newItem.setUserName(jsonObject.getString("userName"));
                    newItem.setHeroAvatar(jsonObject.getString("userHeroAvatar"));
                    newItem.setWin((int)t.getScore());

                    rankItemList.add(newItem);
                }

                _rankItemList = rankItemList;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        }
    }

    /**
     * 刷新排行榜
     *
     * @param winId 赢家id
     * @param loserId 输家id
     */
    public void refreshRank(int winId,int loserId) {
        try (Jedis redis = RedisUtil.getRedis()){
            //增加用户的输赢次数
            redis.hincrBy("User_" + winId,"Win" , 1);
            redis.hincrBy("User_" + loserId,"Lose" , 1);

            //查看赢家的获胜次数
            String winStr = redis.hget("User_" + winId,"Win");
            int winInt = Integer.parseInt(winStr);

            //修改排行榜
            redis.zadd("Rank",winInt,String.valueOf(winId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
