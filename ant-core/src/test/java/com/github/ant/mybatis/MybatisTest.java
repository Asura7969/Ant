package com.github.ant.mybatis;

import com.github.ant.mybatis.mapper.TaskMapper;
import com.github.ant.mybatis.model.Task;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class MybatisTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void before() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void taskTest() throws IOException {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        //使用Mapper
        TaskMapper taskMapper = sqlSession.getMapper(TaskMapper.class);
        Task task = taskMapper.selectByPrimaryKey(1L);
        System.out.println(task);
        sqlSession.close();
    }

}
