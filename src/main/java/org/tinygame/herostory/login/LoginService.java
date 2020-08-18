package org.tinygame.herostory.login;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.GameMsgDecoder;
import org.tinygame.herostory.MySqlSessionFactory;
import org.tinygame.herostory.async.AsyncOperationProcessor;
import org.tinygame.herostory.async.IAsyncOperation;
import org.tinygame.herostory.login.db.IUserDao;
import org.tinygame.herostory.login.db.UserEntity;
import org.tinygame.herostory.util.RedisUtil;
import redis.clients.jedis.Jedis;

import java.util.function.Function;

/**
 * 登录服务
 */
public final class LoginService {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    /**
     * 单例对象
     */
    private static LoginService _instance = new LoginService();

    /**
     * 私有化类默认构造器
     */
    private LoginService() {
    }

    /**
     * 获取单例对象
     *
     * @return
     */
    public static LoginService getInstance() {
        return _instance;
    }

    /**
     * 用户登录
     *
     * @param userName 用户名
     * @param password 密码
     * @param callback 回调函数
     * @return 用户实体
     */
    public void userLogin(String userName, String password, Function<UserEntity, Void> callback) {
        if (null == userName || null == password) {
            return;
        }

        AsyncOperationProcessor.getInstance().process(new AsyncGetUserByName(userName, password) {

            @Override
            public void doFinish() {
                if (null != callback) {
                    callback.apply(this.getUserEntity());
                }
            }
        });

    }

    /**
     * 更新用户基本信息
     *
     * @param userEntity 用户实体
     */
    private void updateUserBasicInfoInRedis(UserEntity userEntity) {
        if (null == userEntity) {
            return;
        }

        try (Jedis redis = RedisUtil.getRedis()) {
            //获取用户id
            int userId = userEntity.getUserId();
            String userName = userEntity.getUserName();
            String userHeroAvatar = userEntity.getHeroAvatar();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId",userId);
            jsonObject.put("userName",userName);
            jsonObject.put("userHeroAvatar",userHeroAvatar);

            //更新 redis 数据
            redis.hset("User_" + userEntity.getUserId(),"BasicInfo",jsonObject.toJSONString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
    }

    /**
     * 异步方式获取用户
     */
    private class AsyncGetUserByName implements IAsyncOperation {

        /**
         * 用户名称
         */
        private final String _userName;

        /**
         * 密码
         */
        private final String _password;

        /**
         * 用户实体
         */
        private UserEntity _userEntity = null;

        /**
         * @param _userName 用户名称
         * @param _password 密码
         */
        public AsyncGetUserByName(String _userName, String _password) {
            this._userName = _userName;
            this._password = _password;
        }

        /**
         * 获取用户实体
         *
         * @return 用户实体
         */
        public UserEntity getUserEntity() {
            return _userEntity;
        }

        @Override
        public int bindId() {
            //根据用户名最后一个字母拿到了 bindId
            return _userName.charAt(_userName.length() - 1);
        }

        @Override
        public void doAsync() {
            try (SqlSession mySqlSession = MySqlSessionFactory.openSession()) {
                //获取Dao
                IUserDao dao = mySqlSession.getMapper(IUserDao.class);

                UserEntity userEntity = dao.getUserById(_userName);

                if (null != userEntity) {
                    if (!_password.equals(userEntity.getPassword())) {
                        LOGGER.error("用户密码错误，错误用户 = {}", _userName);
                    }
                } else {
                    //新建用户实体
                    userEntity = new UserEntity();
                    userEntity.setUserName(_userName);
                    userEntity.setPassword(_password);
                    userEntity.setHeroAvatar("Hero_Shaman");

                    //将用户实体插入到数据库
                    dao.insertInto(userEntity);
                }

                _userEntity = userEntity;

                //登陆时将用户信息更新进 redis
                LoginService.getInstance().updateUserBasicInfoInRedis(userEntity);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}
