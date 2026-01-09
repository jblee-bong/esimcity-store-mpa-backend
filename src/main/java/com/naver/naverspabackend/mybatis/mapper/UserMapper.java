package com.naver.naverspabackend.mybatis.mapper;

import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.dto.UserStoreMappDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("select * from TB_USER where email = #{id}")
    Optional<UserDto> findById(@Param("id") String id);

    /* xml */
    int createUser(UserDto entity);

    int selectUserListCnt(Map<String, Object> paramMap);

    List<UserDto> selectUserList(Map<String, Object> paramMap);

    int deleteUser(UserDto entity);

    void createUserStoreMapp(UserStoreMappDto userStoreMappDto);

    List<Map<String, String>> findUserStoreMappByEmail(String email);

    int updateUser(UserDto entity);

    void deleteUserStoreMapp(String email);
}
