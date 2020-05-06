# AssetRefGenerator
[Flutter][1] 资源声明和Dart引用生成工具。像 Android 中 R.drawable.xxx 方式一样，更加愉快的引用资源。

无需手动编辑 pubspec.yaml 中的资源文件声明和代码中的资源字符串。避免出错，也方便开发编码。

[English Doc][2]

## 功能
 - 自动更新 pubspec.yaml 文件中的资源声明。
 - 生成 res.dart 文件，其中包含资源文件的 String 类型使用定义。

## 使用方法
 - 打开一个 Flutter 工程。
 - 创建资源目录，名称可以是 asset, assets, 或 images，并将资源文件放在该目录下。
 - 点击 IDE 工具栏的 ![Image text](https://chinastyle812.github.io/AssetRefGenerator/genAssetRef.svg) 按钮。
 - 打开 pubspec.yaml 看一下，你将看到文件中已经自动添加了资源文件的声明。并且在lib目录下生成了一个res.dart文件，其中包含资源文件的 String 类型使用定义。

## 更新日志
1.0.1
 - 支持Flutter Module、Package、Plugin项目类型.
 - 忽略Mac OS X 上的 .DS_Store 文件.
 
1.0.0
 - 初始版本功能添加和完善

## 致谢
 - 插件logo及图标：[Iconfont-阿里巴巴矢量图标库][3]
 
[1]:https://flutterchina.club/
[2]:https://github.com/ChinaStyle812/AssetsRefGenerator
[3]:https://www.iconfont.cn/search/index?q=flutter
