package com.ai.nocodeapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootTest
class NocodeappApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {

    }

    @Test
    void testConnection() throws SQLException {
        // 打印 DataSource 对象，如果成功输出且不为 null，说明连接池配置正确
        System.out.println("数据库连接信息：" + dataSource.getConnection());
    }

}
