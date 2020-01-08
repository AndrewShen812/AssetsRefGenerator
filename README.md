Flutter 资源声明和Dart引用生成插件。
## 功能
扫描工程 asset/assets/images 目录下的资源文件，自动在 pubspec.yaml 文件中添加资源文件声明；并生成一个 res.dart 文件，
包含所有资源文件的字符串声明。

## 主要解决问题
无需手动编辑 pubspec.yaml 中的资源文件声明和代码中的资源引用字符串。即避免出错，也方便开发编码，像 Android 中
R.drawable.xxx 方式一样，更加愉快的引用资源。
