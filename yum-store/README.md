# yum 源码

## 运行效果(以提供unzip源命令为例)

###解压
cd 到解压后的bin目录

###搜索,下载unzip相关的rpm命令:
	yumtool download unzip -o centos/7/os/x86_64/Packages -c conf/yum-store-centos-7.xml
	
其他相关的可以用yumtool download -help 命令查询

###生成repo索引文件
	yumtool repo centos/7/os/x86_64

### 配置nginx
	location / {
		root  ${path}
		autoindex on;
		autoindex_exact_size on;
		autoindex_localtime on;
	}
${path}为解压后的目录


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