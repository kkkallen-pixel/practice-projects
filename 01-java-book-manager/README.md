# 01 Java + MySQL 图书管理系统

这是用 **Java SE 8 + JDBC + MySQL** 编写的图书管理练手项目。
代码按常见企业分层组织：`model`（实体）→ `dao`（数据访问）→ `service`（业务）→ `ui`（控制台菜单），方便自己理解和在面试时讲解。

## 功能

- 图书：新增、查询（按书名/作者/ISBN 模糊搜索）、删除；
- 读者：新增、查询；
- 借书：校验库存后借出（使用事务，防止并发超借）；
- 还书：归还后自动恢复库存；
- 在借记录查询。

## 环境准备

1. 安装 JDK 8 或更高版本，并配置好 `JAVA_HOME`。
2. 安装 MySQL 5.7 / 8.0。
3. 下载 MySQL Connector/J 8.x 的 jar 包，放到项目根目录。
   - 官方下载：https://dev.mysql.com/downloads/connector/j/
   - 也可以把 Maven 中央仓库里的 `mysql-connector-j-8.0.33.jar` 下载到本目录。

## 初始化数据库

打开 MySQL 命令行（或 Navicat），执行 `sql/init.sql`。
它会自动创建数据库 `book_manager`、三张表和示例数据。

## 配置连接信息

复制 `db.properties.example` 为 `db.properties`，改成你自己的 MySQL 账号密码：

```properties
db.url=jdbc:mysql://localhost:3306/book_manager?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
db.username=root
db.password=你的密码
```

## 编译与运行

Windows 上打开 PowerShell，在项目根目录执行：

```powershell
.\run.ps1
```

脚本会提示输入 mysql 驱动 jar 的文件名（例如 `mysql-connector-j-8.0.33.jar`），然后自动编译并启动。

也可以手动执行：

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp "out;mysql-connector-j-8.0.33.jar" com.demo.library.Main
```

## 代码里值得讲给面试官的点

- 所有 SQL 都使用 `PreparedStatement` 参数占位，避免 SQL 注入；
- 借书/还书放在同一个事务里：先锁定图书行检查库存，再插入借阅记录并扣减库存；
- 图书库存用 `available` 字段维护，业务上禁止把已借出图书删除；
- DAO 与 Service 分层，之后把控制台界面换成 Web 界面时，Service 层不用改。

## 你可以怎么讲

*“这是我课程项目里做得比较完整的一个。数据库三张表是图书、读者和借阅记录；借书时会先查库存，再在事务里扣减库存并写入记录。为了不让中文乱码，我统一用 UTF-8 连接参数和建表字符集。下一步我想把它改造成 Spring Boot + REST 接口的版本。”*

> 注意：面试前请亲手运行至少一遍，并尝试改一个小功能（比如新增“出版社”字段），确保你能讲清每一层在做什么。
