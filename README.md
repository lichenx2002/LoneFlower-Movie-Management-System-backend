git add README.md# CinemaBackend 影院管理系统后端

## 项目简介
本项目是一个基于 Spring Boot + MyBatis-Plus 的影院管理系统后端，支持用户注册、登录、电影排片、在线选座、购票、订单、评论等核心功能。

## 技术栈
- Java 17
- Spring Boot 3.x
- MyBatis-Plus
- MySQL
- Lombok
- Maven

## 主要功能
- 用户注册、登录、信息修改
- 管理员管理电影、影厅、场次、座位模板
- 电影排片、场次管理
- 在线选座、购票、订单管理
- 用户评论、评分

## 目录结构
```
cinemabackend/
├── src/
│   ├── main/
│   │   ├── java/com/example/cinemabackend/
│   │   │   ├── controller/   # 控制器层
│   │   │   ├── entity/       # 实体类
│   │   │   ├── mapper/       # MyBatis-Plus Mapper接口
│   │   │   ├── service/      # Service接口
│   │   │   ├── service/impl/ # Service实现
│   │   │   └── dto/          # DTO对象
│   │   └── resources/
│   │       ├── application.yml # 配置文件
│   │       ├── db/            # SQL脚本
│   │       └── mapper/        # MyBatis XML
│   └── test/                  # 测试
├── pom.xml                    # Maven依赖
└── README.md                  # 项目说明
```

## 启动方式
1. 配置数据库连接（`src/main/resources/application.yml`）
2. 导入SQL建表语句和初始数据（`src/main/resources/db/`）
3. 使用IDEA或命令行运行 `CinemabackendApplication.java`
4. 访问接口文档或用Postman测试

## 数据库说明
- 使用MySQL，建表语句见 `src/main/resources/db/`
- 主要表：user、admin、movie、hall、seat_template、schedule、schedule_seat、orders、order_seat、comment

## 常用接口示例
- 用户注册：`POST /user/register`
- 用户登录：`POST /user/login`
- 获取电影列表：`GET /movie/list`
- 获取场次详情：`GET /schedules/{scheduleId}/detail`
- 选座锁定：`POST /schedules/{scheduleId}/seats/lock`
- 订单支付：`POST /orders/pay`

## .gitignore 配置说明
- 已配置忽略 `target/`、`.idea/`、`.mvn/`、`build/`、`.vscode/` 等IDE和编译生成文件
- 不上传数据库数据文件、日志、临时文件等大文件
- 仅上传源码、配置、SQL脚本等主要文件

## 贡献&交流
如有建议或问题，欢迎提issue或PR。
