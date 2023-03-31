package org.erym.im.service;

import org.erym.im.common.model.dto.ResultInfo;
import org.erym.im.common.model.entity.User;
import org.erym.im.common.model.vo.UserVo;

import java.util.List;

/**
 * @author cjt
 * @date 2021/6/21 0:15
 */
public interface FriendService {

    ResultInfo<List<User>> findMutualFriend(Integer userId, Integer friendId);

    ResultInfo<List<User>> findRecommendFriend(Integer userId);

    ResultInfo<List<UserVo>> findAllFriend(Integer userId);

    ResultInfo<?> addFriend(Integer userId, String addUserName);

    ResultInfo<List<User>> getRequest(Integer userId);

    ResultInfo<?> agreeRequest(Integer userId, Integer addId);

    ResultInfo<?> refuseRequest(Integer userId, Integer addId);

    ResultInfo<?> deleteFriend(Integer userId, Integer friendId);

}
