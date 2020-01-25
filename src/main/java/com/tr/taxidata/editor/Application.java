package com.tr.taxidata.editor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
/*@ComponentScan({"com.tr.taxidata.editor.*"})
@EntityScan("com.tr.taxidata.editor.model")
@EnableJpaRepositories(basePackages = "com.tr.taxidata.editor.repository")*/
public class Application {

    public static void main(String[] args) {
        ApplicationContext applicationContext =
                SpringApplication.run(Application.class, args);
        for (String name : applicationContext.getBeanDefinitionNames()) {
            System.out.println(name);
        }
    }
}
