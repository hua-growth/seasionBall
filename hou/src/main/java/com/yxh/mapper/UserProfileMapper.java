package com.yxh.mapper;

import com.yxh.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileMapper {
    UserProfile findById(@Param("id") Long id);
    
    UserProfile findByNickname(@Param("nickname") String nickname);
    
    void insert(UserProfile userProfile);
    
    void update(UserProfile userProfile);
}