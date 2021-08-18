# AssetRefGenerator
Makes it easier to use and preview assets in [Flutter][1], just like using R.drawable.xxx in Android!

View asset images quickly in both yaml and dart file.

![yaml usage](https://andrewshen812.github.io/AssetRefGenerator/usage_yaml.gif)
![dart usage](https://andrewshen812.github.io/AssetRefGenerator/usage_dart.gif)

Support asset image preview in a variety of dart code writing styles.

![dart usage](https://andrewshen812.github.io/AssetRefGenerator/usage_dev.gif)

[中文文档][2]

## Features
### 1.1.0
 - You can view the asset image by click the gutter icon, or ctrl+click, or mouse hover, even you didn't generate the res.dart.
 - When rename a asset file, the related reference string will also be updated automatically.
### 1.0.0
 - Update assets declaration in pubspec.yaml automatically.
 - Generate a **res.dart** file contained assets definition.

## Getting started
 - Open your Flutter project.
 - Create assets directory named asset, assets, or images, put your asset files in the directory.
 - Click the action button ![Image text](https://andrewshen812.github.io/AssetRefGenerator/genAssetRef.svg) in the Toolbar.
 - Now you'll see the pubspec.yaml file has been updated, and a res.dart file also has been created under lib.

## Change-notes
1.1.0
 - When there's a asset reference in the code line, the editor will show a gutter icon. Clicking the gutter icon will open the asset file.
 - Support ctrl+click on asset reference to open the asset file in the editor.
 - Mouse hover on a asset reference in the code line, the documentation window will show the asset image preview.
 - When rename a asset file, the related reference string will also be updated automatically.
 - Bug fix: the generated res.dart has syntax error when asset file name contains hyphen([#8][4]). The solution is to replace hyphen with a underline.

1.0.1
 - Support Flutter Module, Package and Plugin project.
 - Ignore .DS_Store file on Mac OS X.
 
1.0.0
 - Implement basic functions.
 
## Thanks
 - Logo and icon: [Iconfont-阿里巴巴矢量图标库][3]

[1]:https://flutter.dev/
[2]:https://github.com/AndrewShen812/AssetsRefGenerator/blob/master/README_zh.md
[3]:https://www.iconfont.cn/search/index?q=flutter
[4]:https://github.com/AndrewShen812/AssetsRefGenerator/issues/8