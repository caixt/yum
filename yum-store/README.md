# yum 源码

## 运行效果

1.在yum-store创建目录centos/7/os/x86_64
2.在创建的目录中加入对应rpm文件
3.运行com.github.cat.yum.store.Main的测试用例
4.在nginx的配置中加入文件映射
5.在浏览器中访问http://127.0.0.1/centos/7/os/x86_64/repodata

***
## nginx配置
	location / {
		root    D:\\git\\cxt\\yum\\yum-store\\yum-store;
		autoindex on;
		autoindex_exact_size on;
		autoindex_localtime on;
	}
	#root为对应的目录

## 在服务器提供yum源配置/etc/yum.repos.d/
	[myyumrepo]
	name=DockerRepository
	baseurl=https://#{sercer}/centos/7/os/x86_64/repodata
	enabled=1
	gpgcheck=0

> 服务器上可能有很多yum源被使用，可以在命令中添加 --disablerepo=\* --enablerepo=myyumrepo