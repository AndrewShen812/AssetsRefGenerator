# AssetRefGenerator
[Flutter][1] 资源声明和Dart引用生成工具。像 Android 中 R.drawable.xxx 方式一样，更加愉快的引用资源、查看资源。

无需手动编辑 pubspec.yaml 中的资源文件声明和代码中的资源字符串。避免出错，也方便开发编码。

在 yaml 和 dart 文件中快速查看资源图片预览：
![yaml usage](https://andrewshen812.github.io/AssetRefGenerator/usage_yaml.gif)
![dart usage](https://andrewshen812.github.io/AssetRefGenerator/usage_dart.gif)

支持多种引用资源图片的 dart 代码写法：
![dart usage](https://andrewshen812.github.io/AssetRefGenerator/usage_dev.gif)

支持自定义生成文件的路径、生成类的名称和类字段的命名风格：
![settings_usage](https://andrewshen812.github.io/AssetRefGenerator/usage_settings.gif)

[English Doc][2]

## 功能
### 1.2.0
 - 插件支持自定义设置了！你可以配置生成文件的路径、生成类的名称和类字段的命名风格。
  设置入口：Preferences/Settings > Tools > Flutter Assets Ref Generator
 - 优先使用指定目录的方式在 pubspec.yaml 中指定资源，如：`- assets/images/`。如果已存在类似`- assets/images/home.png`的具体文件指定方式，
则继续沿用。
### 1.1.0
 - 可以通过：点击编辑器左侧图标、或 ctrl + click、或鼠标悬停，3种方式快速查看资源图片，即使在这之前没有生成 res.dart。
 - 重命名资源文件时，dart 和 yaml 中相关联的资源引用字符串也会自动更新。
### 1.0.0
 - 自动更新 pubspec.yaml 文件中的资源声明。
 - 生成 res.dart 文件，其中包含资源文件的 String 类型使用定义。

## 使用方法
 - 打开一个 Flutter 工程。
 - 创建资源目录，名称可以是 asset, assets, 或 images，并将资源文件放在该目录下。
 - 点击 IDE 工具栏的 ![Image text](https://andrewshen812.github.io/AssetRefGenerator/genAssetRef.svg) 按钮。
 - 打开 pubspec.yaml 看一下，你将看到文件中已经自动添加了资源文件的声明。并且在lib目录下生成了一个res.dart文件，其中包含资源文件的 String 类型使用定义。

## 更新日志
1.2.0
 - 插件支持自定义设置([#14][5])，可以配置生成文件的路径、生成类的名称和类字段的命名风格。
 - 优先使用指定目录的方式在 pubspec.yaml 中指定资源，如：`- assets/images/`。如果已存在类似`- assets/images/home.png`的具体文件指定方式，
   则继续沿用。
 - Bug fix([#17][5]): 不再检查.packages文件是否存在。

1.1.0
 - 当 dart 或 pubspec.yaml 代码行中包含一个资源引用字符串时，编辑器左侧会显示一个图标。点击图标可以打开图片文件。
 - 支持在资源引用字符串上 ctrl + click 打开图片文件。
 - 鼠标悬停在资源引用字符串上时，文档悬浮窗会显示资源图片预览。
 - 重命名资源文件时，dart 和 yaml 中相关联的资源引用字符串也会自动更新。
 - Bug fix([#8][4]): 当资源文件名包含连字符'-'时，生成的res.dart中常量名也有连字符，显示语法错误。解决方法是将名称中的连字符'-'替换为下划线'_'。

1.0.1
 - 支持Flutter Module、Package、Plugin项目类型.
 - 忽略Mac OS X 上的 .DS_Store 文件.
 
1.0.0
 - 初始版本功能添加和完善

## 致谢
 - 插件logo及图标：[Iconfont-阿里巴巴矢量图标库][3]
 
[1]:https://flutterchina.club/
[2]:https://github.com/AndrewShen812/AssetsRefGenerator
[3]:https://www.iconfont.cn/search/index?q=flutter
[4]:https://github.com/AndrewShen812/AssetsRefGenerator/issues/8
[5]:https://github.com/AndrewShen812/AssetsRefGenerator/issues/14
[6]:https://github.com/AndrewShen812/AssetsRefGenerator/issues/17
