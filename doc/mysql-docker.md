### 本地环境搭建

```
docker pull mysql:5.7

mkdir -p /usr/local/docker/mysql/conf
mkdir -p /usr/local/docker/mysql/logs
mkdir -p /usr/local/docker/mysql/data
```


#### /usr/local/docker/mysql/conf/ 目录下创建 my.cnf
```
[mysqld]
user=mysql
character-set-server=utf8
default_authentication_plugin=mysql_native_password
secure_file_priv=/var/lib/mysql
expire_logs_days=7
sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION
max_connections=1000
secure_file_priv=/var/lib/mysql

[client]
default-character-set=utf8

[mysql]
default-character-set=utf8
```

#### 启动
```
docker run -p 3306:3306 --name mysql -v /usr/local/docker/mysql/conf:/etc/mysql/conf.d -v /usr/local/docker/mysql/logs:/var/log/mysql -v /usr/local/docker/mysql/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=123456 -d mysql:5.7 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
```