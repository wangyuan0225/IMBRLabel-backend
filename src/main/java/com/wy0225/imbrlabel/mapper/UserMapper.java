package com.wy0225.imbrlabel.mapper;

import com.wy0225.imbrlabel.pojo.DO.UserDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from user where username=#{username}")
    UserDO getByUsername(String username);

    @Insert("insert into `imbr-label`.user(username, nickname, password)"+
           "values "+
            "(#{username},#{nickname},#{password})")
    void insert(UserDO userDO);
}
