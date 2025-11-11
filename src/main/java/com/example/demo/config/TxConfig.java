package com.example.demo.config;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

//설정파일
@Configuration
@EnableTransactionManagement //TX매니저 환경을 만들어 주겠다!
public class TxConfig {

    //dataSource 거내쓰기
    @Autowired
    private DataSource dataSource;

//    //기본 Tx(Mybatis용)
//    @Bean(name="dataSourceTransactionManager")
//    public DataSourceTransactionManager transactionManager(){
//        return new DataSourceTransactionManager(dataSource);
//    }

    //JPA Tx - JPA 전용 TX매니저를 이용해서 DATASOURCE를 이용해 관리
    @Bean(name="jpaTransactionManager")

            //JpaTransactionManager를 JpaConfig에 포함시키는 작업
    public JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {//만들어진 entityManagerFactory를 전달받음(의존주입)
                                                        //: 여러가지 엔터티를 관리하고 레파지토리를 이용해서 내용 삽입 담당
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        //EntityManagerFactory를 전달받아 JpaTransactionManager set-up하는 중
        //EntityManagerFactory에 TX Manager가 포함되게 됨
        transactionManager.setEntityManagerFactory(entityManagerFactory);
                                //이 매니저에 Transaction처리를 해주는 Manager가 포함됨
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }
}
