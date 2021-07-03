 
# What is TedBottomPicker?
In Google's Material Design, Google introduce **Bottom sheets**.([Components – Bottom sheets](https://material.google.com/components/bottom-sheets.html))<br/>
**Bottom sheets** slide up from the bottom of the screen to reveal more content.

If you want pick image from gallery or take picture, this library can help easily.<br/>
**TedBottomPicker** provide 3 options: <br/>

1. Take a picture by camera(using `MediaStore.ACTION_IMAGE_CAPTURE` intent)
2. Get image from gallery(using `Intent.ACTION_PICK` intent)
3. Get image from recent image(using `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` cursor)


**TedBottomPicker** is simple image picker using bottom sheet.

<br/><br/>
## TedImagePicker
- If you want full screen image picker, use [TedImagePicker](https://github.com/ParkSangGwon/TedImagePicker)
- TedImagePicker is simple/beautiful/smart image picker

<img src="https://github.com/ParkSangGwon/TedImagePicker/raw/master/art/multi_select.gif" width="200"> .   <img src="https://github.com/ParkSangGwon/TedImagePicker/raw/master/art/album.gif" width="200">

<br/><br/>
## Demo
1. Show Bottom Sheet.
2. Pick Image

### Single/Multi Select

![Screenshot](https://github.com/ParkSangGwon/TedBottomPicker/blob/master/screenshot1.jpeg?raw=true)    ![Screenshot](https://github.com/ParkSangGwon/TedBottomPicker/blob/master/demo.gif?raw=true)  
![Screenshot](https://github.com/ParkSangGwon/TedBottomPicker/blob/master/screenshot_multi_select.jpeg?raw=true)    

           




## Setup


### Gradle
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ParkSangGwon/tedbottompicker.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.ParkSangGwon%22%20AND%20a:%tedbottompicker%22)
```javascript
dependencies {
    implementation 'gun0912.ted:tedbottompicker:x.y.z'
    //implementation 'gun0912.ted:tedbottompicker:2.0.1'
}

```

If you think this library is useful, please press star button at upside. 
<br/>
<img src="https://phaser.io/content/news/2015/09/10000-stars.png" width="200">
<br/><br/>



## How to use
### 1. Check Permission
You have to grant `WRITE_EXTERNAL_STORAGE` permission from user.<br/>
If your targetSDK version is 23+, you have to check permission and request permission to user.<br/>
Because after Marshmallow(6.0), you have to not only declare permissions in `AndroidManifest.xml` but also request permissions at runtime.<br/>
There are so many permission check library in [Android-Arsenal](http://android-arsenal.com/tag/235?sort=rating)<br/>
I recommend [TedPermission](https://github.com/ParkSangGwon/TedPermission)<br/>
**TedPermission** is super simple and smart permission check library.<br/>
<br/>


### 2. Start TedBottomPicker
- TedBottomPicker support `RxJava` and `Listener` style
<br/><br/>
### RxJava
**You have to use TedRxBottomPicker**
- Single image
```java
        TedRxBottomPicker.with(MainActivity.this)
                 .show()
                 .subscribe(uri ->
                    // here is selected image uri
                 }, Throwable::printStackTrace);


```
- Multi image
```java
        
        TedRxBottomPicker.with(MainActivity.this)
                //.setPeekHeight(getResources().getDisplayMetrics().heightPixels/2)
                .setPeekHeight(1600)
                .showTitle(false)
                .setCompleteButtonText("Done")
                .setEmptySelectionText("No Select")
                .setSelectedUriList(selectedUriList)
                .showMultiImage()
                .subscribe(uris -> {
                   // here is selected image uri list
                }, Throwable::printStackTrace);

```

<br/><br/>
### Listener
**You have to use TedBottomPicker**
- Single image
```java
        TedBottomPicker.with(MainActivity.this)
               .show(new TedBottomSheetDialogFragment.OnImageSelectedListener() {
                   @Override
                   public void onImageSelected(Uri uri) {
                     // here is selected image uri
                   }
               });

```

- Multi image
```java
        TedBottomPicker.with(MainActivity.this)
               .setPeekHeight(1600)
               .showTitle(false)
               .setCompleteButtonText("Done")
               .setEmptySelectionText("No Select")
               .setSelectedUriList(selectedUriList)
               .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
                    @Override
                    public void onImagesSelected(List<Uri> uriList) {
                         // here is selected image uri list
                    }
              });
```
<br/>

## Customize
You can customize something ...<br />

### Function

#### Common

* `showVideoMedia()` : Not only load image, but also load video
* `setPreviewMaxCount(Int) (default: 25)`
* `setPeekHeight(Int)`
* `setPeekHeightResId(R.dimen.xxx)`
* `showCameraTile(Boolean) (default: true)`
* `setCameraTile(R.drawable.xxx or Drawable)`
* `setCameraTileBackgroundResId(R.color.xxx)`
* `setGalleryTile(R.drawable.xxx or Drawable)`
* `showGalleryTile(Boolean) (default: true)`
* `setGalleryTileBackgroundResId(R.color.xxx)`
* `setSpacing(Int)`
* `setSpacingResId(R.dimen.xxx)`
* `setOnErrorListener(OnErrorListener)`
* `setTitle(String or R.string.xxx) (default: 'Select Image','사진 선택')`
* `showTitle(Boolean) (default: true)`
* `setTitleBackgroundResId(R.color.xxx)`
* `setImageProvider(ImageProvider)`
: If you want load grid image yourself, you can use your ImageProvider

#### Single Select
* `setSelectedUri(Uri)`

#### Multi Select
* `setDeSelectIcon(R.drawable.xxx or Drawable)`
* `setSelectedForeground(R.drawable.xxx or Drawable)`
* `setSelectMaxCount(Int)`
* `setSelectMinCount(Int)`
* `setCompleteButtonText(String or R.string.xxx) (default: 'Done','완료')`
* `setEmptySelectionText(String or R.string.xxx) (default: 'No Image','이미지가 선택되지 않았습니다')`
* `setSelectMaxCountErrorText(String or R.string.xxx)`
* `setSelectMinCountErrorText(String or R.string.xxx)`
* `setSelectedUriList(ArrayList<Uri>)`

<br/><br/>



## Thanks 
* [Flipboard-bottomsheet](https://github.com/Flipboard/bottomsheet) - Android component which presents a dismissible view from the bottom of the screen




<br/><br/>


## License 
 ```code
Copyright 2017 Ted Park

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.```
