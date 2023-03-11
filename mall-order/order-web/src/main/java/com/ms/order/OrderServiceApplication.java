package com.ms.order;

import com.ms.cart.ShoppingCartServiceFeign;
import com.ms.product.ProductServiceFeign;
import com.ms.user.api.UserServiceFeign;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@ComponentScan(basePackages = "com.ms")
@EnableFeignClients(basePackageClasses = {UserServiceFeign.class, ProductServiceFeign.class, ShoppingCartServiceFeign.class})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
