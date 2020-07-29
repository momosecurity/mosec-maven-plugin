# MOSEC-MAVEN-PLUGIN

用于检测maven项目的第三方依赖组件是否存在安全漏洞。

该项目是基于 [snyk-maven-plugin](https://github.com/snyk/snyk-maven-plugin.git) 的二次开发。

## 关于我们

Website：https://security.immomo.com

WeChat:

<img src="https://momo-mmsrc.oss-cn-hangzhou.aliyuncs.com/img-1c96a083-7392-3b72-8aec-bad201a6abab.jpeg" width="200" hegiht="200" align="left" />

## 版本要求

Maven >= 3.1

## 安装

#### 向pom.xml中添加plugin仓库

```xml
<!-- pom.xml -->
<pluginRepositories>
  <pluginRepository>
      <id>gh</id>
      <url>https://raw.github.com/momosecurity/mosec-maven-plugin/master/mvn-repo/</url>
  </pluginRepository>
</pluginRepositories>
```

## 使用

首先运行 [MOSEC-X-PLUGIN Backend](https://github.com/momosecurity/mosec-x-plugin-backend.git)

#### 命令行使用
```
> cd your_maven_project_dir/
> MOSEC_ENDPOINT=http://127.0.0.1:9000/api/plugin \
  mvn com.immomo.momosec:mosec-maven-plugin:1.0.6:test \
  -DonlyProvenance=true

# .m2/settings.xml 中增加如下配置，可简化使用命令
--------------------------------
    <!-- .m2/settings.xml -->

    <pluginGroups>
        <pluginGroup>com.immomo.momosec</pluginGroup>
    </pluginGroups>
--------------------------------
> MOSEC_ENDPOINT=http://127.0.0.1:9000/api/plugin \
  mvn mosec:test -DonlyProvenance=true
```

#### 项目中使用

```xml
<!-- pom.xml -->

<plugins>
    <plugin>
        <groupId>com.immomo.momosec</groupId>
        <artifactId>mosec-maven-plugin</artifactId>
        <version>1.0.6</version>
        <executions>
            <execution>
                <id>test</id>
                <goals>
                    <goal>test</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <endpoint>http://127.0.0.1:9000/api/plugin</endpoint>
            <severityLevel>High</severityLevel>
            <onlyProvenance>true</onlyProvenance>
            <failOnVuln>true</failOnVuln>
        </configuration>
    </plugin>
</plugins>
```

## 开发

#### Intellij 远程调试 Maven 插件

1.将mosec-maven-plugin安装至本地仓库

2.git clone mosec-maven-plugin

3.Intellij 中新建 Remote Configuration 并填入如下信息

![remote-configuration](https://github.com/momosecurity/mosec-maven-plugin/blob/master/static/remote-configuration.jpg)

4.在另一个maven工程中执行如下命令

```shell script
> mvnDebug com.immomo.momosec:mosec-maven-plugin:1.0.6:test
```

5.回到Intellij中，下断点，开始Debug
