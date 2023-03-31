package org.erym.im.service.impl;

import org.erym.im.common.constant.CodeEnum;
import org.erym.im.common.exception.ConditionException;
import org.erym.im.common.model.dto.ResultInfo;
import org.erym.im.common.model.entity.Admin;
import org.erym.im.common.model.entity.Ban;
import org.erym.im.common.model.entity.User;
import org.erym.im.common.model.vo.UserVo;
import org.erym.im.common.util.DateUtil;
import org.erym.im.common.util.TokenUtil;
import org.erym.im.mapper.AdminMapper;
import org.erym.im.service.AdminService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author cjt
 * @date 2022/11/2 22:13
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String ONLINE_USER = "online_user";
    private static final String BAN_USER = "ban_user:";

    @Override
    public ResultInfo<JSONObject> login(String name, String password) throws Exception {
        if (name.length() == 0 || password.length() == 0){
            return ResultInfo.error(400, "请填写完整信息");
        }
        Admin admin = adminMapper.login(name, password);
        if (admin == null){
            return ResultInfo.error(400, "用户名或密码错误");
        }
        Integer id = admin.getId();
        String accessToken = TokenUtil.generateAccessToken(Long.valueOf(id));
        String refreshToken = TokenUtil.generateRefreshToken(Long.valueOf(id));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accessToken", accessToken);
        jsonObject.put("refreshToken", refreshToken);
        jsonObject.put("admin", admin);
        return ResultInfo.success(CodeEnum.SUCCESS, jsonObject);
    }

    @Override
    public ResultInfo<JSONObject> refreshAccessToken(String refreshToken) throws Exception {
        Long id = TokenUtil.verifyToken(refreshToken);
        Admin admin = adminMapper.getAdminById(id);
        if (admin == null) {
            throw new ConditionException("认证token失败");
        }
        //更新双token
        String accessToken = TokenUtil.generateAccessToken(id);
        String newRefreshToken = TokenUtil.generateRefreshToken(id);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accessToken", accessToken);
        jsonObject.put("refreshToken", newRefreshToken);
        return ResultInfo.success(CodeEnum.SUCCESS, jsonObject);
    }


    @Override
    public ResultInfo<List<UserVo>> getAllUser() {
        List<User> allUser = adminMapper.getAllUser();
        List<UserVo> allUserVo = new LinkedList<>();
        for(User user: allUser){
            UserVo userVo = new UserVo();
            userVo.setUser(user);
            Boolean status = redisTemplate.opsForSet().isMember(ONLINE_USER, String.valueOf(user.getUserId()));
            userVo.setStatus(Boolean.TRUE.equals(status));
            allUserVo.add(userVo);
        }
        return ResultInfo.success(CodeEnum.SUCCESS, allUserVo);
    }

    /**
     * 用户禁言
     */
    @Override
    public ResultInfo<String> banUser(Integer userId, String daysStr) {
        try {
            Integer days = Integer.valueOf(daysStr);

            Ban ban = Ban.builder().userId(userId).days(days)
                    .startTime(DateUtil.getDate()).endTime(DateUtil.getDate(days)).build();
            adminMapper.insertBan(ban);

            redisTemplate.opsForValue().set(BAN_USER + userId, ban.getEndTime().toString());
            redisTemplate.expire(BAN_USER + userId, days, TimeUnit.DAYS);

            return ResultInfo.success(CodeEnum.SUCCESS, ban.getEndTime().toString());
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResultInfo.error(CodeEnum.PARAM_PATTERN_INVALID);
        }
    }

    @Override
    public ResultInfo<String> unBan(Integer userId) {
        redisTemplate.delete(BAN_USER + userId);
        return ResultInfo.success(CodeEnum.SUCCESS);
    }

}
