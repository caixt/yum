# yum 源码

## 运行效果(以提供unzip源命令为例)

###解压
cd 到解压后的bin目录

###搜索,下载unzip相关的rpm命令:
	yumtool retrive centos 7 x86_64 unzip -o source/centos/7/x86_64/Packages
	
其他相关的可以用yumtool retrive -help 命令查询

###加入自定义yum源
	<!-- 阿里云镜像-->
	<store>http://mirrors.aliyun.com/{os}/{releasever}/os/{basearch}</store>
	<!-- nginx镜像 -->
	<store>http://nginx.org/packages/{os}/{releasever}/{basearch}</store> 
	<!-- docker镜像 -->
	<store basearch="x86_64">https://yum.dockerproject.org/repo/main/{os}/{releasever}</store>
	yumtool retrive centos 7 x86_64 nginx -c conf/yum-store.custom.xml -v 1.10.1
	yumtool retrive centos 7 x86_64 docker-engine -c conf/yum-store.custom.xml

###生成repo索引文件
	yumtool repo source/centos/7/x86_64

### 配置nginx
	location / {
		root  ${path}
		autoindex on;
		autoindex_exact_size on;
		autoindex_localtime on;
	}
${path}为source目录


###验证
在浏览器中访问http://127.0.0.1/centos/7/os/x86_64/repodata
浏览器中存在3个压缩文件和一个repomd.xml


###安装yum源
在服务器提供yum源配置/etc/yum.repos.d/

	[myyumrepo]
	name=DockerRepository
	baseurl=https://#{sercer}/centos/7/os/x86_64
	enabled=1
	gpgcheck=0

> 服务器上可能有很多yum源被使用，可以在命令中添加 --disablerepo=\* --enablerepo=myyumrepo