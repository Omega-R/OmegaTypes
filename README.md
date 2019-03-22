[![](https://jitpack.io/v/Omega-R/OmegaTypes.svg)](https://jitpack.io/#Omega-R/OmegaTypes)
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://opensource.org/licenses/MIT)

# OmegaTypes
OmegaTypes it's cover for:  
1) String and @StringRes. 
You can create Text class inside your presenter or somewhere else, and set it in activity. Activity doesn't need to know what type 
of resource you use.
2) Drawable, Bitmap, @DrawableRes. Class Image has the similar idea with Text class.

# Installation
To get a Git project into your build:

**Step 1.** Add the JitPack repository to your build file
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
**Step 2.** Add the dependency
```
dependencies {
    implementation 'com.github.Omega-R.OmegaTypes:omegatypes:0.0.6'
    // implementation 'com.github.Omega-R.OmegaTypes:picasso:0.0.6' // for load url
}
```

# Usage

```
val text = Text.from(R.string.hello_world)
text.applyTo(exampleTextView) // or textView.setText(text)
        
val image = Image.from(R.mipmap.ic_launcher)
image.applyTo(imageView) // or imageView.setImage(image)

```

# License
```
The MIT License

Copyright 2018 Omega-R

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
associated documentation files (the "Software"), to deal in the Software without restriction, 
including without limitation the rights to use, copy, modify, merge, publish, distribute, 
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is 
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
