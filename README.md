sshexec-maven-plugin
====================

ssh的文件上传和命令执行的插件，可用于项目自动化部署，亲测可用

基于：https://github.com/somesky/sshexec-maven-plugin

## quickStart
#### 1. 事前检查
插件有一些独特配置，在打包前请确认，主要在pom.xml中，我们指定了Java版本为17，如果您编译环境低于此版本，可能会产生问题。因此请在执行第二步前将此版本调整为您正使用的Java版本：（如下代码中version），另外，Java的版本不建议低于8

```
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>{version}</maven.compiler.source>
        <maven.compiler.target>{version}</maven.compiler.target>
        <maven.compiler.compilerVersion>{version}</maven.compiler.compilerVersion>
    </properties>
```

#### 2. 引入依赖

克隆项目，执行 `mvn install` 将代码加载到本地仓库(或者使用私服)，目前暂未上传至中央仓库

#### 3. 配置插件

在需要引入的项目中，添加配置，以下是配置示例：(版本参与install的版本)

其中的 “<phase>package</phase>” 是可选配置，表示将此插件执行绑定在package生命周期中，这样能在每次打包时自动部署，您也可以注释该配置，通过执行goal命令单独部署

```
 <build>
            <!-- 自动打包上传插件 -->
            <plugin>
                <groupId>cn.fishmaple</groupId>
                <artifactId>sshexec</artifactId>
                <version>1.0.2</version>
                <configuration>
                    <!-- 目标host -->
                    <host>10.0.0.1</host>
                    <!-- 用户名 -->
                    <user>root</user>
                    <!-- ssh端口 -->
                    <port>22</port>
                    <!-- 需要传输的文件或目录 -->
                    <oriFile>\target\demo.jar</oriFile>
                    <!-- 目标目录 -->
                    <destDirectory>/root/workspace</destDirectory>
                    <!-- 连接公钥(可选) -->
                    <identify>${pom.basedir}\src\main\resources\key</identify>
                    <!-- 连接密码(可选) -->
                    <passwd>123</passwd>
                    <!-- 文件传输前执行命令 -->
                    <precommands>
                        <command>echo 'start'</command>
                    </precommands>
                    <!-- 文件传输后执行命令 -->
                    <commands>
                        <command>sudo ./start.sh</command>
                    </commands>
                </configuration>
                <executions>
                    <execution>
                        <id>main</id>
                        <!-- 将该行为绑定至package，每次打包后将直接部署 -->
                        <phase>package</phase>
                        <goals>
                            <goal>exec</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

```



      
      
