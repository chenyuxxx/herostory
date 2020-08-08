package org.tinygame.herostory.login;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.GameMsgDecoder;
import org.tinygame.herostory.MySqlSessionFactory;
import org.tinygame.herostory.login.db.IUserDao;
import org.tinygame.herostory.login.db.UserEntity;

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
    public static LoginService getInstance(){
        return _instance;
    }

    /**
     * 用户登录
     *
     * @param userName 用户名
     * @param password 密码
     * @return 用户实体
     */
    public UserEntity userLogin(String userName,String password){
        if (null == userName || null == password) {
            return null;
        }

        try (SqlSession mySqlSession = MySqlSessionFactory.openSession()){
            //获取Dao
            IUserDao dao = mySqlSession.getMapper(IUserDao.class);

            UserEntity user = dao.getUserById(userName);

            if (null != user) {
                if (!password.equals(user.getPassword())) {
                    LOGGER.error("用户密码错误，错误用户 = {}",userName);

                    throw new RuntimeException("用户密码错误");
                }
            } else {
                //新建用户实体
                user = new UserEntity();
                user.setUserName(userName);
                user.setPassword(password);
                user.setHeroAvatar("Hero_Shaman");

                //将用户实体插入到数据库
                dao.insertInto(user);
            }

            return user;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            return null;
        }
    }
}
