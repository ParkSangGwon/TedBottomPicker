 
#What is TedBottomPicker?
In Google's Material Design, Google introduce **Bottom sheets**.([Components – Bottom sheets](https://material.google.com/components/bottom-sheets.html))<br/>
**Bottom sheets** slide up from the bottom of the screen to reveal more content.

If you want pick image from gallery or take picture, this library can help easily.<br/>
**TedBottomPicker** provide 3 options: <br/>

1. Take a picture by camera(using `MediaStore.ACTION_IMAGE_CAPTURE` intent)
2. Get image from gallery(using `Intent.ACTION_PICK` intent)
3. Get image from recent image(using `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` cursor)


**TedBottomPicker** is simple image picker using bottom sheet.

<br/><br/>



##Demo

![Screenshot](https://github.com/ParkSangGwon/TedBottomPicker/blob/master/screenshot1.jpeg?raw=true)    ![Screenshot](https://github.com/ParkSangGwon/TedBottomPicker/blob/master/demo.gif?raw=true)    
           
           
1. Show Bottom Sheet.
2. Pick Image


<br/><br/>


##Setup


###Gradle
```javascript

dependencies {
    compile 'gun0912.ted:tedbottompicker:1.0.2'
}

```

<br/><br/>



##How to use
###1. Check Permission
You have to grant `WRITE_EXTERNAL_STORAGE` permission from user.<br/>
If your targetSDK version is 23+, you have to check permission and request permission to user.<br/>
Because after Marshmallow(6.0), you have to not only decalare permisions in `AndroidManifest.xml` but also request permissions at runtime.<br/>
There are so many permission check library in [Android-Arsenal](http://android-arsenal.com/tag/235?sort=rating)<br/>
I recommend [TedPermission](https://github.com/ParkSangGwon/TedPermission)<br/>
**TedPermission** is super simple and smart permission check library.<br/>
<br/>


###2. Start TedBottomPicker
**TedBottomPicker** class extend `BottomSheetDialogFragment`.<br/>
`TedBottomPicker.Builder` make `new TedBottomPicker()`.<br/>
After then, you can show TedBottomPicker<br/>


```javascript

     TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(MainActivity.this)
                                .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                    @Override
                                    public void onImageSelected(Uri uri) {
                                        // here is selected uri
                                    }
                                })
                                .create();

     tedBottomPicker.show(getSupportFragmentManager());
```


**Don't forget!!**<br/>
You have to declare `setOnImageSelectedListener()` in Builder.<br/>
This listener will pass selected Uri.<br/>




<br/>

##Customize
You can customize something ...<br />

###Function



* `setMaxCount(Int) (default: 25)`
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

<br/><br/>



##Thanks 
* [Flipboard-bottomsheet](https://github.com/Flipboard/bottomsheet) - Android component which presents a dismissible view from the bottom of the screen




<br/><br/>


##License 
 ```code
Copyright 2016 Ted Park

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.```
