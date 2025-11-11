package com.example.demo.config;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration

//Entity가 있는 위치를 스캐닝
@EntityScan(basePackages = {"com.example.demo.domain.entity"}) //엔티티 찾기 위한 경로 설정
//JpaRepository 위치 찾기
@EnableJpaRepositories(
        basePackages = {"com.example.demo.domain.repository"},
        transactionManagerRef = "jpaTransactionManager"
        //레파지토리들이 TX manager를 참조해서 TX처리가 될 수 있도록 함
)
public class JpaConfig {
    
    //Datasource 의존주입
    @Autowired
    private DataSource dataSource;

    @Bean
                 //FactoryBean을 이용해서 여러 엔터티를 Bean으로 만듦
    LocalContainerEntityManagerFactoryBean entityManagerFactory() { //이름 동일해야함!!
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setPackagesToScan("com.example.demo.domain.entity");

        Properties jpaProperties = new Properties();


        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");                         // 필요에 따라 'create', 'validate' 등으로 변경
//        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect"); // 사용 중인 DB에 맞게 변경
        properties.put("hibernate.show_sql", true);
//        properties.put("hibernate.format_sql", true);
//
//        properties.put("hibernate.hibernate.jdbc.batch_size", 1000);
//        properties.put("hibernate.hibernate.order_inserts", true);
//        properties.put("hibernate.order_updates", true);
//        properties.put("hibernate.jdbc.batch_versioned_data", true);
        entityManagerFactoryBean.setJpaPropertyMap(properties);

        return entityManagerFactoryBean;
    }


    //애플리케이션 시작 시 데이터베이스 초기화
//    @Bean
//    public DataSourceInitializer dataSourceInitializer() {
//        DataSourceInitializer initializer = new DataSourceInitializer();
//        initializer.setDataSource(dataSource);
//        initializer.setDatabasePopulator(databasePopulator());
//        return initializer;
//    }
//
//    //schema.sql과 data.sql 스크립트를 실행
//    private DatabasePopulator databasePopulator() {
//        //Spring Framework에서 제공하는 클래스로, 외부 리소스에 정의된 SQL 스크립트를 사용하여 데이터베이스를 초기화하거나 정리하는 데 사용
//        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
//        //src/main/resources 디렉토리에 위치한 SQL 스크립트를 로드
//        Resource schemaScript = new ClassPathResource("schema.sql");
//        Resource dataScript = new ClassPathResource("data.sql");
//        populator.addScript(schemaScript);
//        populator.addScript(dataScript);
//        return populator;
//    }

}

