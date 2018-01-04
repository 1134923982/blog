package cn.derker.blog;

import org.apache.commons.io.IOUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 入口
 *
 * @author derker
 */
@SpringBootApplication
@MapperScan("cn.derker.blog.dao.mapper")
public class Application implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Resource
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... strings) {
        initTables();
    }

    /**
     * 初始化数据库表
     */
    private void initTables() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String sql : readInitSqlFile().split(";")) {
                statement.addBatch(sql);
            }
            statement.executeBatch();
            connection.commit();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取init.sql脚本文件
     *
     * @return 文件内容
     */
    private String readInitSqlFile() throws IOException {
        String content = IOUtils.toString(new InputStreamReader(getClass().getResourceAsStream("/init.sql")));
        LOGGER.info("read data from init.sql:\n{}", content);
        return content;
    }
}