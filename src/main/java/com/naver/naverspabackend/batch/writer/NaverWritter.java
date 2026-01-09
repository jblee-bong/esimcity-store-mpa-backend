package com.naver.naverspabackend.batch.writer;

import com.naver.naverspabackend.dto.MatchInfoDto;
import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.ProductOptionDto;
import com.naver.naverspabackend.mybatis.mapper.MatchInfoMapper;
import com.naver.naverspabackend.mybatis.mapper.OrderMapper;
import com.naver.naverspabackend.mybatis.mapper.ProductMapper;
import com.naver.naverspabackend.mybatis.mapper.ProductOptionMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class NaverWritter {

    @Autowired
    @Qualifier("batchDbSessionTemplate")
    private SqlSessionTemplate batchsqlSessionTemplate;

    @Autowired
    private MatchInfoMapper matchInfoMapper;
    /**
     * product와 하위 옵션 create update delete
     * @param productDtoList
     * @param productOptionDtoList
     */
    public void productWrite(List<ProductDto> productDtoList, List<ProductOptionDto> productOptionDtoList){

        SqlSession sqlSession = batchsqlSessionTemplate.getSqlSessionFactory().openSession();
        ProductMapper productMapper =  sqlSession.getMapper(ProductMapper.class);
        ProductOptionMapper productOptionMapper =  sqlSession.getMapper(ProductOptionMapper.class);

        try {
            for (ProductDto productDto : productDtoList) {
                if(productDto.isDeleteFlag()){
                    productMapper.deleteProduct(productDto);
                }else if(productDto.isUpdateFlag()){
                    productMapper.updateProduct(productDto);
                }else if(productDto.isInsertFlag()){
                    productMapper.insertProduct(productDto);
                }
            }

            sqlSession.flushStatements();
            sqlSession.commit();

            for (ProductOptionDto productOptionDto : productOptionDtoList) {
                if(productOptionDto.isDeleteFlag()){
                    productOptionMapper.deleteProductOption(productOptionDto);
                }else if(productOptionDto.isUpdateFlag()){
                    productOptionMapper.updateProductOption(productOptionDto);
                }else if(productOptionDto.isInsertFlag()){
                    productOptionMapper.insertProductOption(productOptionDto);
                }
            }

            sqlSession.flushStatements();
            sqlSession.commit();

        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            sqlSession.close();
        }

    }

    public void orderWrite(List<OrderDto> orderDtoList){
        SqlSession sqlSession = batchsqlSessionTemplate.getSqlSessionFactory().openSession();
        OrderMapper orderMapper =  sqlSession.getMapper(OrderMapper.class);
        ProductOptionMapper productOptionMapper =  sqlSession.getMapper(ProductOptionMapper.class);

        try {
            for (OrderDto orderDto : orderDtoList) {
                if(orderDto.isInsertFlag()){
                    if(orderDto.getSendStatus()!=null && (orderDto.getSendStatus().equals("4") || orderDto.getSendStatus().equals("5") || orderDto.getSendStatus().equals("6") || orderDto.getSendStatus().equals("7"))){
                        Map<String,Object> param = new HashMap<>();
                        param.put("id",orderDto.getId());
                        OrderDto orderDto1 = orderMapper.adSelectOrder(param);
                        if(orderDto1!=null)
                            if(orderDto1.getSendStatus()!=null && !orderDto1.getSendStatus().equals("4") && !orderDto1.getSendStatus().equals("5") && !orderDto1.getSendStatus().equals("6") && !orderDto1.getSendStatus().equals("7"))
                                orderMapper.updateBeforeStatus(orderDto);
                    }

                    MatchInfoDto matchInfoDto = matchInfoMapper.selectMatchInfoByOrder(orderDto);
                    //0: 발주처리필요, 1:발주처리완료, 2:발주처리불필요
                    if(matchInfoDto!=null)
                        orderDto.setOrderingUseStatus(matchInfoDto.getOrderingUseYn().equals("Y")?0:2);
                    else
                        orderDto.setOrderingUseStatus(2);
                    orderMapper.insertOrder(orderDto);




                }
            }
            sqlSession.flushStatements();
            sqlSession.commit();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            sqlSession.close();
        }
    }

}
