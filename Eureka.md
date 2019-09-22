## 创建Eureka Server服务端

### （1）引入依赖：

spring-boot工程

```xml
  <parent>

​        <groupId>org.springframework.boot</groupId>

​        <artifactId>spring-boot-starter-parent</artifactId>

​        <version>1.5.13.RELEASE</version>

​    </parent>
```

spring-cloud依赖    

```xml
    <dependencyManagement>

​        <dependencies>

​            <dependency>

​                <groupId>org.springframework.cloud</groupId>

​                <artifactId>spring-cloud-dependencies</artifactId>

​                <version>Edgware.SR3</version>

​                <type>pom</type>

​                <scope>import</scope>

​            </dependency>

​        </dependencies>

​    </dependencyManagement>

 

​    <dependencies>

​        <dependency>

​            <groupId>org.springframework.cloud</groupId>

​            <artifactId>spring-cloud-starter-config</artifactId>

​        </dependency>

​        <dependency>

​            <groupId>org.springframework.cloud</groupId>

​            <artifactId>spring-cloud-starter-eureka</artifactId>

​        </dependency>

​        <dependency>

​            <groupId>org.springframework.cloud</groupId>

​            <artifactId>spring-cloud-starter-eureka-server</artifactId>

​        </dependency>

​    </dependencies>
```

### （2）创建入口类

```java
@SpringBootApplication

@EnableEurekaServer

public class EurekaServer {

​    public static void main(String[] args) {

​        SpringApplication.run(EurekaServer.class, args);

​    }

}
```

@EnableEurekaServer注解表示这是一个Eureka服务

### （3）修改配置

```yaml
server:

  port: 8761

eureka:

  client:

​    registerWithEureka: false

​    fetchRegistry: false
```

### （4）运行程序，JDK需要是1.8，否则需要增加依赖

https://blog.csdn.net/alger_magic/article/details/83041811

打开浏览器，输入http://localhost:8761，就可以看到Eureka的控制台



## 开发被调用服务A

### （1）引入依赖

```xml
    <dependencies>

​        <dependency>

​            <groupId>org.springframework.cloud</groupId>

​            <artifactId>spring-cloud-starter-config</artifactId>

​        </dependency>

​        <dependency>

​            <groupId>org.springframework.cloud</groupId>

​            <artifactId>spring-cloud-starter-eureka</artifactId>

​        </dependency>

​    </dependencies>
```

（2）创建入口类

```java
@SpringBootApplication

@EnableEurekaClient

public class ServiceAApplication {

​    public static void main(String[] args) {

​        SpringApplication.run(ServiceAApplication.class, args);

​    }

}
```

@EnableEurekaClient注解说明这是一个Eureka客户端应用，会将自己注册到Eureka服务上去

（3）添加控制器

```java
@RestController

public class ServiceAController {

​    @RequestMapping(value = "/sayHello/{name}", 

​            method = RequestMethod.GET)

​    public String sayHello(@PathVariable("name") String name) {

​        return "{'msg': 'hello, " + name + "'}";  

​    }

}
```

（4）修改配置

```yaml
server:

  port: 8080

spring:

  application:

​    name: ServiceA

eureka:

  instance:

​    hostname: localhost

  client:

​    serviceUrl:

​      defaultZone: http://localhost:8761/eureka
```

配置了自己的服务名称，主机地址/端口，还有eureka服务的地址

（5）运行程序

看一下Eureka控制台，就会发现一个服务出现了。

输入http://localhost:8080/sayHello/yx，可以访问ServiceA提供的服务



## 开发服务调用者B

### （1）引入依赖

```xml
    <dependencies>

​        <dependency>

​            <groupId>org.springframework.cloud</groupId>

​            <artifactId>spring-cloud-starter-config</artifactId>

​        </dependency>

​        <dependency>

​            <groupId>org.springframework.cloud</groupId>

​            <artifactId>spring-cloud-starter-eureka</artifactId>

​        </dependency>

​        <dependency>

​            <groupId>org.springframework.cloud</groupId>

​            <artifactId>spring-cloud-starter-ribbon</artifactId>

​        </dependency>

​    </dependencies>
```

### （2）创建入口类

```java
@SpringBootApplication

@EnableEurekaClient

public class ServiceBApplication {

​    public static void main(String[] args) {

​        SpringApplication.run(ServiceBApplication.class, args);

​    }

}
```

@EnabeEurekaClient注解说明这是一个Eureka客户端应用，可以去eureka抓取注册的服务了，而且自己也会到eureka上去注册一下

### （3）添加控制器

```java
@RestController

@Configuration

public class ServiceBController {

​    @Bean

​    @LoadBalanced

​    public RestTemplate getRestTemplate() {

​        return new RestTemplate();

​    }

​    @RequestMapping(value = "/greeting/{name}", method = RequestMethod.GET)

​    public String greeting(@PathVariable("name") String name) {

​        RestTemplate restTemplate = getRestTemplate();

​        return restTemplate.getForObject("http://ServiceA/sayHello/" + name, String.class);

​    }

}
```

### （4）修改配置

```yaml
server:

  port: 9090

spring:

  application:

​    name: ServiceB

eureka:

  instance:

​    hostname: localhost

  client:

​    serviceUrl:

​      defaultZone: http://localhost:8761/eureka
```

配置了自己的服务名称，主机地址/端口，还有eureka服务的地址

### （5）运行程序

启动了ServiceB以后，在eureka控制台上，就可以看到ServiceA和ServiceB两个服务了.

然后在浏览器里访问，http://localhost:9090/greeting/leo，就可以看到结果了，这里的结果是ServiceA返回给ServiceB，再返回给浏览器的



## 作业

**搭建eureka集群模式，启动两个eureka server，启动两个serviceA，一个serviceB。网页访问serviceB，会分别调用到两台serviceA上去。确保可以运行后，上传到自己的github上，并确保下载下来可以运行。**



### 1、eureka注册中心集群

**添加hostname**

C:\Windows\System32\drivers\etc

127.0.0.1 peer1 peer2

 **修改配置**

```yaml
server:

  port: 8761

eureka:

  instance:

​    hostname: peer1

  client:

​    serviceUrl:

​      defaultZone: http://peer2:8762/eureka/
```



```yaml
server:

  port: 8762

eureka:

  instance:

​    hostname: peer2

  client:

​    serviceUrl:

​      defaultZone: http://peer1:8761/eureka/
```

启动两个eureka服务，互相注册，组成一个集群

### 2、将服务改造为集群

```yaml
server:
  port: 8080
spring:
  application:
    name: ServiceA
eureka:
  instance:
    hostname: localhost
  client:
    serviceUrl:
      defaultZone: http://peer1:8761/eureka,http://peer2:8762/eureka
```

### 3、改造服务调用者

```yaml
server:
  port: 9090
spring:
  application:
    name: ServiceB
eureka:
  instance:
    hostname: localhost
  client:
    serviceUrl:
      defaultZone: http://peer1:8761/eureka,http://peer2:8762/eureka
```

