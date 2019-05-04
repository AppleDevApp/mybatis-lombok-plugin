# mybatis-lombok-plugin
mybatis general Lombok and Guava Support Plugin

# 使用说明
拉取代码，然后在本地install到本地maven库里
使用maven的mybatis生成插件
在配置文件里，加上代码

(```)
    <!-- 使用自定义的插件 -->
    <plugin type="com.codebox.codetemplate.plugin.LombokPlugin"/>
    <plugin type="com.codebox.codetemplate.plugin.GuavaStringPlugin"/>
    <plugin type="com.codebox.codetemplate.plugin.GuavaHashPlugin"/>
(```)

在pom.xml里添加插件依赖
(```)
 <!-- mybatis generator 自动生成代码插件 -->
            <plugin>
                <groupId>org.mybatis.generator</groupId>
                <artifactId>mybatis-generator-maven-plugin</artifactId>
                <version>1.3.7</version>
                <configuration>
                    <skip>${skip-mybatis-codegen}</skip>
                    <overwrite>true</overwrite>
                    <verbose>true</verbose>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.mybatis.generator</groupId>
                        <artifactId>mybatis-generator-core</artifactId>
                        <version>1.3.7</version>
                    </dependency>
                    <dependency>
                        <groupId>mysql</groupId>
                        <artifactId>mysql-connector-java</artifactId>
                        <version>8.0.16</version>
                    </dependency>
                    <dependency>
                        <groupId>com.codebox</groupId>
                        <artifactId>mybatis-lombok-plugin</artifactId>
                        <version>1.0.0</version>
                    </dependency>
                </dependencies>
            </plugin>
(```)
