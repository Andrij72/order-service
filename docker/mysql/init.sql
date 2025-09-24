CREATE DATABASE IF NOT EXISTS order_service;
CREATE USER 'usermysql'@'%' IDENTIFIED WITH mysql_native_password BY 'mysql';
GRANT ALL PRIVILEGES ON order_service.* TO 'usermysql'@'%';
FLUSH PRIVILEGES;