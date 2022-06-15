<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes" />
	<xsl:template match="/formats">
		<xsl:comment>Generated automatically from formats.xml and AndroidManifest.xsl. Do not edit.</xsl:comment>
		<manifest xmlns:android="http://schemas.android.com/apk/res/android"
			package="net.sf.recoil" android:versionCode="{translate(@version, '.', '')}" android:versionName="{@version}" android:installLocation="auto">
			<supports-screens android:largeScreens="true" android:xlargeScreens="true" />
			<application android:label="@string/app_name" android:icon="@drawable/ic_launcher" android:description="@string/app_description">
				<activity android:name=".Viewer" android:label="@string/app_name" android:exported="true"
					android:launchMode="singleTop" android:configChanges="mcc|mnc|keyboard|keyboardHidden|orientation|screenSize">
					<intent-filter>
						<action android:name="android.intent.action.MAIN" />
						<category android:name="android.intent.category.LAUNCHER" />
					</intent-filter>
				</activity>
			</application>
		</manifest>
	</xsl:template>
</xsl:stylesheet>
