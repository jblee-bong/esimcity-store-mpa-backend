package com.naver.naverspabackend.mybatis.mapper;

import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper {

    @Insert(
        "insert into TB_FILE (file_extention,"
                            + " file_save_name,"
                            + " file_original_name,"
                            + " file_path,"
                            + " file_save_path,"
                            + " file_type ) "
            + " values ("
            + "#{fileExtention},"
            + "#{fileSaveName},"
            + "#{fileOriginalName},"
            + "#{filePath},"
            + "#{fileSavePath},"
            + "#{fileType})"
    )
    int insertFileInfo(Map<String, Object> map);

}
