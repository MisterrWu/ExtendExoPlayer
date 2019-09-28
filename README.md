   Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

   Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.MisterrWu:ExtendExoPlayer:1.0.4'
	}
